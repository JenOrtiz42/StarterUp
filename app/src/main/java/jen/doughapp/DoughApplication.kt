package jen.doughapp

import android.app.Application
import jen.doughapp.data.RecipeDatabase
import jen.doughapp.data.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DoughApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database by lazy { RecipeDatabase.getDatabase(this, applicationScope) }
    val recipePreferences by lazy { provideRecipePreferences(this) }
    val screenPreferences by lazy { provideScreenPreferences(this) }

    val repository by lazy {
        RecipeRepository(
            database.recipeDao(),
            database.ingredientDao(),
            recipePreferences
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Force database initialization on startup
        applicationScope.launch(Dispatchers.IO) {
            database.openHelper.writableDatabase
            // Accessing the database once triggers the Room callbacks
        }
    }
}