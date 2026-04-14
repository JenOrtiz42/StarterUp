package jen.doughapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.ui.components.DoughNavBar
import jen.doughapp.ui.navigation.Home
import jen.doughapp.ui.navigation.LevainPlanner
import jen.doughapp.ui.navigation.Timers
import jen.doughapp.ui.navigation.doughGraph

val navBarScreens = listOf(Home, LevainPlanner(), Timers)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoughAppTheme(dynamicColor = false) {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as DoughApplication
    val screenPrefs = app.screenPreferences
    val appScope = app.applicationScope

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if the current destination matches any of our main tabs
    // hasRoute<T>() is the type-safe way to check where you are
    val showBottomBar = navBarScreens.any { screen ->
        currentDestination?.hasRoute(screen::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DoughNavBar(
                    currentDestination = currentDestination,
                    navItems = navBarScreens,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },

        // This is the key: tell the Scaffold not to force insets on the content
        //contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Home,
            //modifier = Modifier.padding(innerPadding),
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                //todo: if we use innerPadding.calculateTopPadding() for the top, there's
                //a bit extra padding at the top compared to other apps, but if we don't have it,
                //the app is too high (gets in front of the camera and phone top bar)

                //note: took out bottom padding so that
                //stuff can be seen below the nav bar rounded corners
                //but this requires a ton of padding in the LazyColumn.
                //there are still insets and stuff elsewhere to prevent it from being under the
                //phone's bottom buttons
                //bottom = innerPadding.calculateBottomPadding(),
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
            ),
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            doughGraph(
                navController = navController,
                appScope = appScope,
                repository = app.repository
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    DoughAppTheme(dynamicColor = false) {
        Box(modifier = Modifier.height(150.dp)) {
            Scaffold(
                bottomBar = {
                    DoughNavBar(
                        currentDestination = null,
                        navItems = listOf(Home, LevainPlanner(), Timers),
                        onNavigate = { }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Navigation Content Area")
                }
            }
        }
    }
}


