package jen.doughapp.data

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class RecipeImportModel(
    val name: String,
    val yield: String,
    val totalFlourAmount: Double,
    val sortOrder: Int,
    val ingredients: List<IngredientImportModel>
)

data class IngredientImportModel(
    val name: String,
    val bakersPercent: Double,
    val sortOrder: Int,
    val type: IngredientType
)

class RecipeDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope,
    private val dbProvider: () -> RecipeDatabase
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        scope.launch(Dispatchers.IO) {
            populateInitialData()
        }
    }

    // Seed an initial recipe
    private suspend fun populateInitialData() {
        val database = dbProvider()
        val recipeDao = database.recipeDao()
        val ingredientDao = database.ingredientDao()

        try {
            val jsonString = context.assets.open("recipes.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<RecipeImportModel>>() {}.type
            val recipesToImport: List<RecipeImportModel> = Gson().fromJson(jsonString, listType)

            recipesToImport.forEach { import ->
                val recipeId = recipeDao.upsertRecipe(
                    Recipe(
                        name = import.name,
                        yield = import.yield,
                        totalFlourAmount = import.totalFlourAmount,
                        sortOrder = import.sortOrder
                    )
                )

                import.ingredients.forEach { ing ->
                    ingredientDao.upsertIngredient(
                        Ingredient(
                            recipeId = recipeId,
                            name = ing.name,
                            bakersPercent = ing.bakersPercent,
                            sortOrder = ing.sortOrder,
                            type = ing.type
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("RecipeDatabase", "Error importing seed data", e)
        }
    }
}
