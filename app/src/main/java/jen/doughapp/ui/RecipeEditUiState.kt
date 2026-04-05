package jen.doughapp.ui

import jen.doughapp.data.IngredientType

/*
1. The "Draft" (The Input Layer)
The Draft represents the "Work in Progress." It is messy.
•
Purpose: To hold exactly what the user types before it is validated.
•
Data Types: It uses String for almost everything (even numbers).
•
Why? If a user types 1., a Double can't store that trailing decimal. If they leave a field blank, an Int would force a 0. The Draft keeps the UI "responsive" to typing without crashing or changing the user's input.
2. The "UiState" (The Screen Layer)
The UiState represents the "Whole Picture." It is a snapshot of the screen at this exact millisecond.
•
Purpose: To tell the Composable what to show (Is it loading? Is the save button enabled? Is there a validation error?).
•
Contents: It contains the Draft, but also includes metadata about the screen


This is where the "Separation" happens. Your ViewModel manages the UiState, and the UI updates the Draft inside it.

1.
Initialization: You load a Recipe from the database. The ViewModel converts it into a RecipeDraft (converting Doubles to Strings) and puts it into the UiState.
2.
User Types: User types "High Gluten Flour". The ViewModel updates the RecipeDraft inside the UiState.
3.
Validation: Every time the RecipeDraft changes, the ViewModel re-calculates isEntryValid.
4.
Saving: When the user clicks Save, the ViewModel takes the RecipeDraft, converts the Strings back into Doubles, and sends the "Real" Recipe entity to the Repository.
*/

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

data class RecipeEditUiState(
    val recipe: RecipeDraft = RecipeDraft(),
    val initialRecipe: RecipeDraft = RecipeDraft(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEntryValid: Boolean = false
) {
    val isChanged: Boolean get() = recipe != initialRecipe
}