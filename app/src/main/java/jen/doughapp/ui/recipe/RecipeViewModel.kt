package jen.doughapp.ui.recipe

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.toRoute
import jen.doughapp.data.Ingredient
import jen.doughapp.data.IngredientType
import jen.doughapp.data.Recipe
import jen.doughapp.data.RecipeRepository
import jen.doughapp.data.RecipeWithIngredients
import jen.doughapp.data.toDisplayModel
import jen.doughapp.domain.getHydration
import jen.doughapp.ui.navigation.RecipeDetail
import jen.doughapp.ui.navigation.RecipeEdit
import jen.doughapp.ui.utils.formatMultiplier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// The multiplier drives recipe scaling/weight calculations
data class MultiplierSyncState(
    val multiplier: Double,
    val customMultiplierInput: String,
    val refreshTrigger: Long = 0L
)

// Used for optimistic updates to the UI; ensures optimistic multiplier
// and optimistic customMultiplierInput are updated at the same time
data class OptimisticMultiplier(
    val multiplier: Double?,
    val customMultiplier: Double?,
    val customMultiplierInput: String?
)

class RecipeViewModel(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{
    private val args = try {
        // Try to get args from RecipeEdit
        savedStateHandle.toRoute<RecipeEdit>()
    } catch (e: Exception) {
        // Fallback for when we navigated via RecipeDetail instead
        val detailArgs = savedStateHandle.toRoute<RecipeDetail>()
        RecipeEdit(detailArgs.recipeId)
    }

    val recipeId = args.recipeId

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Used to provide instant UI feedback for multiplier and custom text
    // (OptimisticMultiplier ensures that optimistic multiplier and custom values are
    // updated at the same time)
    private val _optimisticMultiplierState = MutableStateFlow<OptimisticMultiplier?>(null)

    // Used to trigger data flow updates in cases when other StateFlow dependencies haven't
    // changed but a refresh is still needed
    private val _syncMultiplierTrigger = MutableStateFlow(0L)

    // Track launched job to update the multiplier
    private var _updateMultiplierJob: Job? = null

    // Used to sync the UI state to updated data
    @OptIn(ExperimentalCoroutinesApi::class)
    val multiplierSyncState: StateFlow<MultiplierSyncState> = combine(
        if (recipeId == -1L) flowOf(1.0) else repository.getSavedMultiplier(recipeId),
        if (recipeId == -1L) flowOf(null) else repository.getSavedCustomMultiplier(recipeId),
        _optimisticMultiplierState,
        _syncMultiplierTrigger
    ) { dbMultiplier, dbCustom, optimisticState, _ ->

        val isSyncing = optimisticState != null

        // Check if the DB values are caught up to the optimistic values.
        // If they are, the optimistic state will be reset to null.
        if (isSyncing) {
            val isMultiplierMatch = dbMultiplier == optimisticState.multiplier

            // Note: If optimistic input is null, we aren't tracking a custom change, so it's a match.
            // Otherwise, compare the numeric values (optimistic.customMultiplier vs dbCustom)
            val isCustomTextMatch = when {
                optimisticState.customMultiplierInput == null -> true
                optimisticState.customMultiplier != null && dbCustom != null -> {
                    kotlin.math.abs(dbCustom - optimisticState.customMultiplier) < 0.0001
                }
                else -> dbCustom == optimisticState.customMultiplier
            }

            if (isMultiplierMatch && isCustomTextMatch) {
                _optimisticMultiplierState.value = null
            }
        }

        // Determine what to show in the UI:
        // Prioritize optimistic values, otherwise use DB values
        val currentMultiplier = optimisticState?.multiplier ?: dbMultiplier

        val currentCustomInput = if (optimisticState != null && optimisticState.customMultiplierInput != null) {
            optimisticState.customMultiplierInput
        } else {
            dbCustom?.formatMultiplier() ?: ""
        }

        MultiplierSyncState(
            multiplier = currentMultiplier,
            customMultiplierInput = currentCustomInput,
            refreshTrigger = _syncMultiplierTrigger.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MultiplierSyncState(Double.NaN, "")
    )

    val recipes: StateFlow<List<RecipeWithIngredients>> = repository.allRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        Log.d("DOUGH_DEBUG", "init called")

        observeRecipeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecipeData() {
        viewModelScope.launch {
            Log.d("DOUGH_DEBUG", "launch observe recipe data")

            val dataFlow = if (recipeId == -1L) {
                flowOf(null to MultiplierSyncState(1.0, ""))
            } else {
                combine(
                    repository.getRecipeById(recipeId),
                    multiplierSyncState
                ) { recipeData, syncState ->
                    recipeData to syncState
                }
            }

            dataFlow.collect { (recipeData, syncState) ->
                // Now we perform the update logic once per emission
                if (recipeData == null) {
                    // If we expect a recipe (id != -1) but got null, it was deleted/missing.
                    // If we don't expect one (id == -1), we proceed to show the New Recipe draft.
                    if (recipeId != -1L) return@collect

                    val newDraft = RecipeDraft()
                    _uiState.update {
                        it.copy(
                            recipe = newDraft,
                            initialRecipe = newDraft,
                            displayIngredients = emptyList(),
                            isLoading = false
                        )
                    }
                } else {
                    val recipeDraft = recipeData.toDraft()
                    val displayIngredients = recipeData.ingredients.map {
                        it.toDisplayModel(recipeData.recipe.totalFlourAmount)
                    }
                    val hydration = getHydration(displayIngredients)

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            refreshTrigger = syncState.refreshTrigger,
                            recipe = recipeDraft,
                            // Note: Only set initialRecipe once when the data first loads
                            initialRecipe = if (currentState.initialRecipe.id != recipeId) {
                                recipeDraft
                            } else {
                                currentState.initialRecipe
                            },
                            displayIngredients = displayIngredients,
                            multiplier = syncState.multiplier,
                            customMultiplierInput = syncState.customMultiplierInput,
                            hydration = hydration,
                            totalWeight = displayIngredients.sumOf { it.amount }
                        )
                    }
                }
            }
        }
    }

    fun updateRecipeName(newName: String) {
        _uiState.update { currentState ->
            val newDraft = currentState.recipe.copy(name = newName)
            currentState.copy(
                recipe = newDraft,
                isEntryValid = newDraft.isValid()
            )
        }
    }

    fun updateFlourWeight(newWeight: String) {
        _uiState.update { currentState ->
            val newDraft = currentState.recipe.copy(flourWeight = newWeight)
            currentState.copy(
                recipe = newDraft,
                isEntryValid = newDraft.isValid()
            )
        }
    }

    fun updateYield(newYield: String) {
        _uiState.update { currentState ->
            val newDraft = currentState.recipe.copy(yield = newYield)
            currentState.copy(
                recipe = newDraft,
                isEntryValid = newDraft.isValid()
            )
        }
    }

//    // Update a specific ingredient in the list
//    fun updateIngredient(index: Int, updatedIngredient: IngredientDraft) {
//        _uiState.update { currentState ->
//            val newList = currentState.recipe.ingredients.toMutableList()
//            newList[index] = updatedIngredient
//            val newDraft = currentState.recipe.copy(ingredients = newList)
//            currentState.copy(recipe = newDraft)
//        }
//    }
//
//    fun addIngredient() {
//        _uiState.update { currentState ->
//            val newList = currentState.recipe.ingredients + IngredientDraft()
//            val newDraft = currentState.recipe.copy(ingredients = newList)
//            currentState.copy(recipe = newDraft)
//        }
//    }
//
//    fun removeIngredient(index: Int) {
//        _uiState.update { currentState ->
//            val newList = currentState.recipe.ingredients.toMutableList()
//            newList.removeAt(index)
//            val newDraft = currentState.recipe.copy(ingredients = newList)
//            currentState.copy(recipe = newDraft)
//        }
//    }

    fun updateIngredients(newIngredients: List<IngredientDraft>) {
        _uiState.update { currentState ->
            val newDraft = currentState.recipe.copy(ingredients = newIngredients)
            // Note: You should also trigger your validation logic here
            //check ingredients valid
            //FragileAutoDetectType ?
            currentState.copy(
                recipe = newDraft,
                isEntryValid = newDraft.isValid()
            )
        }
    }

    fun saveRecipe() {
        val currentDraft = _uiState.value.recipe
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            Log.d("RECIPE_DEBUG", "saving recipe")

            val recipeEntity = Recipe(
                id = currentDraft.id,
                name = currentDraft.name,
                totalFlourAmount = currentDraft.flourWeight.toDoubleOrNull() ?: 0.0,
                yield = currentDraft.yield,
                sortOrder = 1,
                createdTimestamp = currentDraft.createdTimestamp?: System.currentTimeMillis()
            )

            // Filter out blank ingredients
            val ingredientsToSave = currentDraft.ingredients.filter { !it.isBlank() }
            val ingredientEntities = ingredientsToSave.mapIndexed { index, it ->
                Ingredient(
                    recipeId = currentDraft.id,
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    bakersPercent = it.percentage.toDoubleOrNull() ?: 0.0,
                    sortOrder = index
                )
            }

            val newId = repository.upsertRecipeWithIngredients(recipeEntity, ingredientEntities)

            _uiState.update { currentState ->
                val finalId = if (newId > 0) newId else currentState.recipe.id


                Log.d("RECIPE_DEBUG", "updating state after saved")

                val savedDraft = currentState.recipe.copy(
                    id = finalId,
                    ingredients = ingredientsToSave
                )

                // Reset changed status by making sure recipe == initialRecipe
                currentState.copy(
                    recipe = savedDraft,
                    initialRecipe = savedDraft,
                    isSaving = false
                )
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    // For dev testing
    fun addSampleRecipe() {
        viewModelScope.launch {
            val sample = getSampleRecipe()
            repository.upsertRecipeWithIngredients(sample.recipe, sample.ingredients)
        }
    }

    fun onCustomMultiplierInputChange(input: String) {
        _uiState.update { it.copy(customMultiplierInput = input) }
    }

    fun updateMultiplier(input: String, commonMultipliers: List<Double>) {
        if (recipeId == -1L) return

        // If multiplier is NaN, the initial DB load hasn't finished yet.
        if (_uiState.value.multiplier.isNaN()) {
            return
        }

        val newMultiplier = input.toDoubleOrNull()
        val isValid = newMultiplier != null && newMultiplier > 0
        val isCustom = !commonMultipliers.contains(newMultiplier)

        // Optimistic updates so the UI doesn't have to wait for the DB
        if (isValid) {
            _optimisticMultiplierState.update { currentState ->
                OptimisticMultiplier(
                    multiplier = newMultiplier,
                    customMultiplier = if (isCustom) newMultiplier else currentState?.customMultiplier,
                    customMultiplierInput = if (isCustom) input else currentState?.customMultiplierInput
                )
            }
        }

        // Cancel the previous job if it's still running.
        // This is relevant when the user enters a custom value, but immediately
        // taps a common chip. We want to save the custom, but cancel the update
        // to the multiplier. A second call will update the multiplier to the
        // common chip value.
        _updateMultiplierJob?.cancel()

        _updateMultiplierJob = viewModelScope.launch {
            // We want to handle custom even if the job is cancelled
            withContext(NonCancellable) {
                if (isCustom){
                    if (isValid) {
                        // Valid custom input
                        repository.updateRecipeCustomMultiplier(recipeId, newMultiplier)
                    }
                    else {
                        // Invalid input
                        if (input == "") {
                            // The custom was blanked out, but after that nothing will be selected.
                            // Reset the multiplier/custom to the default.
                            repository.resetMultipliers(recipeId)
                        } else {
                            // The custom was invalid. We nullify the optimistic state
                            // and trigger a call to the sync state. This has the effect
                            // of reverting to the previous value.
                            _optimisticMultiplierState.update { null }
                            _syncMultiplierTrigger.value = System.currentTimeMillis()
                        }
                    }
                }
            }

            // This will be cancelled if updateMultiplier is called a second time
            if (isValid) {
                repository.updateRecipeMultiplier(recipeId, newMultiplier)
            }
        }
    }
}

// Note: the modern standard is DI with Koin or Hilt to alleviate the need to
// manually pass in every dependency through here
class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(
                repository = repository,
                savedStateHandle = savedStateHandle
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// For dev
fun getSampleRecipe() : RecipeWithIngredients
{
    return RecipeWithIngredients(
        recipe = Recipe(
            name = "Sample Recipe ${System.currentTimeMillis() / 1000}",
            totalFlourAmount = 100.0,
            yield = "1 loaf",
            sortOrder = 1
        ),
        ingredients = listOf(
            Ingredient(
                recipeId = 0,
                name = "Bread Flour",
                sortOrder = 0,
                type = IngredientType.FLOUR,
                bakersPercent = 100.0
            ),
            Ingredient(
                recipeId = 0,
                name = "Water",
                sortOrder = 1,
                type = IngredientType.HYDRATION,
                bakersPercent = 70.0
            ),
            Ingredient(
                recipeId = 0,
                name = "Salt",
                sortOrder = 2,
                type = IngredientType.SALT,
                bakersPercent = 2.5
            ),
            Ingredient(
                recipeId = 0,
                name = "Starter",
                sortOrder = 3,
                type = IngredientType.STARTER,
                bakersPercent = 20.0
            )
        )
    )
}
