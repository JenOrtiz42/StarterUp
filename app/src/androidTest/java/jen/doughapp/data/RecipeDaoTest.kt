package jen.doughapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RecipeDaoTest {
    private lateinit var recipeDao: RecipeDao
    private lateinit var db: RecipeDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RecipeDatabase::class.java
        ).build()
        recipeDao = db.recipeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeRecipeAndReadInList() = runBlocking {
        val recipe = Recipe(
            id = 1,
            name = "Test Sourdough",
            yield = "1 loaf",
            sortOrder = 0,
            totalFlourAmount = 500.0
        )
        val ingredients = listOf(
            Ingredient(recipeId = 1, name = "Flour", bakersPercent = 100.0, sortOrder = 0, type = IngredientType.FLOUR),
            Ingredient(recipeId = 1, name = "Water", bakersPercent = 70.0, sortOrder = 1, type = IngredientType.HYDRATION)
        )

        recipeDao.insertRecipe(recipe, ingredients)
        
        val allRecipes = recipeDao.getAllRecipes().first()
        assertEquals(1, allRecipes.size)
        assertEquals("Test Sourdough", allRecipes[0].recipe.name)
        assertEquals(2, allRecipes[0].ingredients.size)
    }

    @Test
    fun upsertRecipeWithIngredients_updatesExistingAndReplacesIngredients() = runBlocking {
        // 1. Initial Insert
        val recipeId = 1L
        val initialRecipe = Recipe(
            id = recipeId,
            name = "Initial Name",
            yield = "1 loaf",
            sortOrder = 0,
            totalFlourAmount = 500.0
        )
        val initialIngredients = listOf(
            Ingredient(recipeId = recipeId, name = "Old Flour", bakersPercent = 100.0, sortOrder = 0)
        )
        recipeDao.insertRecipe(initialRecipe, initialIngredients)

        // 2. Perform Upsert (Update)
        val updatedRecipe = initialRecipe.copy(name = "Updated Name")
        val newIngredients = listOf(
            Ingredient(recipeId = recipeId, name = "New Flour", bakersPercent = 100.0, sortOrder = 0),
            Ingredient(recipeId = recipeId, name = "New Salt", bakersPercent = 2.0, sortOrder = 1)
        )
        
        recipeDao.upsertRecipeWithIngredients(updatedRecipe, newIngredients)

        // 3. Verify
        val result = recipeDao.getRecipeById(recipeId).first()
        assertEquals("Updated Name", result.recipe.name)
        assertEquals(2, result.ingredients.size)
        assertTrue(result.ingredients.any { it.name == "New Salt" })
        assertTrue(result.ingredients.none { it.name == "Old Flour" })
    }
}
