package jen.doughapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//todo, find out how contrast works; do we need a high contrast theme?

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,

    secondary = Red50,
    onSecondary = Color.White,

    tertiary = Brown40,
    onTertiary = Color.White,

    background = WarmBackground100,
    onBackground = Red50,

    surface = Color.White,
    onSurface = BrownGray30,

    surfaceVariant = WarmBackground98,
    onSurfaceVariant = Red20,

    outlineVariant = BrownGrey93,


//    // The "Multiple Variants" approach
//    surfaceContainerLowest = Color.White,          // Use for cards you want to "pop"
//    surfaceContainerLow = WarmCreamLight4,         // Slightly darker
//    surfaceContainer = WarmBackgroundColor,        // Your "Standard" container
//    surfaceContainerHigh = BrownGrey83.copy(alpha = 0.1f), // A bit more emphasis
//    surfaceContainerHighest = BrownGrey83.copy(alpha = 0.2f), // Strongest emphasis

)

@Composable
fun DoughAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}