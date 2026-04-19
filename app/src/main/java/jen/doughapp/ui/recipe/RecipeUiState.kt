package jen.doughapp.ui.recipe

import jen.doughapp.data.IngredientType
import jen.doughapp.data.RecipeWithIngredients

data class RecipeUiState(
    // Common
    val isLoading: Boolean = true,
    val refreshTrigger: Long = 0L,

    // Data for Editing
    val recipe: RecipeDraft = RecipeDraft(),
    val initialRecipe: RecipeDraft = RecipeDraft(),
    val isEntryValid: Boolean = false,
    val isSaving: Boolean = false,

    // Data for Viewing
    val displayIngredients: List<IngredientDisplayModel> = emptyList(),
    val multiplier: Double = Double.NaN,
    val customMultiplierInput: String = "",
    val hydration: Double = 0.0,
    val totalWeight: Double = 0.0
) {
    val isChanged: Boolean get() = recipe != initialRecipe
}

data class RecipeDraft(
    val id: Long = 0,
    val name: String = "",
    val flourWeight: String = "",
    val yield: String = "",
    val createdTimestamp: Long? = null,
    val ingredients: List<IngredientDraft> = emptyList()
) {
    fun isValid(): Boolean {
        val weight = flourWeight.toDoubleOrNull()
        return name.isNotBlank() && weight != null && weight > 0
    }
}

data class IngredientDraft(
    val id: Long = 0,
    val name: String = "",
    val percentage: String = "",
    val type : IngredientType? = null,
    val isEditing: Boolean = false
) {
    // An ingredient is "blank" if the user hasn't typed anything in it at all
    fun isBlank(): Boolean = name.isBlank() && percentage.isBlank() && type == null

    fun isValid(): Boolean {
        val percent = percentage.toDoubleOrNull()
        return name.isNotBlank() && percent != null && percent > 0
    }
}

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
