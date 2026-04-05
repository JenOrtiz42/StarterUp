package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.theme.WarmBackground100

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
        containerColor = WarmBackground100
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
    hasValue: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    val textCustom = "Custom"
    val labelCustom: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (!hasValue) {
                // Show textCustom as a background hint
                Text(
                    text = textCustom,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (selected) {
                            Color.Transparent
                        }
                        else {
                            MaterialTheme.colorScheme.secondary
                        }
                )
            }

            label()
        }
    }

    DoughFilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = labelCustom
    )
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