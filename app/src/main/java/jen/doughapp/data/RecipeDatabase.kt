package jen.doughapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope

class Converters {
    @TypeConverter
    fun fromIngredientType(value: IngredientType?): String? {
        return value?.name
    }@TypeConverter
    fun toIngredientType(value: String?): IngredientType? {
        return value?.let { enumValueOf<IngredientType>(it) }
    }
}

@Database(entities = [Recipe::class, Ingredient::class], version = 9)
@TypeConverters(Converters::class)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                .addCallback(RecipeDatabaseCallback(
                    context = context,
                    scope = scope
                ) {
                    getInstance(context, scope)
                })
                // todo: learn more about migrations
                // fallbackToDestructiveMigration is a dev thing I think
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getInstance(context: Context, scope: CoroutineScope): RecipeDatabase {
            return INSTANCE ?: getDatabase(context, scope)
        }
    }
}