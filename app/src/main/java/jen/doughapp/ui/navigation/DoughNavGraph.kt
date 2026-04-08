package jen.doughapp.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jen.doughapp.DoughApplication
import jen.doughapp.data.RecipeRepository
import jen.doughapp.ui.RecipeViewModel
import jen.doughapp.ui.RecipeViewModelFactory
import jen.doughapp.ui.screens.HomeScreen
import jen.doughapp.ui.screens.LevainScreen
import jen.doughapp.ui.screens.RecipeDetailScreen
import jen.doughapp.ui.screens.RecipeEditScreen
import jen.doughapp.ui.screens.TimersScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.doughGraph(
    navController: NavHostController,
    appScope: CoroutineScope,
    repository: RecipeRepository
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onRecipeClick = { id ->
                navController.navigate(Screen.RecipeDetail.createRoute(id))
            },
            onAddNewRecipe = {
                navController.navigate(Screen.RecipeEdit.createRoute())
            },
            onEditRecipe = { id ->
                navController.navigate(Screen.RecipeEdit.createRoute(id))
            }
        )
    }

    composable(
        route = Screen.RecipeDetail.route,
        arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
    ) {
        val viewModel: RecipeViewModel = viewModel(
            factory = RecipeViewModelFactory(repository)
        )

        RecipeDetailScreen(
            viewModel = viewModel,
            navController = navController,
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.RecipeEdit.route,
        arguments = listOf(navArgument("recipeId") {
            type = NavType.LongType
            defaultValue = -1L
        })
    ) {
        val viewModel: RecipeViewModel = viewModel(
            factory = RecipeViewModelFactory(repository)
        )

        RecipeEditScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    composable(
        route = Screen.LevainPlanner.route,
        arguments = listOf(
            navArgument("overrideAmount") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val context = LocalContext.current
        val screenPrefs = (context.applicationContext as DoughApplication).screenPreferences

        val overrideAmount = backStackEntry.arguments?.getString("overrideAmount")
        val savedTarget by screenPrefs.getLevainTarget().collectAsState(initial = null)

        LevainScreen(
            onBack = { navController.popBackStack() },
            // If we have a direct override (from Recipe Detail), use it.
            // Otherwise, use the last saved value from preferences.
            overrideAmount = overrideAmount,
            initialTargetAmount = if (overrideAmount == null) savedTarget else null,
            onTargetAmountUpdated = { newAmount ->
                if (overrideAmount == null) {
                    appScope.launch {
                        screenPrefs.saveLevainTarget(newAmount)
                    }
                }
            }
        )
    }

    composable(Screen.Timers.route) {
        TimersScreen()
    }
}