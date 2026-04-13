package jen.doughapp

import jen.doughapp.data.Ingredient
import jen.doughapp.data.IngredientType
import jen.doughapp.data.toDisplayModel
import jen.doughapp.domain.StarterRatio
import jen.doughapp.ui.recipe.IngredientDisplayModel
import jen.doughapp.ui.screens.getAmount
import jen.doughapp.ui.screens.getRatioSum
import jen.doughapp.ui.screens.getTargetAmountFromStarterAmount
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationLogicTest {

    @Test
    fun `getRatioSum correctly sums 1-2-2 ratio`() {
        val ratio = StarterRatio(1, 2, 2)
        assertEquals(5, getRatioSum(ratio))
    }

    @Test
    fun `getAmount calculates correct flour for 1-2-2 ratio with 500g target`() {
        // Ratio 1:2:2 means 5 total parts. 
        // 500g / 5 = 100g per portion. 
        // Flour is 2 portions = 200g.
        val amount = getAmount(targetTotal = "500", ratioSum = 5, portion = 2)
        assertEquals(200, amount)
    }

    @Test
    fun `getAmount handles invalid target amount string gracefully`() {
        val amount = getAmount(targetTotal = "invalid", ratioSum = 5, portion = 2)
        assertEquals(0, amount)
    }

    @Test
    fun `getAmount handles empty target amount string gracefully`() {
        val amount = getAmount(targetTotal = "", ratioSum = 5, portion = 2)
        assertEquals(0, amount)
    }

    @Test
    fun `getTargetAmountFromStarterAmount calculates correct total for 1-2-2 ratio`() {
        // 100g starter with a 1:2:2 ratio (sum 5) should result in 500g total
        val total = getTargetAmountFromStarterAmount(starterTotal = 100, ratioSum = 5)
        assertEquals(500, total)
    }

    @Test
    fun `toDisplayModel calculates correct weight based on Bakers Percent`() {
        val ingredient = Ingredient(
            recipeId = 1,
            name = "Water",
            bakersPercent = 70.0,
            sortOrder = 1,
            type = IngredientType.HYDRATION
        )
        
        // 70% of 500g flour should be 350g
        val displayModel: IngredientDisplayModel = ingredient.toDisplayModel(totalFlourAmount = 500.0)
        
        assertEquals(350.0, displayModel.amount, 0.001)
        assertEquals("Water", displayModel.name)
        assertEquals(70.0, displayModel.bakersPercent, 0.001)
    }
}
