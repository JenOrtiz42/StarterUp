package jen.doughapp.ui.navigation

import jen.doughapp.R

sealed class Screen(val route: String, val label: String, val icon: Int) {
    open fun createRoute(): String = route

    object Home : Screen(
        route = "home",
        label ="Home",
        icon = R.drawable.bakery_dining_24px)

    object LevainPlanner : Screen(
        route = "levainPlanner?overrideAmount={overrideAmount}",
        label = "Levain Planner",
        icon = R.drawable.calculate_24px
    ) {
        override fun createRoute(): String = "levainPlanner"

        fun createRoute(overrideAmount: String): String {
            return "levainPlanner?overrideAmount=$overrideAmount"
        }
    }

    object Timers : Screen(
        route = "timers",
        label = "Timers",
        icon = R.drawable.alarm_24px)

    // Routes not on nav
    // (assigned generic icons)
    object RecipeEdit : Screen(
        route = "recipeEdit?recipeId={recipeId}",
        label = "Edit Recipe",
        icon = R.drawable.bakery_dining_24px
    ) {
        fun createRoute(recipeId: Long? = null) =
            if (recipeId != null) "recipeEdit?recipeId=$recipeId" else "recipeEdit"
    }

    object RecipeDetail : Screen(
        route = "recipeDetail/{recipeId}",
        label = "Details",
        icon = R.drawable.bakery_dining_24px
    ) {
        fun createRoute(recipeId: Long) = "recipeDetail/$recipeId"
    }
}
