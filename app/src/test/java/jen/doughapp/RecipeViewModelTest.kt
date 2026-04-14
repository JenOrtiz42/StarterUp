package jen.doughapp

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import jen.doughapp.data.RecipeRepository
import jen.doughapp.data.RecipeWithIngredients
import jen.doughapp.ui.recipe.IngredientDraft
import jen.doughapp.ui.recipe.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var repository: RecipeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk<RecipeRepository>()
//
//        every { repository.allRecipes } returns flowOf(emptyList())
//
//        viewModel = RecipeViewModel(repository)

        // Mock the repository calls that happen during init
        every { repository.allRecipes } returns flowOf(emptyList())
        // Since flatMapLatest triggers immediately, we need to mock the multiplier flow
        every { repository.getSavedMultiplier(any()) } returns flowOf(1.0)
        // If you added flatMapLatest for the recipe data as well:
        //every { repository.getRecipeById(any()) } returns flowOf(null)

        // 3. FIX: Create a dummy object because the Flow is non-nullable
        val dummyRecipe = mockk<RecipeWithIngredients>(relaxed = true)
        // 4. FIX: Return the dummy object inside the flow
        every { repository.getRecipeById(any()) } returns flowOf(dummyRecipe)

        // FIX: Explicitly define the null type for the flow
        // Replace 'RecipeWithIngredients' with the actual class name in your data layer
        //every { repository.getRecipeById(any()) } returns flowOf(null) as Flow<RecipeWithIngredients>
// This creates a Flow that can contain a RecipeWithIngredients OR a null
        //every { repository.getRecipeById(any()) } returns flowOf(null as RecipeWithIngredients?)
        //every { repository.getRecipeById(any()) } returns flowOf<RecipeWithIngredients?>(null)

        // Create a SavedStateHandle with the expected key
        val savedStateHandle = SavedStateHandle(mapOf("recipeId" to -1L))

        // Pass the handle into the constructor
        viewModel = RecipeViewModel(
            repository = repository,
            savedStateHandle = savedStateHandle
        )

        // NEW: Ensure the init block finishes before any @Test starts
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEntryValid)
        assertEquals("", state.recipe.name)
    }

    @Test
    fun `updateRecipeName updates state and validation`() {
        // Act
        viewModel.updateRecipeName("Sourdough")

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Sourdough", state.recipe.name)
        // Still invalid because weight is empty
        assertFalse(state.isEntryValid)
    }

    @Test
    fun `isValid returns true only when name and weight are valid`() {
        // 1. Just name
        viewModel.updateRecipeName("Bread")
        assertFalse(viewModel.uiState.value.isEntryValid)

        // 2. Add valid weight
        viewModel.updateFlourWeight("500")
        assertTrue(viewModel.uiState.value.isEntryValid)

        // 3. Invalid weight
        viewModel.updateFlourWeight("abc")
        assertFalse(viewModel.uiState.value.isEntryValid)
    }

    @Test
    fun `isChanged becomes true when draft differs from initial`() {
        // Initial state
        assertFalse(viewModel.uiState.value.isChanged)

        // Update something
        viewModel.updateRecipeName("New Name")
        
        assertTrue(viewModel.uiState.value.isChanged)
    }

    @Test
    fun `updateIngredients updates the list in state`() {
        val newIngredients = listOf(
            IngredientDraft(name = "Flour", percentage = "100")
        )
        
        viewModel.updateIngredients(newIngredients)
        
        assertEquals(1, viewModel.uiState.value.recipe.ingredients.size)
        assertEquals("Flour", viewModel.uiState.value.recipe.ingredients[0].name)
    }
}
