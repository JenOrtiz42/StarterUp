package jen.doughapp

import jen.doughapp.data.Ingredient
import jen.doughapp.data.IngredientType
import jen.doughapp.data.getIngredientTypeName
import jen.doughapp.data.toDisplayModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataMappingTest {

    @Test
    fun `getIngredientTypeName returns correct string for all types`() {
        assertEquals("Flour", getIngredientTypeName(IngredientType.FLOUR))
        assertEquals("Hydration", getIngredientTypeName(IngredientType.HYDRATION))
        assertEquals("Salt", getIngredientTypeName(IngredientType.SALT))
        assertEquals("Yeast", getIngredientTypeName(IngredientType.YEAST))
        assertEquals("Starter", getIngredientTypeName(IngredientType.STARTER))
    }

    @Test
    fun `getIngredientTypeName returns empty string for null type`() {
        assertEquals("", getIngredientTypeName(null))
    }

    @Test
    fun `Ingredient toDisplayModel maps all fields correctly`() {
        val ingredient = Ingredient(
            id = 10,
            recipeId = 1,
            name = "Bread Flour",
            bakersPercent = 100.0,
            sortOrder = 5,
            type = IngredientType.FLOUR
        )
        
        val totalFlour = 500.0
        val displayModel = ingredient.toDisplayModel(totalFlour)
        
        assertEquals("Bread Flour", displayModel.name)
        assertEquals(500.0, displayModel.amount, 0.001)
        assertEquals("g", displayModel.unit)
        assertEquals(100.0, displayModel.bakersPercent, 0.001)
        assertEquals(5, displayModel.sortOrder)
        assertEquals(IngredientType.FLOUR, displayModel.type)
    }

    @Test
    fun `Ingredient toDisplayModel handles null type correctly`() {
        val ingredient = Ingredient(
            recipeId = 1,
            name = "Secret Ingredient",
            bakersPercent = 1.0,
            sortOrder = 0,
            type = null
        )
        
        val displayModel = ingredient.toDisplayModel(100.0)
        assertNull(displayModel.type)
    }

    @Test
    fun `Ingredient toDisplayModel calculates small percentages accurately`() {
        val ingredient = Ingredient(
            recipeId = 1,
            name = "Instant Yeast",
            bakersPercent = 0.5,
            sortOrder = 0
        )
        
        // 0.5% of 500g is 2.5g
        val displayModel = ingredient.toDisplayModel(500.0)
        assertEquals(2.5, displayModel.amount, 0.001)
    }
}
