package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.DoughAppTheme

// Outlined card style with thicker border at the bottom
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoughCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    rippleColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    //todo note: would prefer ripple color to be a brighter purple then primary,
    // but also want to integrate that into the theme somehow

    val outlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val shape = MaterialTheme.shapes.medium

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = rippleColor)
    ) {
        // The Outer Surface provides the thick bottom "border"
        Surface(
            onClick = onClick ?: {},
            enabled = onClick != null,
            modifier = modifier,
            color = outlineColor,
            shape = shape
        ) {
            // The Inner Surface provides the actual card background
            Surface(
                modifier = Modifier
                    .padding(bottom = 2.dp, start = .5.dp, end = 1.dp, top = .5.dp),
                color = containerColor,
                shape = shape
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, name = "Single Card Preview")
@Composable
fun DoughCardPreview() {
    DoughAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DoughCard(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Total Weight",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "500g",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Card Style Variations")
@Composable
fun DoughCardVariantsPreview() {
    DoughAppTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Standard Surface Card
            DoughCard(onClick = {}) {
                Text("Standard Card", Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Color Card
            DoughCard(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text("Colored Container", Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Non-clickable version (no shadow/interaction)
            DoughCard(onClick = null) {
                Text("Non-Clickable Card", Modifier.padding(16.dp))
            }
        }
    }
}