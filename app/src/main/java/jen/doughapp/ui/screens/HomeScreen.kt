package jen.doughapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jen.doughapp.DoughApplication
import jen.doughapp.R
import jen.doughapp.data.Recipe
import jen.doughapp.data.RecipeWithIngredients
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.ui.RecipeViewModel
import jen.doughapp.ui.RecipeViewModelFactory
import jen.doughapp.ui.components.DoughLogoIcon
import jen.doughapp.ui.components.DoughPrimaryButton
import jen.doughapp.ui.components.DoughRecipeList

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onRecipeClick: (Long) -> Unit = {},
    onEditRecipe: (Long) -> Unit = {},
    onAddNewRecipe: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as DoughApplication).repository }
    val viewModel: RecipeViewModel = viewModel(factory = RecipeViewModelFactory(repository))
    val recipes by viewModel.recipes.collectAsState()

    HomeScreenContent(
        recipes = recipes,
        modifier = modifier,
        onRecipeClick = onRecipeClick,
        onEditRecipe = onEditRecipe,
        onDeleteRecipe = { recipe -> viewModel.deleteRecipe(recipe) },
        onAddNewRecipe = onAddNewRecipe,
        onAddDevRecipe = { viewModel.addSampleRecipe() }
    )
}

@Composable
fun HomeScreenContent(
    recipes: List<RecipeWithIngredients>,
    modifier: Modifier = Modifier,
    onRecipeClick: (Long) -> Unit = {},
    onEditRecipe: (Long) -> Unit = {},
    onDeleteRecipe: (Recipe) -> Unit = {},
    onAddNewRecipe: () -> Unit = {},
    onAddDevRecipe: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            DoughLogoIcon()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DoughPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Add New Recipe",
                onClick = onAddNewRecipe)
            Spacer(modifier = Modifier.height(24.dp))
        }

        //todo, recipe search/filter/sort
        //also, make a general recipe list component

        item {
            DoughRecipeList(
                recipes = recipes,
                onRecipeClick = onRecipeClick,
                onEditRecipe = onEditRecipe,
                onDeleteRecipe = onDeleteRecipe
            )
        }

        item {
            //todo: remove later
            Button(
                onClick = onAddDevRecipe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Add Dev Recipe")
            }
        }

        //padding at the bottom, but without causing a gap above the nav bar
        //note we took out innerpadding at the bottom, so we need a lot
        //need to find a better solution
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DoughAppTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreenContent(recipes = emptyList())
        }
    }
}
