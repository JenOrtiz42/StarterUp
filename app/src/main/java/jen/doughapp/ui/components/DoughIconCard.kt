package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.DoughAppTheme

@Composable
fun DoughIconCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    //todo, pass in colors as an object?
    // could also have like an enum of icon card types; they're limited
    // would prefer to integrate the sets of colors into theme or objects
    // also pass in label and text and handle that formatting?

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.medium
    ) {
        //todo note, the cards don't need to be quite as tall

        // We use a Box to allow the background icon to sit behind the content
        Box {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp) // Large size
                    .align(Alignment.BottomEnd) // Position in corner
                    .offset(x = 25.dp, y = 10.dp) // Bleed off the edges
                    .graphicsLayer(
                        alpha = 0.5f, // Semi-transparent
                        rotationZ = -15f // Tilted for style
                    ),
                tint = iconTint ?: containerColor.copy(alpha = 0.8f)
            )

            // Render the main card content on top of the icon
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoughIconCardPreview() {
    DoughAppTheme {
        Column {
            // Scale icon, 200dp width
            Column(modifier = Modifier
                .padding(20.dp)
                .width(200.dp)
            ) {
                DoughIconCard(
                    icon = Icons.Default.Scale,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Weight",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "1000g",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // Water icon, full width
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                DoughIconCard(
                    icon = Icons.Default.WaterDrop,
                    containerColor = Color(0xFFE3F2FD), // Light Blue
                    iconTint = Color(0xFF2196F3).copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hydration", style = MaterialTheme.typography.labelMedium)
                        Text("70%", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}