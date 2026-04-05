package jen.doughapp.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoughTopAppBar(
    text: String,
    onBack: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = { },
    )
{
    TopAppBar(
        // Set windowInsets to 0 to remove the automatic status bar padding
        // (prevents doubling up)
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.typography.titleMedium.color,
            navigationIconContentColor = MaterialTheme.typography.titleMedium.color,
            actionIconContentColor = MaterialTheme.typography.titleMedium.color
        ),
    )
}