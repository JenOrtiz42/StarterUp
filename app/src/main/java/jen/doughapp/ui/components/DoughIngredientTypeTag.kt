package jen.doughapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.data.IngredientType
import jen.doughapp.data.getIngredientTypeName
import jen.doughapp.theme.DoughAppTheme

@Composable
fun DoughIngredientTypeTag(
    modifier: Modifier = Modifier,
    ingredientType: IngredientType?,
) {
    //todo, put these colors into Color/Theme?
    // (or just leave it the same for all themes?
    //also gray not as light as we want
    var bgColor = Color(0xFFFAFAF9)
    val borderColor = Color(0xFFF2F2F2)
    if (ingredientType == IngredientType.FLOUR) bgColor = Color(0xFFE9DAC1)
    if (ingredientType == IngredientType.HYDRATION) bgColor = Color(0xFFD0E3F0)
    if (ingredientType == IngredientType.SALT) bgColor = Color(0xFFE8E5E1)
    if (ingredientType == IngredientType.YEAST) bgColor = Color(0xFFE8F0D0)
    if (ingredientType == IngredientType.STARTER) bgColor = Color(0xFFD8F0E0)

    val displayText = ingredientType?.let { getIngredientTypeName(it) } ?: "Assign type..."

    Row(
        modifier = modifier
            .background(
                color = bgColor,
                shape = MaterialTheme.shapes.small,
            )
            // Add the border only if ingredientType is null
            .then(
                if (ingredientType == null) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.small
                    )
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            text = displayText.uppercase()
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewDoughIngredientTypeTags() {
    DoughAppTheme {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Defined Types:", style = MaterialTheme.typography.labelMedium)

            // Preview every defined enum value
            IngredientType.entries.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${type.name}: ",
                        modifier = Modifier.width(80.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    DoughIngredientTypeTag(ingredientType = type)
                }
            }

            Spacer(modifier = Modifier.size(8.dp))
            Text("Undefined/Null State:", style = MaterialTheme.typography.labelMedium)

            // Preview the "Assign type..." state
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NULL: ",
                    modifier = Modifier.width(80.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                DoughIngredientTypeTag(ingredientType = null)
            }
        }
    }
}