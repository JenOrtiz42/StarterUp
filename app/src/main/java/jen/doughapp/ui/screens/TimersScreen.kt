package jen.doughapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jen.doughapp.theme.Montserrat
import jen.doughapp.theme.Neuton
import jen.doughapp.theme.NotoSerif
import jen.doughapp.theme.PlusJakartaSans
import jen.doughapp.theme.Sora

@Composable
fun TimersScreen(
) {
    // Just a planned screen, so let's see how some fonts look...

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Timers (Jakarta)",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PlusJakartaSans)
            )

            Text(
                "Timers (Noto Serif)",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = NotoSerif)
            )

            Text(
                "Timers (Sora)",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Sora)
            )

            Text(
                "Timers (Montserrat)",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Montserrat)
            )

            Text(
                "Timers (Neuton)",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Neuton)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "(Planned)",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
