package jen.doughapp.domain

import jen.doughapp.data.IngredientType
import jen.doughapp.ui.recipe.IngredientDisplayModel
import kotlin.math.roundToInt

fun getTotalIngredientType(
    ingredients: List<IngredientDisplayModel>,
    type: IngredientType,
    starterHydration: Int? = null): Double
{
    //todo, since we only have a single starter hydration for now,
    // we're assuming only one starter per recipe...
    // is that an OK assumption?

    val starterContribution = if (starterHydration != null) {
        val totalParts = 100.0 + starterHydration
        val ratio = when (type) {
            IngredientType.FLOUR -> 100.0 / totalParts
            IngredientType.HYDRATION -> starterHydration / totalParts
            else -> 0.0
        }

        ingredients
            .filter { it.type == IngredientType.STARTER }
            .sumOf { it.amount * ratio }
    } else 0.0

    return ingredients
        .filter { it.type == type }
        .sumOf { it.amount } + starterContribution
}

fun getHydration(
    ingredients: List<IngredientDisplayModel>,
    starterHydration: Int? = 100
): Double {
    val totalFlour = getTotalIngredientType(ingredients, IngredientType.FLOUR, starterHydration)
    val totalLiquids = getTotalIngredientType(ingredients, IngredientType.HYDRATION, starterHydration)

    if (totalFlour > 0 && totalLiquids > 0) {
        return (100 * totalLiquids / totalFlour)
    }

    return 0.0
}
