package jen.doughapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DoughDeleteAlertDialog(
    itemName: String,
    itemTypeName: String,
    onDeleteItem: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Delete $itemTypeName") },
        text = { Text("Are you sure you want to delete \"${itemName}\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onDeleteItem
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}