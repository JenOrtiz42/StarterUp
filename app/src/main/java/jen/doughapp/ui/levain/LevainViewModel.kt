package jen.doughapp.ui.levain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import jen.doughapp.domain.LevainIngredients
import jen.doughapp.domain.StarterRatio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LevainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LevainUiState())
    val uiState: StateFlow<LevainUiState> = _uiState.asStateFlow()

    init {
        // Set initial ratio
        val initialRatio = _uiState.value.ratios.first()
        onEvent(LevainEvent.SelectRatio(initialRatio))
    }

    fun onEvent(event: LevainEvent) {
        when (event) {
            is LevainEvent.UpdateTargetAmount -> {
                _uiState.update { it.copy(targetAmount = event.amount) }
                recalculateIngredients()
            }
            is LevainEvent.SelectRatio -> {
                _uiState.update { it.copy(selectedRatio = event.ratio) }
                recalculateIngredients()
            }
            is LevainEvent.UpdateCustomRatio -> {
                val newRatio = _uiState.value.selectedRatio?.copy(
                    flourPortion = event.flour,
                    waterPortion = event.water
                ) ?: StarterRatio(1, event.flour, event.water)
                _uiState.update { it.copy(selectedRatio = newRatio) }
                recalculateIngredients()
            }
        }
    }

    private fun recalculateIngredients() {
        _uiState.update { state ->
            val amounts = state.selectedRatio?.calculateAmounts(state.targetAmount)
                ?: LevainIngredients(0, 0, 0)
            state.copy(ingredients = amounts)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    // If you later add a repository:
                    // val repository = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DoughApplication).repository
                    LevainViewModel()
                }
            }
    }
}