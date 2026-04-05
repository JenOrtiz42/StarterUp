package jen.doughapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.Brown40
import jen.doughapp.theme.Orange70
import jen.doughapp.theme.Cream100

//todo, work the colors into theme (surface roles?)

@Composable
fun DoughTag(
    //todo background, text, outline -- depending on type
    text: String,
    fixedWidth: Dp? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Brown40,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(end = 12.dp)
            .let { current ->
                if (fixedWidth != null) current.widthIn(min = fixedWidth) else current
            }
            .background(
                color = Cream100,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Orange70,
                shape = CircleShape
            )
            .padding(vertical = 8.dp, horizontal= 8.dp)
    )
}