package jen.doughapp.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.KeyboardType
import jen.doughapp.theme.DoughAppTheme
import org.junit.Rule
import org.junit.Test

class DoughFilterChipRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun staticChipSelection_updatesValueAndClearsFocus() {
        var selectedValue by mutableStateOf("1")
        val staticChips = listOf("1", "2", "3")

        composeTestRule.setContent {
            DoughAppTheme {
                DoughFilterChipRow(
                    focusManager = LocalFocusManager.current,
                    staticChips = staticChips,
                    selectedValue = selectedValue,
                    onCommitValueChange = { selectedValue = it }
                )
            }
        }

        // Click on chip "2"
        composeTestRule.onNodeWithText("2").performClick()

        // Verify selection update
        assert(selectedValue == "2")

        // Verify visual state (Material3 FilterChip uses state descriptions or semantics)
        composeTestRule.onNodeWithText("2").assertIsSelected()
        composeTestRule.onNodeWithText("1").assertIsNotSelected()
    }

    @Test
    fun customChip_firstTapSelects_secondTapFocuses() {
        var committedValue by mutableStateOf("1")
        var customInput by mutableStateOf("99")

        composeTestRule.setContent {
            DoughAppTheme {
                DoughFilterChipRow(
                    focusManager = LocalFocusManager.current,
                    staticChips = listOf("1", "2"),
                    selectedValue = committedValue,
                    onCommitValueChange = { committedValue = it },
                    showCustomChip = true,
                    customInputValue = customInput,
                    onCustomInputValueChange = { customInput = it },
                    keyboardType = KeyboardType.Number
                )
            }
        }

        // Initial State: Static "1" is selected
        composeTestRule.onNodeWithText("1").assertIsSelected()

        // First tap on Custom Chip (containing "99")
        // Note: Since "99" is the text in the BasicTextField, we find it by text
        composeTestRule.onNodeWithText("99", useUnmergedTree = true).performClick()

        // Wait for the state change to propagate
        composeTestRule.waitForIdle()

        // Verify that the static chip "1" is now deselected
        composeTestRule.onNodeWithText("1").assertIsNotSelected()

        // Verify that the chip with a child with text "99" is selected
        val isFilterChip = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
        composeTestRule
            .onNode(isFilterChip and hasAnyChild(hasText("99")))
            .assertIsSelected()

        // Verify the text node with "99" is not yet focused for editing
        assert(committedValue == "99")
        composeTestRule.onNodeWithText("99").assertIsNotFocused()

        // Second tap on the already selected Custom Chip
        composeTestRule.onNodeWithText("99").performClick()
        composeTestRule.waitForIdle()

        // Behavior check: Now it should have focus
        composeTestRule.onNodeWithText("99").assertIsFocused()
    }

    @Test
    fun customChip_typingAndDoneAction_commitsValue() {
        var committedValue by mutableStateOf("1")
        var customInput by mutableStateOf("")

        composeTestRule.setContent {
            DoughAppTheme {
                DoughFilterChipRow(
                    focusManager = LocalFocusManager.current,
                    staticChips = listOf("1"),
                    selectedValue = committedValue,
                    onCommitValueChange = { committedValue = it },
                    showCustomChip = true,
                    customInputValue = customInput,
                    onCustomInputValueChange = { customInput = it },
                    keyboardType = KeyboardType.Number
                )
            }
        }

        // Tap "Custom" to start editing (since it's empty, first tap triggers edit)
        composeTestRule.onNodeWithText("Custom", useUnmergedTree = true).performClick()

        // Use hasSetTextAction to find the BasicTextField
        val inputNode = composeTestRule.onNode(hasSetTextAction(), useUnmergedTree = true)

        inputNode.performTextInput("55")
        inputNode.performImeAction() // Triggers onDone

        composeTestRule.waitForIdle()

        // Verify behavior
        assert(committedValue == "55")
        // Once committed, it should no longer be focused
        composeTestRule.onNodeWithText("55", useUnmergedTree = true).assertIsNotFocused()
    }
}