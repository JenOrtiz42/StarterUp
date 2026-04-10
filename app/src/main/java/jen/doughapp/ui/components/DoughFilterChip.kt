package jen.doughapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.DoughAppTheme

//todo, gather the theme and color stuff together in one spot for cohesion


// Creates a row of styled filter chips with static text
@Composable
fun DoughFilterChipRow(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    staticChips: List<String>,
    selectedValue: String,
    onCommitValueChange: (String) -> Unit)
{
    DoughFilterChipRow(
        modifier = modifier,
        focusManager = focusManager,
        staticChips = staticChips,
        selectedValue = selectedValue,
        onCommitValueChange = onCommitValueChange,
        showCustomChip = false,
        customInputValue = "",
        onCustomInputValueChange = {},
        keyboardType = KeyboardType.Text
    )
}

// Creates a row of styled filter chips, with an optional editable custom value at the end
@Composable
fun DoughFilterChipRow(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    staticChips: List<String>,
    selectedValue: String,
    onCommitValueChange: (String) -> Unit,
    showCustomChip: Boolean,
    customInputValue: String,
    onCustomInputValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    /*
    * The staticChips act as regular FilterChips.
    * The custom FilterChip, if used, incorporates a text field that allows editing.
    * We use a textFieldFocusRequester to let us conditionally focus the text field
    * inside the chip.
    * */

    var isEditingCustom by remember { mutableStateOf(false) }
    val isCustomSelected = isEditingCustom || (selectedValue == customInputValue && customInputValue.isNotEmpty())
    val textFieldFocusRequester = remember { FocusRequester() }

    val staticChipOnClick: (String) -> Unit = {
        // If we were editing custom, we aren't now
        isEditingCustom = false
        // This chip is static and doesn't have focus, but
        // we need to clear focus from other fields.
        focusManager.clearFocus()
        // Update our value
        onCommitValueChange(it)
    }

    Row(
        modifier = modifier.fillMaxWidth(), //todo, do we want fillMaxWidth for sure?
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        staticChips.forEach {
            val isSelected = !isEditingCustom && selectedValue == it

            DoughFilterChip(
                //todo, decide how we want to handle...
                // consider if we want the fixed width or not
                // might depend on the font size and what text we have?
                modifier = Modifier.width(54.dp),

                selected = isSelected,
                onClick = { staticChipOnClick(it) },
                text = it
            )
        }

        if (showCustomChip) {
            DoughFilterChipCustom(
                selected = isCustomSelected,
                editing = isEditingCustom,
                onEditingChange = {
                    isEditingCustom = it
                },
                inputValue = customInputValue,
                onInputValueChange = {
                    onCustomInputValueChange(it)
                },
                onCommitValueChange = {
                    onCommitValueChange(it)
                },
                textFieldFocusRequester = textFieldFocusRequester,
                focusManager = focusManager,
                keyboardType = keyboardType
            )
        }
    }
}

// Styled filter chip
@Composable
fun DoughFilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    text: String
) {
    DoughFilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                textAlign = TextAlign.Center
            )
        }
    )
}

// Styled filter chip
// Overload allows full label composable to be passed in for custom functionality
@Composable
fun DoughFilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    //todo, for containerColor use theme with surface roles?
    // also, maybe would like a larger font, but don't want to wrap accidentally
    val fcColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.secondary,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
        labelColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.surface
    )

    val fcBorder = FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = MaterialTheme.colorScheme.secondary,
    )

    val shape = RoundedCornerShape(24.dp)

    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            CompositionLocalProvider(
                value = LocalTextStyle provides MaterialTheme.typography.labelSmall.copy(
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onSecondary
                        }
                        else {
                            MaterialTheme.colorScheme.secondary
                        }
                )
            ) {
                label()
            }
        },
        colors = fcColors,
        border = fcBorder,
        shape = shape,
    )
}

