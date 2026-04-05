package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

//temporary messing around

//todo, set colors in theme
@Composable
fun DoughAmountEditBox2(
    modifier: Modifier = Modifier,
    //todo, rounding options? (since this is an amount card)
    value: String,
    onValueChange: (String) -> Unit,
    unitText: String,
    onFocusChanged: (FocusState) -> Unit,
    onDone: (KeyboardActionScope.() -> Unit)?
) {
    //todo, the cursor thing is very visible, but could make this a setting

    /*
    * We use textFieldValue, isFocused, and LaunchedEffect so we can automatically move the cursor
    * to the end when the text field is focused.
    * If the text field is already focused, we leave the cursor alone.
    * */

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            // A tiny delay is required to wait for the system tap-handling
            // to finish before moving the cursor to the end.
            delay(10)
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
        }
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    //todo, fix colors to tie into theme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        val baseStyle = MaterialTheme.typography.titleLarge

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (newValue.text.all { it.isDigit() || it == '.' }) {
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                }
            },
            modifier = Modifier
                .onFocusChanged { state ->
                    onFocusChanged(state)
                    isFocused = state.isFocused
                },
            textStyle = baseStyle.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = onDone
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    innerTextField()
                }
            }
        )
        Text(
            text = unitText,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
