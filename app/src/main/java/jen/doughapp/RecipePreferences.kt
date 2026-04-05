package jen.doughapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Only ONE delegate per file, or better yet, one for the whole app
private val Context.dataStore by preferencesDataStore(name = "app_prefs")

fun provideRecipePreferences(context: Context): RecipePreferences {
    return RecipePreferences(context.dataStore)
}

fun provideScreenPreferences(context: Context): ScreenPreferences {
    return ScreenPreferences(context.dataStore)
}

class RecipePreferences(private val dataStore: DataStore<Preferences>) {
    // Use a unique key for each recipe's multiplier
    private fun multiplierKey(recipeId: Long) = doublePreferencesKey("multiplier_$recipeId")

    fun getMultiplier(recipeId: Long): Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[multiplierKey(recipeId)] ?: 1.0
        }

    suspend fun saveMultiplier(recipeId: Long, multiplier: Double) {
        dataStore.edit { preferences ->
            preferences[multiplierKey(recipeId)] = multiplier
        }
    }
}

class ScreenPreferences(private val dataStore: DataStore<Preferences>) {
    private val levainTargetKey = stringPreferencesKey("levain_target_amount")

    fun getLevainTarget(): Flow<String> = dataStore.data
        .map { prefs -> prefs[levainTargetKey] ?: "200" } // Default to 200

    suspend fun saveLevainTarget(amount: String) {
        dataStore.edit { prefs ->
            prefs[levainTargetKey] = amount
        }
    }
}