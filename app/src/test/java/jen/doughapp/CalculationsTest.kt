package jen.doughapp.domain

import jen.doughapp.data.IngredientType
import jen.doughapp.ui.recipe.IngredientDisplayModel
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationsTest {

    @Test
    fun `getTotalIngredientType calculates base flour correctly without starter`() {
        val ingredients = listOf(
            IngredientDisplayModel(name = "Bread Flour", amount = 500.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.FLOUR),
            IngredientDisplayModel(name = "Water", amount = 350.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.HYDRATION)
        )

        val totalFlour = getTotalIngredientType(ingredients, IngredientType.FLOUR, null)
        assertEquals(500.0, totalFlour, 0.1)
    }

    @Test
    fun `getTotalIngredientType includes flour from starter at 100 percent hydration`() {
        // 100g of 100% hydration starter = 50g flour + 50g water
        val ingredients = listOf(
            IngredientDisplayModel(name = "Bread Flour", amount = 500.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.FLOUR),
            IngredientDisplayModel(name = "Starter", amount = 100.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.STARTER)
        )

        val totalFlour = getTotalIngredientType(ingredients, IngredientType.FLOUR, 100)

        // Expected: 500 (base) + 50 (from starter) = 550
        assertEquals(550.0, totalFlour, 0.1)
    }

    @Test
    fun `getTotalIngredientType includes water from starter at 80 percent hydration`() {
        // 180g of 80% hydration starter (100g flour + 80g water)
        val ingredients = listOf(
            IngredientDisplayModel(name = "Starter", amount = 180.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.STARTER)
        )

        val totalWater = getTotalIngredientType(ingredients, IngredientType.HYDRATION, 80)

        assertEquals(80.0, totalWater, 0.1)
    }

    @Test
    fun `getHydration calculates basic ratio correctly`() {
        val ingredients = listOf(
            IngredientDisplayModel(name = "Flour", amount = 500.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.FLOUR),
            IngredientDisplayModel(name = "Water", amount = 350.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.HYDRATION)
        )

        val hydration = getHydration(ingredients, null)

        // 350 / 500 = 0.7 -> 70%
        assertEquals(70, hydration)
    }

    @Test
    fun `getHydration handles starter contribution correctly`() {
        // Recipe: 450g Flour, 300g Water, 100g Starter (at 100% hydration)
        // Total Flour: 450 + 50 = 500
        // Total Water: 300 + 50 = 350
        val ingredients = listOf(
            IngredientDisplayModel(name = "Flour", amount = 450.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.FLOUR),
            IngredientDisplayModel( name = "Water", amount = 300.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.HYDRATION),
            IngredientDisplayModel(name = "Starter", amount = 100.0, unit = "", bakersPercent = 0.0, sortOrder = 0, type = IngredientType.STARTER)
        )

        val hydration = getHydration(ingredients, 100)

        // 350 / 500 = 70%
        assertEquals(70, hydration)
    }

    @Test
    fun `getHydration returns zero when ingredients are empty`() {
        val hydration = getHydration(emptyList(), null)
        assertEquals(0, hydration)
    }
}