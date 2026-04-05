package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.DoughAppTheme

@Composable
fun DoughSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.secondary
            ),
            //modifier = Modifier.padding(bottom = 4.dp)
        )
//        // todo -- do I like this?
//        HorizontalDivider(
//            thickness = 0.5.dp,
//            color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true, name = "Section Header Preview")
@Composable
fun DoughSectionHeaderPreview() {
    DoughAppTheme(dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DoughSectionHeader(text = "Recipes")

            // Adding a second one to see how they look in a list context
            DoughSectionHeader(text = "Ingredients")
        }
    }
}
