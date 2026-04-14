package jen.doughapp.ui.recipe

import jen.doughapp.data.IngredientType

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

data class RecipeUiState(
    // Common
    val isLoading: Boolean = true,

    // Data for Editing
    val recipe: RecipeDraft = RecipeDraft(),
    val initialRecipe: RecipeDraft = RecipeDraft(),
    val isEntryValid: Boolean = false,
    val isSaving: Boolean = false,

    // Data for Viewing
    val displayIngredients: List<IngredientDisplayModel> = emptyList(),
    val multiplier: Double = 1.0,
    val hydration: Double = 0.0,
    val totalWeight: Double = 0.0
) {
    val isChanged: Boolean get() = recipe != initialRecipe
}