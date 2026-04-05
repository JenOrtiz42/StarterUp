package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DoughPrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    //NOTE: the drop shadow (when using buttonModifier is a lot deeper for buttons on the
    // bottm of the screen than near the top)
    //AI suggested the drawBehind thing, though as-is the shape is not rounded enough
    //should look this up online

    val shadowColor = MaterialTheme.colorScheme.primary

    // 1. Define a consistent shadow that doesn't rely on OS "Light Sources"
    val shadowModifier = if (enabled) {
        Modifier.drawBehind {
            drawIntoCanvas { canvas ->
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()

                // This is the manual way to get a hard, consistent shadow
                // that doesn't change based on screen position
                frameworkPaint.color = shadowColor.toArgb() // Use your primary color
                frameworkPaint.setShadowLayer(
                    12f,   // Blur radius (adjust for "thickness")
                    0f,    // X offset
                    4f,    // Y offset
                    shadowColor.toArgb()
                )

                val shape = RoundedCornerShape(8.dp) // Match your button shape
                val outline = shape.createOutline(size, layoutDirection, this)
                canvas.drawOutline(outline, paint)
            }
        }
    } else {
        Modifier
    }

    val buttonModifier = if (enabled) {
        Modifier.shadow(
            elevation = 6.dp,
            //todo, subtle difference w/ or w/o ambient color
            // for now, leaving the shadow a little bit harsh
            shape = ButtonDefaults.shape, // Matches the default button rounding
            //spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            //ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            spotColor = MaterialTheme.colorScheme.primary, // This tints the shadow
            ambientColor = MaterialTheme.colorScheme.primary // Optional: tints the softer glow
        )
    } else {
        Modifier
    }

    Button(
        onClick = onClick,
        modifier = modifier.then(buttonModifier),
        //modifier = modifier.then(shadowModifier),
        enabled = enabled,
        // Using ButtonDefaults.buttonColors to ensure no tonal tinting is interfering
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            // FORCE tonal elevation to 0 here to prevent color shifts
            // and "ghost" shadows on different screens
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        // Standardize elevation here to prevent it from doubling up
        // or behaving differently across different screens
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp,
            disabledElevation = 0.dp
        )
//        elevation = ButtonDefaults.elevatedButtonElevation(
//            defaultElevation = 8.dp,
//            pressedElevation = 2.dp
//        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DoughPrimaryButtonPreview() {
    // Basic preview with default background
    Box(modifier = Modifier.padding(16.dp)) {
        DoughPrimaryButton(
            text = "Calculate Dough",
            onClick = {}
        )
    }
}
