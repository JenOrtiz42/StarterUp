package jen.doughapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jen.doughapp.RecipePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.edit
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RecipeRepositoryTest {
    private lateinit var repository: RecipeRepository
    private lateinit var db: RecipeDatabase
    private lateinit var preferences: RecipePreferences
    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Use in-memory database for tests
        db = Room.inMemoryDatabaseBuilder(
            context, RecipeDatabase::class.java
        ).build()

        // Create a unique filename for every test method to avoid
        // "Multiple DataStores active" errors
        val uniqueName = "test_prefs_${UUID.randomUUID()}"

        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(uniqueName) }
        )

        preferences = RecipePreferences(testDataStore)

        repository = RecipeRepository(
            recipeDao = db.recipeDao(),
            ingredientDao = db.ingredientDao(),
            preferences = preferences
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // Clean up the preferences so the next test run is fresh
        runBlocking { testDataStore.edit { it.clear() } }
        db.close()
    }

    @Test
    fun upsertAndGetRecipe_returnsCorrectDataFromFlow() {
        runBlocking {
            // Arrange
            val recipe = Recipe(
                id = 1,
                name = "Repository Test",
                yield = "1 loaf",
                sortOrder = 0,
                totalFlourAmount = 1000.0
            )
            val ingredients = listOf(
                Ingredient(recipeId = 1, name = "Bread Flour", bakersPercent = 100.0, sortOrder = 0)
            )

            // Act
            repository.upsertRecipeWithIngredients(recipe, ingredients)

            // Assert
            val result = repository.getRecipeById(1).first()
            assertEquals("Repository Test", result.recipe.name)
            assertEquals(1, result.ingredients.size)
            assertEquals("Bread Flour", result.ingredients[0].name)
        }
    }

    @Test
    fun deleteRecipe_removesFromDatabase() {
        runBlocking {
            // Arrange
            val recipe = Recipe(
                id = 2,
                name = "To Delete",
                yield = "1",
                sortOrder = 0,
                totalFlourAmount = 500.0
            )
            repository.upsertRecipeWithIngredients(recipe, emptyList())

            // Verify it exists first
            val initialList = repository.allRecipes.first()
            assertEquals(1, initialList.size)

            // Act
            repository.deleteRecipe(recipe)

            // Assert
            val finalList = repository.allRecipes.first()
            assertEquals(0, finalList.size)
        }
    }

    @Test
    fun multiplierPreferences_areSavedAndRetrieved() {
        runBlocking {
            val recipeId = 55L

            // Act: Save a specific multiplier
            repository.updateRecipeMultiplier(recipeId, 2.5)

            // Assert: Retrieve it
            val savedMultiplier = repository.getSavedMultiplier(recipeId).first()
            assertEquals(2.5, savedMultiplier, 0.001)
        }
    }
}
