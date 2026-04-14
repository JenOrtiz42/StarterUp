package jen.doughapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Home

@Serializable data class RecipeDetail(
    val recipeId: Long
)

@Serializable data class RecipeEdit(
    val recipeId: Long = -1L
)

@Serializable data class LevainPlanner(
    val overrideAmount: String? = null
)

@Serializable object Timers