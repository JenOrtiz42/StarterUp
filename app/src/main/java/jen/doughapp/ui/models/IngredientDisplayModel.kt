package jen.doughapp.ui.models

import jen.doughapp.data.IngredientType

data class IngredientDisplayModel(
    val name: String,
    val amount: Double,
    val unit: String,
    val bakersPercent: Double,
    val sortOrder: Int,
    val type: IngredientType? = null
)