@Composable
fun DoughFilterChipCustom(
    modifier: Modifier = Modifier,
    selected: Boolean,
    editing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    onCommitValueChange: (String) -> Unit,
    focusManager: FocusManager,
    textFieldFocusRequester: FocusRequester,
    keyboardType: KeyboardType,
) {
    val hasValue = inputValue.isNotEmpty()

    val onClick: () -> Unit = {
        // When the value is empty, a single tap should go into edit mode.
        // If the value is NOT empty, the first tap will select it, and the
        // second tap will go into edit mode.
        if (hasValue && !selected) {
            // Don't edit yet -- just select by calling the commit function
            onEditingChange(false)
            onCommitValueChange(inputValue)
        }
        else {
            // Focus text field to edit
            onEditingChange(true)
            textFieldFocusRequester.requestFocus()
        }
    }

    val labelCustom: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Handles hint text display (e.g. "Custom")
            CustomChipBackgroundHint(
                selected = selected,
                hasValue = hasValue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary)

            // Handles the input
            CustomChipTextField(
                selected = selected,
                editing = editing,
                onEditingChange = onEditingChange,
                inputValue = inputValue,
                onInputValueChange = onInputValueChange,
                onCommitValueChange = onCommitValueChange,
                focusManager = focusManager,
                textFieldFocusRequester = textFieldFocusRequester,
                keyboardType = keyboardType
            )

            // Intercepts taps for more subtle UI behavior
            CustomChipTransparentOverlay(
                selected = selected,
                onClick = onClick
            )
        }
    }

    DoughFilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = labelCustom
    )
}

@Composable
private fun CustomChipTextField(
    selected: Boolean,
    editing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    onCommitValueChange: (String) -> Unit,
    focusManager: FocusManager,
    textFieldFocusRequester: FocusRequester,
    keyboardType: KeyboardType
) {
    BasicTextField(
        value = inputValue,
        onValueChange =  onInputValueChange,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondary),
        modifier = Modifier
            .focusRequester(textFieldFocusRequester)
            .width(IntrinsicSize.Min)
            .widthIn(min = 40.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onEditingChange(true)
                }
                if (editing && !focusState.isFocused) {
                    // No longer focused -- time to call the update
                    onEditingChange(false)
                    onCommitValueChange(inputValue)
                }
            },
        textStyle = MaterialTheme.typography.labelSmall.copy(
            textAlign = TextAlign.Center,
            color = if (selected)
                MaterialTheme.colorScheme.onSecondary
            else
                MaterialTheme.colorScheme.secondary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        singleLine = true
    )
}

@Composable
private fun CustomChipBackgroundHint(
    selected: Boolean,
    hasValue: Boolean,
    style: TextStyle,
    color: Color
) {
    // Show Custom as a background hint if there's no value
    if (!hasValue) {
        val textCustom = "Custom"
        Text(
            text = textCustom,
            style = style,
            color =
                if (selected) {
                    // Hide if selected
                    Color.Transparent
                }
                else {
                    // Otherwise normal color
                    color
                }
        )
    }
}

@Composable
private fun BoxScope.CustomChipTransparentOverlay(
    selected: Boolean,
    onClick: () -> Unit
) {
    // Transparent overlay to intercept taps when the chip isn't selected.
    // This prevents BasicTextField from gaining focus on the first tap.
    if (!selected) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    // Trigger the same logic as the FilterChip's onClick
                    onClick = onClick,
                    // "indication = null" disables the ripple effect, since this
                    // overlay is supposed to be transparent.
                    // Interaction source is just here because indication needs it.
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoughFilterChips() {
    DoughAppTheme {
        // State to track which number is currently selected
        var selectedNumber by remember { mutableStateOf("1") }
        val numbers = listOf("1", "2", "3", "42")

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            numbers.forEach { number ->
                DoughFilterChip(
                    modifier = Modifier.width(60.dp),
                    selected = selectedNumber == number,
                    onClick = { selectedNumber = number },
                    label = {
                        Text(
                            text = number,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }
}