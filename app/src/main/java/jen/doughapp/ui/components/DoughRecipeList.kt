package jen.doughapp.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.data.Recipe
import jen.doughapp.data.RecipeWithIngredients
import jen.doughapp.theme.DoughAppTheme

//todo, recipe search/filter/sort

@Composable
fun DoughRecipeList(
    modifier: Modifier = Modifier,
    recipes: List<RecipeWithIngredients>,
    onRecipeClick: (Long) -> Unit = {},
    onEditRecipe: (Long) -> Unit = {},
    onDeleteRecipe: (Recipe) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DoughSectionHeader(text = "Recipes")

        if (recipes.isEmpty()) {
            Text(
                text = "No recipes found. Add one to get started!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            recipes.forEach { recipeWrapper ->
                key(recipeWrapper.recipe.id) {
                    RecipeItem(
                        modifier = Modifier.padding(bottom = 8.dp),
                        recipeWrapper = recipeWrapper,
                        onClick = { onRecipeClick(recipeWrapper.recipe.id) },
                        onEditClick = { onEditRecipe(recipeWrapper.recipe.id) },
                        onDeleteClick = { onDeleteRecipe(recipeWrapper.recipe) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeItem(
    modifier: Modifier = Modifier,
    recipeWrapper: RecipeWithIngredients,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateString = formatTimestamp(recipeWrapper.recipe.lastUpdatedTimestamp)

    if (showDeleteConfirm) {
        DoughDeleteAlertDialog(
            itemName = recipeWrapper.recipe.name,
            itemTypeName = "Recipe",
            onDeleteItem = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }

    DoughCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipeWrapper.recipe.name,
                    style = MaterialTheme.typography.labelLarge
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        fontWeight = FontWeight.Bold
//                    )
                    //fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    //fontSize = 12.sp,
                    //color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        //tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        //tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

@Preview(showBackground = true, name = "Recipe Item Light Mode")
@Composable
fun RecipeItemPreview() {
    DoughAppTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            RecipeItem(
                recipeWrapper = createDummyRecipe(id = 0, name ="Classic Sourdough"),
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Recipe Item List")
@Composable
fun RecipeItemListPreview() {
    DoughAppTheme(dynamicColor = false) {
        val dummyRecipes = listOf(
            createDummyRecipe(
                id = 0,
                name = "Classic Sourdough",
                timestamp = System.currentTimeMillis()),
            createDummyRecipe(
                id = 1,
                name = "Country Loaf",
                timestamp = System.currentTimeMillis() - 86400000), // Yesterday
            createDummyRecipe(
                id = 2,
                name = "Bagel Dough",
                timestamp = System.currentTimeMillis() - 259200000), // 3 days ago
            createDummyRecipe(
                id = 3,
                name = "Pizza Base",
                timestamp = System.currentTimeMillis() - 604800000) // 1 week ago
        )

        DoughRecipeList(
            modifier = Modifier.padding(16.dp),
            recipes = dummyRecipes,
            onRecipeClick = {},
            onEditRecipe = {},
            onDeleteRecipe = {}
        )
    }
}

/**
 * Helper to create dummy data for previews
 */
private fun createDummyRecipe(id: Long, name: String, timestamp: Long = System.currentTimeMillis()): RecipeWithIngredients {
    return RecipeWithIngredients(
        recipe = Recipe(
            id = id,
            name = name,
            yield = "1 loaf",
            totalFlourAmount = 100.0,
            sortOrder = 1,
            createdTimestamp = timestamp,
            lastUpdatedTimestamp = timestamp
        ),
        ingredients = emptyList()
    )
}