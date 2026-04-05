package jen.doughapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import jen.doughapp.ui.models.IngredientDisplayModel

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val bakersPercent: Double,
    val sortOrder: Int,
    val type: IngredientType? = null
)

fun Ingredient.toDisplayModel(totalFlourAmount: Double): IngredientDisplayModel {
    return IngredientDisplayModel(
        name = this.name,
        amount = (this.bakersPercent * totalFlourAmount) / 100.0,
        unit = "g",
        bakersPercent = this.bakersPercent,
        sortOrder = this.sortOrder,
        type = this.type
    )
}
