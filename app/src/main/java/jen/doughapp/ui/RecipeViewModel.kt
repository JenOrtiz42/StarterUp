package jen.doughapp.ui

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import jen.doughapp.data.Ingredient
import jen.doughapp.data.IngredientType
import jen.doughapp.data.Recipe
import jen.doughapp.data.RecipeRepository
import jen.doughapp.data.RecipeWithIngredients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest

class RecipeViewModel(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{
    // Keep this for easy access to the initial ID (must match name in NavGraph route)
    val recipeId: Long = savedStateHandle["recipeId"] ?: -1L
    private val _currentRecipeId = MutableStateFlow<Long?>(recipeId)

    private val _uiState = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    // Whenever _currentRecipeId.value changes, flatMapLatest "switches"
    // to a new database flow automatically.
    val multiplier: StateFlow<Double> = _currentRecipeId
        .flatMapLatest { id ->
            if (id == null || id == -1L) {
                kotlinx.coroutines.flow.flowOf(1.0)
            } else {
                repository.getSavedMultiplier(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Double.NaN
        )

    val customMultiplier: StateFlow<Double?> = _currentRecipeId
        .flatMapLatest { id ->
            if (id == null || id == -1L) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                repository.getSavedCustomMultiplier(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recipeWithIngredients: StateFlow<RecipeWithIngredients?> = _currentRecipeId
        .flatMapLatest { id ->
            if (id == null || id == -1L) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                repository.getRecipeById(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recipes = repository.allRecipes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // We observe the ID flow. If it changes, we fetch the data and update UI state.
        viewModelScope.launch {
            _currentRecipeId.collect { id ->
                if (id == null || id == -1L) {
                    val newDraft = RecipeDraft()
                    _uiState.update {
                        it.copy(recipe = newDraft, initialRecipe = newDraft, isLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = true) }
                    // Fetch the data once to populate the "Edit" fields
                    val data = repository.getRecipeById(id).firstOrNull()
                    data?.let { recipeWithIngredients ->
                        val draft = recipeWithIngredients.toDraft() // Extension function for cleanliness
                        _uiState.update {
                            it.copy(recipe = draft, initialRecipe = draft, isLoading = false)
                        }
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

    //todo do we need?
    fun getRecipe(id: Long) = repository.getRecipeById(id)

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    fun addSampleRecipe() {
        viewModelScope.launch {
            val sample = getSampleRecipe()
            repository.upsertRecipeWithIngredients(sample.recipe, sample.ingredients)
        }
    }

    fun updateMultiplier(input: String, commonMultipliers: List<Double>) {
        // If we are still in the NaN state, the database hasn't reported back yet.
        if (multiplier.value.isNaN()) return

        val newMultiplier = input.toDoubleOrNull()
        val isValid = newMultiplier != null && newMultiplier > 0
        val isCommon = commonMultipliers.contains(newMultiplier)

        // Use the reactive ID to ensure we are targeting the same record the UI is watching
        val id = _currentRecipeId.value ?: return
        if (id == -1L) return

        viewModelScope.launch {
            if (isValid) {
                repository.updateRecipeMultiplier(id, newMultiplier)

                if (!isCommon) {
                    repository.updateRecipeCustomMultiplier(id, newMultiplier)
                }
            }
            else {
                // The new multiplier is not valid (and we assume it must be a custom-entered one),
                // so blank out the custom.
                repository.updateRecipeCustomMultiplier(id, null)
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
