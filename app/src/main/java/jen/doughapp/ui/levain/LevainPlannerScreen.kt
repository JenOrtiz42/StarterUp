package jen.doughapp.ui.levain

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jen.doughapp.R
import jen.doughapp.domain.LevainIngredients
import jen.doughapp.domain.StarterRatio
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.ui.components.DoughAmountEditBox
import jen.doughapp.ui.components.DoughCard
import jen.doughapp.ui.components.DoughFilterChip
import jen.doughapp.ui.components.DoughFilterChipRow
import jen.doughapp.ui.components.DoughSectionHeader
import jen.doughapp.ui.components.DoughTopAppBar

/*
todo, datastore for build ratios, and a way to edit them (long press?)
todo units (right now it's g)

I've made a mess of this screen by adding the ability to edit starter;
clean up later

todo, implement custom ratio value
make it easy to type, just three numbers, auto colons

*/

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
    var selectedRatio by remember { mutableStateOf<StarterRatio?>(ratios[0]) }
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

            val formattedRatios = ratios.map {
                it.displayString
            }

            //updates based on colon-delimited string
            val updateSelectedRatio: (String) -> Unit = { newValue ->
                selectedRatio = StarterRatio.fromString(newValue)
            }

            DoughFilterChipRow(
                modifier = Modifier.fillMaxWidth(),
                focusManager = focusManager,
                staticChips = formattedRatios,
                selectedValue = selectedRatio?.displayString ?: "",
                onCommitValueChange = updateSelectedRatio,
//                showCustomChip = true,
//                customInputValue = customMultiplierInput,
//                onCustomInputValueChange = { newValue ->
//                    // Ignore invalid inputs like "#"
//                    if (newValue.all { it.isDigit() || it == '.' }) {
//                        onCustomMultiplierInputChange(newValue)
//                    }
//                },
//                keyboardType = KeyboardType.Decimal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //todo, experimental...for now using a static chip just to keep it simple

                DoughFilterChip(
                    selected = false,
                    onClick = { },
                    label = {
                        RatioInput(
                            flourValue = selectedRatio?.flourPortion.toString(),
                            onFlourChange = {
                                selectedRatio = selectedRatio?.copy(flourPortion = it.toIntOrNull() ?: 0)
                            },
                            waterValue = selectedRatio?.waterPortion.toString(),
                            onWaterChange = {
                                selectedRatio = selectedRatio?.copy(waterPortion = it.toIntOrNull() ?: 0)
                            }
                        )
                    }
                )
            }

            val amounts = remember(targetAmount, selectedRatio) {
                selectedRatio?.calculateAmounts(targetAmount) ?: LevainIngredients(0, 0, 0)
            }

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
                        amount = amounts.starterWeight,
                        selectedRatio = selectedRatio!!,
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
                        amount = amounts.flourWeight,
                        selectedRatio = selectedRatio!!,
                        iconId = R.drawable.wheat_24px)
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    AmountRow(
                        text ="Water",
                        amount = amounts.waterWeight,
                        selectedRatio = selectedRatio!!,
                        iconId = R.drawable.water_drop_24px)
                }
            }
        }
    }
}


@Composable
fun RatioInput(
    flourValue: String,
    onFlourChange: (String) -> Unit,
    waterValue: String,
    onWaterChange: (String) -> Unit
) {
    //todo:
    // it's working, yay! now need to refine:
    // need to fix font/colors
    // need to not have it enter 0 when number is deleted
    // need to have change vs commit
    // need to have it not change immediately when selected starter does
    // ...also, need to save in prefs

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("1 : ", style = MaterialTheme.typography.bodyLarge)

        // Flour Field
        BasicTextField(
            value = flourValue,
            onValueChange = { if (it.all { c -> c.isDigit() }) onFlourChange(it) },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .drawBehind {
                    drawLine(
                        Color.Gray,
                        Offset(0f, size.height),
                        Offset(size.width, size.height),
                        2f
                    )
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Text(" : ", style = MaterialTheme.typography.bodyLarge)

        // Water Field
        BasicTextField(
            value = waterValue,
            onValueChange = { if (it.all { c -> c.isDigit() }) onWaterChange(it) },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .drawBehind {
                    drawLine(
                        Color.Gray,
                        Offset(0f, size.height),
                        Offset(size.width, size.height),
                        2f
                    )
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
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
                                    //(starterTotal * ratioSum)
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
