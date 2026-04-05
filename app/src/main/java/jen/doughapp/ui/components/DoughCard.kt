package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.Purple40Alpha10

// Outlined card style with thicker border at the bottom
@Composable
fun DoughCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    // Optional background color (defaults to surfaceVariant if null)
    containerColor: Color? = null,
    // Optional background icon
    icon: ImageVector? = null,
    // Color for the background icon
    iconTint: Color? = null,
    content: @Composable () -> Unit
) {
    //todo, pass in outline color, or base it on something else?
    //kind of want to pass in a color scheme of sorts really
    //but maybe worry about it later
    //could have something like an enum to choose style, since it's a limited number of
    //specific things, ie weight, hydration
    //val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val outlineColor = Purple40Alpha10
    val backgroundColor = containerColor ?: MaterialTheme.colorScheme.surface
    val shape = MaterialTheme.shapes.medium
    val elevation = 4.dp

    // Add a tiny bit of padding to the modifier to ensure the shadow doesn't get clipped
    val shadowModifier = modifier.padding(4.dp)

    // The Outer Surface provides the thick bottom "border"
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        color = outlineColor,
        shape = shape
    ) {
        // The Inner Surface provides the actual card background
        Surface(
            modifier = Modifier
                .padding(bottom = 2.dp, start = .5.dp, end = 1.dp, top = .5.dp),
            color = backgroundColor,
            shape = shape
        ) {

            // We use a Box to allow the background icon to sit behind the content
            Box(
                //modifier = Modifier.fillMaxWidth()
            ) {

                // If an icon is provided, render it in the background
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp) // Large size
                            .align(Alignment.BottomEnd) // Position in corner
                            .offset(x = 25.dp, y = 25.dp) // Bleed off the edges
                            .graphicsLayer(
                                alpha = 0.5f, // Semi-transparent
                                rotationZ = -15f // Tilted for style
                            ),
                        tint = iconTint ?: backgroundColor.copy(alpha = 0.8f)
                    )
                }

                // Render the main card content on top of the icon
                content()
            }
        }
    }
}


// Outlined card style with thicker border at the bottom
@Composable
fun DoughIconCard(
    modifier: Modifier = Modifier,
    // Optional background color (defaults to surfaceVariant if null)
    containerColor: Color? = null,
    // Optional background icon
    icon: ImageVector? = null,
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
        Box(
            //modifier = Modifier.fillMaxWidth()
        ) {

            // If an icon is provided, render it in the background
            if (icon != null) {
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
            }

            // Render the main card content on top of the icon
            content()
        }
    }

}

