package jen.doughapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Transaction
    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    fun getIngredientById(ingredientId: Long): Flow<Ingredient>

    @Upsert
    suspend fun upsertIngredient(ingredient: Ingredient) : Long

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    suspend fun deleteIngredientById(ingredientId: Long)
}