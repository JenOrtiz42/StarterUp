package jen.doughapp.data

import jen.doughapp.RecipePreferences
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val preferences: RecipePreferences
) {
    val allRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getAllRecipes()

    fun getRecipeById(recipeId: Long): Flow<RecipeWithIngredients> {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun upsertRecipeWithIngredients(recipe: Recipe, ingredients: List<Ingredient>): Long {
        return recipeDao.upsertRecipeWithIngredients(recipe, ingredients)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    fun getIngredientById(ingredientId: Long): Flow<Ingredient> {
        return ingredientDao.getIngredientById(ingredientId)
    }

    // Note: current not used, but if we switch over,
    // make sure the ingredient has a recipeId and it's valid
    suspend fun upsertIngredient(ingredient: Ingredient) {
        ingredientDao.upsertIngredient(ingredient)
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    fun getSavedMultiplier(recipeId: Long): Flow<Double> {
        return preferences.getMultiplier(recipeId)
    }

    fun getSavedCustomMultiplier(recipeId: Long): Flow<Double?> {
        return preferences.getCustomMultiplier(recipeId)
    }

    suspend fun updateRecipeMultiplier(recipeId: Long, newMultiplier: Double) {
        preferences.saveMultiplier(recipeId, newMultiplier)
    }

    suspend fun updateRecipeCustomMultiplier(recipeId: Long, newMultiplier: Double?) {
        preferences.saveCustomMultiplier(recipeId, newMultiplier)
    }

    suspend fun resetMultipliers(recipeId: Long) {
        preferences.resetMultipliers(recipeId)
    }
}