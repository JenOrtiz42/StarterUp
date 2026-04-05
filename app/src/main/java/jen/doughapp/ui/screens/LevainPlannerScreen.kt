package jen.doughapp.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jen.doughapp.R
import jen.doughapp.domain.StarterRatio
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.ui.components.DoughAmountEditBox
import jen.doughapp.ui.components.DoughCard
import jen.doughapp.ui.components.DoughFilterChip
import jen.doughapp.ui.components.DoughSectionHeader
import jen.doughapp.ui.components.DoughTopAppBar
import kotlin.math.roundToInt

/*
todo, datastore for build ratios, and a way to edit them (long press?)
todo units (right now it's g)

I've made a mess of this screen by adding the ability to edit starter;
clean up later

todo, implement custom ratio value
make it easy to type, just three numbers, auto colons

*/


fun getAmount(
    targetTotal: String,
    ratioSum: Int,
    portion: Int
): Int {
    val targetNumber = targetTotal.toDoubleOrNull() ?: 0.0
    return ((targetNumber / ratioSum) * portion).roundToInt()
}

fun getTargetAmountFromStarterAmount(
    starterTotal: Int,
    ratioSum: Int
): Int {
    return (starterTotal * ratioSum)
}

fun getRatioSum(ratio: StarterRatio): Int {
    return ratio.starterPortion + ratio.flourPortion + ratio.waterPortion
}

@Composable
fun LevainScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    overrideAmount: String? = null,
    initialTargetAmount: String? = null,
    onTargetAmountUpdated: (String) -> Unit = {}
) {
    if (initialTargetAmount == null && overrideAmount == null) {
        return
    }

    val focusManager = LocalFocusManager.current

    val baseValue = overrideAmount ?: initialTargetAmount ?: "200"
    var targetAmount by remember(baseValue) { mutableStateOf(baseValue) }

    //todo, save these, have a default maybe (1:2:2)
    val ratios = listOf(
        //StarterRatio(1, 1, 1),
        StarterRatio(1, 2, 2),
        StarterRatio(1, 3, 3),
        StarterRatio(1, 5, 5),
    )
    var selectedRatio by remember { mutableStateOf(ratios[0]) }
    var localText by remember(targetAmount) {
        mutableStateOf(targetAmount)
    }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    // Clear focus when tapping outside of text fields
                    focusManager.clearFocus()
                })
            }
    ) {
        DoughTopAppBar(text = "Levain Planner", onBack = onBack)

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = "Build Your Levain",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            DoughSectionHeader(
                text = "Target Amount"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DoughAmountEditBox(
                    tempMakeLarge = true, //todo, this is a temp solution
                    value = localText,
                    onValueChange = { localText = it },
                    unitText = "g",
                    onDone = { focusManager.clearFocus() },
                    onFocusChanged = { focusState ->
                        if (isFocused && !focusState.isFocused) {
                            // We're no longer focused -- activate the change
                            targetAmount = localText
                            onTargetAmountUpdated(targetAmount)
                        }
                        isFocused = focusState.isFocused
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DoughSectionHeader(
                text = "Build Ratio"
            )

            //todo: in future editable build ratio
            //also configure default -- or just last one used
            //allow custom amount entered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ratios.forEach { ratio ->
                    DoughFilterChip(
                        selected = selectedRatio == ratio,
                        onClick = { selectedRatio = ratio },
                        label = {
                            Text("${ratio.starterPortion}:${ratio.flourPortion}:${ratio.waterPortion}") }
                    )
                }
            }

            val ratioSum = getRatioSum(selectedRatio)

            val starterAmount = getAmount(targetAmount, ratioSum, selectedRatio.starterPortion)
            val flourAmount = getAmount(targetAmount, ratioSum, selectedRatio.flourPortion)
            val waterAmount = getAmount(targetAmount, ratioSum, selectedRatio.waterPortion)

            Spacer(modifier = Modifier.height(24.dp))

            DoughCard(
//                modifier = Modifier.fillMaxWidth(),
//                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Calculated ingredients",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    AmountRow(
                        text = "Starter",
                        amount = starterAmount,
                        selectedRatio = selectedRatio,
                        iconId = R.drawable.grain_24px,
                        onUpdateTargetAmount = { newTarget ->
                            targetAmount = newTarget
                            onTargetAmountUpdated(newTarget)
                        })
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    AmountRow(
                        text ="Flour",
                        amount = flourAmount,
                        selectedRatio = selectedRatio,
                        iconId = R.drawable.wheat_24px)
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    AmountRow(
                        text ="Water",
                        amount = waterAmount,
                        selectedRatio = selectedRatio,
                        iconId = R.drawable.water_drop_24px)
                }
            }
        }
    }
}

@Composable
fun AmountRow(
    text: String,
    amount: Int,
    selectedRatio: StarterRatio,
    iconId: Int,
    onUpdateTargetAmount: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    var localText by remember { mutableStateOf(amount.toString()) }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge
        )

//        //todo, allow editing of starter -- we can do this a better way
//        if (text == "Starter") {
//
//            DoughAmountEditBox2(
//                modifier = Modifier,
//                value = localText,
//                onValueChange = { localText = it },
//                unitText = "g",
//                onDone = { focusManager.clearFocus() },
//                onFocusChanged = {
//                        focusState ->
//                    // Logic: If we WERE focused and now we ARE NOT, commit the change
//                    if (isFocused && !focusState.isFocused) {
//                        val newAmount = localText.toIntOrNull() ?: 0
//                        if (amount > 0) {
//                            val newTarget = getTargetAmountFromStarterAmount(
//                                newAmount,
//                                getRatioSum(selectedRatio)
//                            )
//                            onUpdateTargetAmount(newTarget.toString())
//                        }
//                    }
//                    isFocused = focusState.isFocused
//                }
//            )
//        }
//        else {
            Text(
                text = "$amount g",
                style = MaterialTheme.typography.titleLarge
            )
//        }
    }
}

//todo, fix to show background color throughout
@Preview(showBackground = true)
@Composable
fun LevainScreenPreview() {
    DoughAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LevainScreen(
                onBack = {},
                initialTargetAmount = "250",
                onTargetAmountUpdated = {}
            )
        }
    }
}
