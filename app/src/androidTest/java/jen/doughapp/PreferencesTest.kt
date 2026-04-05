package jen.doughapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PreferencesTest {
    private lateinit var testContext: Context

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var recipePreferences: RecipePreferences
    private lateinit var screenPreferences: ScreenPreferences

    @Before
    fun setup() {
        testContext = ApplicationProvider.getApplicationContext()

        // Create a unique filename for every test method to avoid
        // "Multiple DataStores active" errors
        val uniqueName = "test_prefs_${UUID.randomUUID()}"

        // Create a fresh DataStore instance for this specific test
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { testContext.preferencesDataStoreFile(uniqueName) }
        )

        recipePreferences = RecipePreferences(testDataStore)
        screenPreferences = ScreenPreferences(testDataStore)
    }

    @After
    fun cleanup() {
        runBlocking {
            // Optional: Delete the test file after each test to stay clean
            testDataStore.edit { it.clear() }
        }
    }

    @Test
    fun recipePreferences_savesAndRetrievesMultiplier() {
        runBlocking {
            val recipeId = 999L
            val multiplier = 2.5

            recipePreferences.saveMultiplier(recipeId, multiplier)
            val savedValue = recipePreferences.getMultiplier(recipeId).first()

            assertEquals(multiplier, savedValue, 0.001)
        }
    }

    @Test
    fun screenPreferences_savesAndRetrievesLevainTarget() {
        runBlocking {
            val testTarget = "450"

            screenPreferences.saveLevainTarget(testTarget)
            val savedValue = screenPreferences.getLevainTarget().first()

            assertEquals(testTarget, savedValue)
        }
    }
    @Test
    fun preferences_returnDefaultValuesWhenEmpty() {
        runBlocking {
            // Multiplier defaults to 1.0
            val multiplier = recipePreferences.getMultiplier(-12345L).first()
            assertEquals(1.0, multiplier, 0.0)
        }
    }
}
