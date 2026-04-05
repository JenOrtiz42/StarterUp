package jen.doughapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import jen.doughapp.data.IngredientType
import jen.doughapp.data.getIngredientTypeName
import kotlin.collections.forEach
import kotlin.collections.plus


@Composable
fun DoughIngredientTypePicker(
    currentType: IngredientType?,
    onSelectType: (type: IngredientType?) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        },
        title = { Text("Select Type") },
        text = {
            val options = IngredientType.entries + listOf(null)

            Column {
                options.forEach { type ->
                    val displayName =
                        type?.let { getIngredientTypeName(it) } ?: "(None)"

                    ListItem(
                        headlineContent = { Text(displayName) },
                        modifier = Modifier.clickable {
                            onSelectType(type)
                        },
                        // Highlight the currently selected item
                        colors = if (currentType == type) {
                            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            ListItemDefaults.colors()
                        }
                    )
                }
            }
        }
    )
}