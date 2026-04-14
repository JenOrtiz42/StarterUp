package jen.doughapp.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import jen.doughapp.DoughApplication
import jen.doughapp.data.RecipeRepository
import jen.doughapp.ui.home.HomeScreen
import jen.doughapp.ui.levain.LevainScreen
import jen.doughapp.ui.recipe.RecipeDetailScreen
import jen.doughapp.ui.recipe.RecipeEditScreen
import jen.doughapp.ui.recipe.RecipeViewModel
import jen.doughapp.ui.recipe.RecipeViewModelFactory
import jen.doughapp.ui.screens.TimersScreen
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.doughGraph(
    navController: NavHostController,
    appScope: CoroutineScope,
    repository: RecipeRepository
) {
    composable<Home> {
        HomeScreen(
            onRecipeClick = { id ->
                navController.navigate(RecipeDetail(recipeId = id))
            },
            onAddNewRecipe = {
                navController.navigate(RecipeEdit())
            },
            onEditRecipe = { id ->
                navController.navigate(RecipeEdit(recipeId = id))
            }
        )
    }

    composable<RecipeDetail>
    {
        val viewModel: RecipeViewModel = viewModel(
            factory = RecipeViewModelFactory(repository)
        )

        RecipeDetailScreen(
            viewModel = viewModel,
            navController = navController,
            onBack = { navController.popBackStack() }
        )
    }

    composable<RecipeEdit>
    {
        val viewModel: RecipeViewModel = viewModel(
            factory = RecipeViewModelFactory(repository)
        )

        RecipeEditScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    composable<LevainPlanner>
    { backStackEntry ->
        val context = LocalContext.current
        val screenPrefs = (context.applicationContext as DoughApplication).screenPreferences

        val overrideAmount = backStackEntry.toRoute<LevainPlanner>().overrideAmount
        val savedTarget by screenPrefs.getLevainTarget().collectAsState(initial = null)

        LevainScreen(
            onBack = { navController.popBackStack() },
            // If we have a direct override (from Recipe Detail), use it.
            // Otherwise, use the last saved value from preferences.
            overrideAmount = overrideAmount,
        )
    }

    composable<Timers> {
        TimersScreen()
    }
}