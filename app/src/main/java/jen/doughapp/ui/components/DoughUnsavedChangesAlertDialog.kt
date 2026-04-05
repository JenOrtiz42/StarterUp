package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

//todo: add an explicit cancel to this dialog (maybe an x in the corner)

@Composable
fun DoughUnsavedChangesAlertDialog(
    onDiscardChanges: () -> Unit,
    onSaveChanges: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Save Changes?")
        },
        text = {
            Text("You have unsaved changes. Would you like to save them before leaving?")
       },
        confirmButton = {
            TextButton(
                onClick = onSaveChanges
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDiscardChanges
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}