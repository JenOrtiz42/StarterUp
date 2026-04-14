package jen.doughapp.ui.levain

import jen.doughapp.domain.LevainIngredients
import jen.doughapp.domain.StarterRatio

data class LevainUiState(
    val targetAmount: String = "200",
    val selectedRatio: StarterRatio? = null,
    val ratios: List<StarterRatio> = listOf(
        StarterRatio(1, 2, 2),
        StarterRatio(1, 3, 3),
        StarterRatio(1, 5, 5)
    ),
    val ingredients: LevainIngredients = LevainIngredients(0, 0, 0)
)

sealed interface LevainEvent {
    data class UpdateTargetAmount(val amount: String) : LevainEvent
    data class SelectRatio(val ratio: StarterRatio) : LevainEvent
    data class UpdateCustomRatio(val flour: Int, val water: Int) : LevainEvent
}