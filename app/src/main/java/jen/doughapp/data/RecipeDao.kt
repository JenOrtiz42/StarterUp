package jen.doughapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeById(recipeId: Long): Flow<RecipeWithIngredients>

    @Upsert
    suspend fun upsertRecipe(recipe: Recipe) : Long

    @Insert
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Transaction
    suspend fun insertRecipe(recipe: Recipe, ingredients: List<Ingredient>) {
        val recipeId = upsertRecipe(recipe)
        val ingredientsWithId = ingredients.map { it.copy(recipeId = recipeId) }
        insertIngredients(ingredientsWithId)
    }

    @Transaction
    suspend fun upsertRecipeWithIngredients(recipe: Recipe, ingredients: List<Ingredient>): Long {
        //todo: note, for now, nuking the ingredients and inserting new ones works well
        // and prevents the need for figuring out which ingredients were added, removed,
        // etc. But we may need to preserve existing records in the future if we implement
        // flour/starter libraries and want to connect ingrdients to them.

        val recipeId = upsertRecipe(recipe).let {
            if (it == -1L) recipe.id else it
        }

        // Clear out old ingredients of old recipe (recipe.id) before adding the updated ones
        deleteIngredientsByRecipeId(recipe.id)

        val ingredientsWithId = ingredients.map { it.copy(recipeId = recipeId, id = 0) }
        insertIngredients(ingredientsWithId)

        // Return the ID returned by upsert
        return recipeId
    }

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)
}
