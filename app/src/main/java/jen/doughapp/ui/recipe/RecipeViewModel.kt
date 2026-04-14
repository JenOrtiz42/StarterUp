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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{
    private val args = try {
        //Try to get args from RecipeEdit
        savedStateHandle.toRoute<RecipeEdit>()
    } catch (e: Exception) {
        // Fallback for when we navigated via RecipeDetail instead
        val detailArgs = savedStateHandle.toRoute<RecipeDetail>()
        RecipeEdit(detailArgs.recipeId)
    }

    val recipeId = args.recipeId

    //todo, review these flows; there are some issues now, and also looking more
    // complicated than it probably needs to

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // A local "override" for the scaling multiplier to provide instant UI feedback
    // (This fixes a visual problem that would occur when custom multiplier
    // gets updated before the multiplier, causing displays based on these
    // values to ping-pong)
    private val _multiplierOverride = MutableStateFlow<Double?>(null)

    // The recipe scaling multiplier
    @OptIn(ExperimentalCoroutinesApi::class)
    val multiplier: StateFlow<Double> =
        (if (recipeId == -1L) flowOf(1.0) else repository.getSavedMultiplier(recipeId))
        .combine(_multiplierOverride) { dbValue, override ->
            // If the database has caught up to our override,
            // we can safely clear the override
            if (override != null && dbValue == override) {
                // We use a check here to avoid unnecessary re-compositions
                _multiplierOverride.value = null
            }

            // Still return the override if it exists to keep the UI stable
            override ?: dbValue
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Double.NaN
        )

    // The custom recipe scaling multiplier
    private val _customMultiplierInput = MutableStateFlow("")
    val customMultiplierInput: StateFlow<String> = _customMultiplierInput.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val customMultiplier: StateFlow<Double?> =
        (if (recipeId == -1L) flowOf(null) else repository.getSavedCustomMultiplier(recipeId))
        .onEach { loadedValue ->
            // Sync the raw input string whenever the database value changes.
            // This handles initial load and external resets
            _customMultiplierInput.value = loadedValue?.formatMultiplier() ?: ""
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recipes: StateFlow<List<RecipeWithIngredients>> = repository.allRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // We observe the ID flow. If it changes, we fetch the data and update UI state.
        observeRecipeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecipeData() {
        viewModelScope.launch {
            val dataFlow = if (recipeId == -1L) {
                // Return a flow that just emits a "New Recipe" state
                flowOf(Triple(recipeId, null, 1.0))
            } else {
                // Return the combined database + multiplier flow
                combine(
                    repository.getRecipeById(recipeId),
                    multiplier
                ) { recipeData, currentMultiplier ->
                    Triple(recipeId, recipeData, currentMultiplier)
                }
            }

            dataFlow.collect { (id, recipeData, currentMultiplier) ->
                    // Now we perform the update logic once per emission
                    if (recipeData == null) {
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
                                recipe = recipeDraft,
                                // Note: Only set initialRecipe once when the data first loads
                                initialRecipe = if (currentState.initialRecipe.id != id) {
                                    recipeDraft
                                } else {
                                    currentState.initialRecipe
                                },
                                displayIngredients = displayIngredients,
                                multiplier = currentMultiplier,
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
        _customMultiplierInput.value = input
    }

    fun updateMultiplier(input: String, commonMultipliers: List<Double>) {
        // If we are still in the NaN state, the database hasn't reported back yet.
        if (multiplier.value.isNaN()) return

        val newMultiplier = input.toDoubleOrNull()
        val isValid = newMultiplier != null && newMultiplier > 0
        val isCommon = commonMultipliers.contains(newMultiplier)

        val id = recipeId
        if (id == -1L) return

        // Optimistic updates
        if (isValid) {
            _multiplierOverride.value = newMultiplier

            if (!isCommon) {
                _customMultiplierInput.value = input
            }
        }

        viewModelScope.launch {
            if (isValid) {
                repository.updateRecipeMultiplier(id, newMultiplier)

                if (!isCommon) {
                    repository.updateRecipeCustomMultiplier(id, newMultiplier)
                    // Note: don't need to update _customMultiplierInput.value here
                    // because it's handled by the flow
                }
                // Else, leave the custom multiplier alone!
            }
            else {
                // The new multiplier is not valid (and we assume it must be a custom-entered one),
                // so blank out the custom.
                repository.updateRecipeCustomMultiplier(id, null)

                // Note: DO need to update _customMultiplierInput.value here, because
                // if it's already null, it's not a change and won't trigger the update
                _customMultiplierInput.value = ""
            }
        }
    }
}

// Helper to keep the ViewModel clean
fun RecipeWithIngredients.toDraft(): RecipeDraft {
    return RecipeDraft(
        id = recipe.id,
        name = recipe.name,
        flourWeight = recipe.totalFlourAmount.toString(),
        yield = recipe.yield,
        createdTimestamp = recipe.createdTimestamp,
        ingredients = ingredients.map { ing ->
            IngredientDraft(
                id = ing.id,
                name = ing.name,
                percentage = ing.bakersPercent.toString(),
                type = ing.type
            )
        }
    )
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
