package jen.doughapp.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.Purple20
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.theme.Purple95
import kotlinx.coroutines.delay


//todo, set colors in theme
@Composable
fun DoughAmountEditBox(
    modifier: Modifier = Modifier,
    //todo, rounding options? (since this is an amount card)
    value: String,
    onValueChange: (String) -> Unit,
    unitText: String,
    onFocusChanged: (FocusState) -> Unit,
    onDone: (KeyboardActionScope.() -> Unit)?,
    tempMakeLarge: Boolean? = null //todo, this is a temp solution :)
) {
    //todo, the cursor thing is very visible, but could make this a setting

    /*
    * Note: We use textFieldValue, isFocused, and LaunchedEffect so we can automatically move
    * thecursor to the end when the text field is focused.
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
        val baseStyle = if (tempMakeLarge == true) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.bodyLarge
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (newValue.text.all { it.isDigit() || it == '.' }) {
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                }
            },
            singleLine = true,
            modifier = Modifier
                .background(
                    color = Purple95,
                    shape = MaterialTheme.shapes.medium,
                )
                //widthIn will allow it to grow, but it seems to be
                //too big to start with
                //.widthIn(min=10.dp)
                //.width(80.dp)
                //horrible hack
                .width(if (tempMakeLarge == true) 120.dp else 80.dp)
                .onFocusChanged { state ->
                    onFocusChanged(state)
                    isFocused = state.isFocused
                },
            textStyle = baseStyle.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                //color = MaterialTheme.colorScheme.onSurface
                color = Purple20
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
                        //.width(80.dp)
                        //.widthIn(min=80.dp)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    innerTextField()
                }
            }
        )
        Text(
            //todo, make this feel more visually integrated?
            text = unitText,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoughAmountEditBox() {
    DoughAppTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            DoughAmountEditBox(
                value = "500.0",
                unitText = "g",
                onValueChange = {},
                onFocusChanged = {},
                onDone = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InteractiveDoughAmountPreview() {
    DoughAppTheme {
        // Local state just for the preview to work interactively
        var text by remember { mutableStateOf("500") }

        Box(modifier = Modifier.padding(20.dp)) {
            DoughAmountEditBox(
                value = text,
                unitText = "g",
                onValueChange = { text = it },
                onFocusChanged = {},
                onDone = {}
            )
        }
    }
}
