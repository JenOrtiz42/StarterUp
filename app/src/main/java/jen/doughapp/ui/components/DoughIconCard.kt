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
    // Optional background color (defaults to surfaceVariant if null)
    containerColor: Color? = null,
    // Optional background icon
    icon: ImageVector,
    // Color for the background icon
    iconTint: Color? = null,
    content: @Composable () -> Unit
) {
    //todo, pass in colors as an object?
    //(could also have like an enum of icon card types; they're limited)
    //could also format text within this
    val backgroundColor = containerColor ?: MaterialTheme.colorScheme.surfaceVariant
    val shape = MaterialTheme.shapes.medium
    val elevation = 4.dp

    // Add a tiny bit of padding to the modifier to ensure the shadow doesn't get clipped
    val shadowModifier = modifier.padding(4.dp)

    // The Inner Surface provides the actual card background
    Surface(
//        modifier = Modifier
//            .padding(bottom = 2.dp, start = .5.dp, end = 1.dp, top = .5.dp),
        modifier = modifier,
        color = backgroundColor,
        shape = shape
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
                tint = iconTint ?: backgroundColor.copy(alpha = 0.8f)
            )

            // Render the main card content on top of the icon
            content()
        }
    }
}

@Preview(showBackground = true, name = "Single Icon Card")
@Composable
fun DoughIconCardPreview() {
    DoughAppTheme {
        Box(modifier = Modifier
            .padding(20.dp)
            .width(200.dp)) {
            DoughIconCard(
                icon = Icons.Default.Scale,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Weight",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "1000g",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Icon Card Variations")
@Composable
fun DoughIconCardVariantsPreview() {
    DoughAppTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Variation 1: Water/Hydration style
            DoughIconCard(
                icon = Icons.Default.WaterDrop,
                containerColor = Color(0xFFE3F2FD), // Light Blue
                iconTint = Color(0xFF2196F3).copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hydration", style = MaterialTheme.typography.labelLarge)
                    Text("70%", style = MaterialTheme.typography.headlineSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Variation 2: Default colors (SurfaceVariant)
            DoughIconCard(
                icon = Icons.Default.Scale,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Default Variant Card")
                }
            }
        }
    }
}