package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import jen.doughapp.R
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.theme.WarmBackground100
import jen.doughapp.ui.navigation.Home
import jen.doughapp.ui.navigation.LevainPlanner
import jen.doughapp.ui.navigation.Timers

@Composable
fun DoughNavBar(
    currentDestination: NavDestination?,
    navItems: List<Any>, // List of our objects (Home, LevainPlanner, etc)
    onNavigate: (Any) -> Unit
//    selectedRoute: String?,
//    navItems: List<Screen>,
//    onNavigate: (Screen) -> Unit
) {
    //todo, navItems should be passed in, not imported?

    // We use a Box wrapper to apply the custom shadow
    // before the Surface clips the content.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(
                color = Color.Black.copy(alpha = 0.1f), // Increase alpha for "thicker" look
                blurRadius = 12.dp,                      // Controls softness
                spread = 2.dp,                           // Controls how "far" it reaches
                offsetY = (-3).dp,                        // Move shadow UP (since nav is at bottom)
                borderRadius = 24.dp
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 0.dp, // Disable native shadow
            color = WarmBackground100
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                //note: messing with the insets
                //windowInsets = WindowInsets(0, 0, 0, 0),
                //windowInsets = NavigationBarDefaults.windowInsets,
                //modifier = Modifier.height(80.dp)
                //windowInsets = WindowInsets(0),
                modifier = Modifier
                    .wrapContentHeight()
                    // FIX: This 'layout' block forces the NavigationBar to shrink its
                    // bounds to the actual size of the icons, removing the "invisible"
                    // header space that M3 NavigationBars usually reserve.
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }

            )
            {
                navItems.forEach { screen ->
                    val isSelected = currentDestination?.hasRoute(route = screen::class) == true

                    //todo, clean up styles and everything
                    NavigationBarItem(
                        icon = {
                            // We wrap Icon and Text in a Column inside the 'icon' slot.
                            // This forces the indicator pill to wrap both
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                                //modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = getIconForScreen(screen)),
                                    contentDescription = null
                                )
                                Text(
                                    text = getLabelForScreen(screen),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        //note: interestingly, the way it's set up the
                                        //color of the text changes before the color of the icon.
                                        // we have this set up here because labelSmall has a specific color
                                        // we could reconsider
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                )
                            }
                        },
                        label = null,
                        //label = { Text(getLabelForScreen(screen)) },
                        selected = isSelected,
                        onClick = { onNavigate(screen) },
                        colors = NavigationBarItemDefaults.colors(
                            // UNIFIED COLORS: Make icon and text match
                            //todo: consider a high contrast theme that uses dark purple
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            // INDICATOR: The "pill" background behind the icon
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            //indicatorColor =   MedDarkPurple
                        )
                    )
                }
            }
        }
    }
}

// Helper to handle labels since we aren't using the old Screen class
fun getLabelForScreen(screen: Any): String = when (screen) {
    is Home -> "Recipes"
    is LevainPlanner -> "Levain"
    is Timers -> "Timers"
    else -> ""
}

fun getIconForScreen(screen: Any): Int = when (screen) {
    is Home -> R.drawable.bakery_dining_24px
    is LevainPlanner -> R.drawable.calculate_24px
    is Timers -> R.drawable.alarm_24px
    else -> R.drawable.bakery_dining_24px
}

// 1. Create a helper for the soft shadow
fun Modifier.softShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    borderRadius: Dp = 24.dp,
    blurRadius: Dp = 10.dp,
    offsetY: Dp = (-2).dp,
    spread: Dp = 1.dp
) = this.drawBehind {
    val transparentColor = color.copy(alpha = 0.0f).toArgb()
    val shadowColor = color.toArgb()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor

        // This creates the "thick" blur effect
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            offsetY.toPx(),
            shadowColor
        )

        canvas.drawRoundRect(
            left = 0f - spread.toPx(),
            top = 0f - spread.toPx(),
            right = size.width + spread.toPx(),
            bottom = size.height + spread.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}


@Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
    //backgroundColor = 0xFFFBF9F5
    //backgroundColor = 0xFFFBF9F5
)
@Composable
fun DoughNavBarPreview() {
    DoughAppTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            // We wrap it in a Box with padding so the shadow
            // doesn't get clipped by the Preview window edges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                DoughNavBar(
                    currentDestination = null, // Mock destination
                    navItems = listOf(Home, LevainPlanner(), Timers),
                    onNavigate = {}
                )
            }
        }
    }
}