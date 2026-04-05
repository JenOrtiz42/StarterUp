package jen.doughapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.R
import jen.doughapp.theme.DoughAppTheme

@Composable
fun DoughLogoIcon() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
//        Column {
//
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1.5f) // Make the surface wide instead of square
//                .shadow(
//                    elevation = 12.dp,
//                    shape = RoundedCornerShape(32.dp),
//                    spotColor = MaterialTheme.colorScheme.primary.copy(
//                        alpha = 0.4f
//                    ),
//                    ambientColor = MaterialTheme.colorScheme.primary.copy(
//                        alpha = 0.2f
//                    )
//                ),
//            color = Color.White,
//            shape = RoundedCornerShape(32.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.sourdough_logo_v3),
//                contentDescription = "App Logo",
//                modifier = Modifier
//                    .fillMaxWidth(0.8f) // Takes up 80% of screen width
//                    .aspectRatio(1f) // Keep aspect ratio
//                    .scale(0.65f), // Zoom icon
//                tint = Color.Unspecified // Keep original SVG colors
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

        DoughCard(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f) // Make the surface wide instead of square
        ) {
            Icon(
                painter = painterResource(id = R.drawable.sourdough_logo_v3),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxWidth(1f) // Takes up 80% of screen width
                    .aspectRatio(1f) // Keep aspect ratio
                    .scale(0.65f), // Zoom icon
                tint = Color.Unspecified // Keep original SVG colors
            )
        }
//        }
    }
}

@Preview(name = "Light BG", showBackground = true)
@Preview(name = "Dark BG", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun DoughLogoIconPreview() {
    DoughAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DoughLogoIcon()
        }
    }
}