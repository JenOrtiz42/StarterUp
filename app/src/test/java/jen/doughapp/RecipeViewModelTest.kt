package jen.doughapp

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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

        // Mock logging
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        repository = mockk<RecipeRepository>()

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<Long>("recipeId") } returns -1L

        // Mock repository calls
        every { repository.allRecipes } returns flowOf(emptyList())
        every { repository.getSavedMultiplier(any()) } returns flowOf(1.0)

        // Create a dummy object because the Flow is non-nullable
        val dummyRecipe = mockk<RecipeWithIngredients>(relaxed = true)
        every { repository.getRecipeById(any()) } returns flowOf(dummyRecipe)

        // Pass the handle into the constructor
        viewModel = RecipeViewModel(
            repository = repository,
            savedStateHandle = savedStateHandle
        )

        // Ensure the init block finishes before any @Test starts
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
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
