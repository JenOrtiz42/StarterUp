package jen.doughapp.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import jen.doughapp.DoughApplication
import jen.doughapp.data.IngredientType
import jen.doughapp.data.toDisplayModel
import jen.doughapp.domain.getHydration
import jen.doughapp.domain.getTotalIngredientType
import jen.doughapp.theme.Brown40
import jen.doughapp.theme.BrownIconBG
import jen.doughapp.theme.BrownIconTint
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.theme.Red50
import jen.doughapp.theme.RedIconBG
import jen.doughapp.theme.RedIconTint
import jen.doughapp.ui.RecipeViewModel
import jen.doughapp.ui.RecipeViewModelFactory
import jen.doughapp.ui.components.DoughAmountEditBox
import jen.doughapp.ui.components.DoughCard
import jen.doughapp.ui.components.DoughSectionHeader
import jen.doughapp.ui.components.DoughFilterChip
import jen.doughapp.ui.components.DoughFilterChipCustom
import jen.doughapp.ui.components.DoughIconCard
import jen.doughapp.ui.components.DoughIngredientTypeTag
import jen.doughapp.ui.components.DoughPrimaryButton
import jen.doughapp.ui.components.DoughTag
import jen.doughapp.ui.components.DoughTopAppBar
import jen.doughapp.ui.models.IngredientDisplayModel
import jen.doughapp.ui.navigation.Screen
import kotlinx.coroutines.launch

//todo: cleanup, this screen is a mess

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    //todo, note, the modern way is DI, look into that later
    val repository = remember {
        (context.applicationContext as DoughApplication).repository
    }

    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(repository)
    )

    val recipeWithIngredients by viewModel.getRecipe(recipeId).collectAsState(initial = null)

    //todo test
    if (recipeWithIngredients == null) {
        // Show a loading spinner or a simple message
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Recipe...")
        }
        return
    }

    // Multiplier that drives all the scale and weight calculations
    var multiplier by remember { mutableDoubleStateOf(1.0) }

    // The custom multplier if there is one
    var customMultiplier by remember { mutableStateOf<Double?>(null) }

    // A list of multipliers we want to make available for quick selection
    val commonMultipliers = listOf(
        0.5, 1.0, 1.5, 2.0
    )


    //todo x, verify, then remove logging

    // Fetch the last used multiplier(s) when the screen opens
    LaunchedEffect(recipeId) {
        launch {
            repository.getSavedMultiplier(recipeId).collect { savedValue ->
                multiplier = savedValue

                Log.d(
                    "DOUGH_DEBUG",
                    "LOAD multiplier: $savedValue"
                )
            }
        }
        launch {
            repository.getSavedCustomMultiplier(recipeId).collect { savedValue ->
                customMultiplier = savedValue

                Log.d(
                    "DOUGH_DEBUG",
                    "LOAD custom: $savedValue"
                )
            }
        }
    }

    //todo remove when we refactor
    // Persist the multiplier(s) when the user leaves the screen
    DisposableEffect(recipeId) {
        onDispose {
            // We use a CoroutineScope from the Application or a background thread
            // because the ViewModel scope might be canceled immediately on disposal
            (context.applicationContext as DoughApplication).applicationScope.launch {
                repository.updateRecipeMultiplier(recipeId, multiplier)
                repository.updateRecipeCustomMultiplier(recipeId, customMultiplier)
            }
        }
    }

    val ingredients = remember(recipeWithIngredients) {
        val totalFlourAmount = recipeWithIngredients?.recipe?.totalFlourAmount
        if (totalFlourAmount == null) {
            emptyList()
        } else {
            recipeWithIngredients?.ingredients?.map {
                it.toDisplayModel(totalFlourAmount)
            } ?: emptyList()
        }
    }

    // A simple counter to force refreshes (for now)
    var resetTrigger by remember { mutableIntStateOf(0) }

    val onMultiplierInputChange: (String) -> Unit = {
        val newMultiplier = it.toDoubleOrNull()
        val isValid = newMultiplier != null && newMultiplier > 0
        val isCommon = commonMultipliers.contains(newMultiplier)

        if (!isValid) {
            Log.d("DOUGH_DEBUG", "INVALID value: $it," +
                    " multiplier:$multiplier, custom:$customMultiplier")

        }

        //todo use the coroutine launch when we refactor, and get rid of the disposable
        //for now, leave it because it's working(ish)

        if (isValid) {
            Log.d(
                "DOUGH_DEBUG",
                "updated multiplier: $newMultiplier"
            )

            multiplier = newMultiplier
//            coroutineScope.launch {
//                repository.updateRecipeMultiplier(recipeId, newMultiplier)
//                Log.d("DOUGH_DEBUG", "Auto-saved multiplier: $newMultiplier")
//            }

            if (!isCommon) {
                Log.d(
                    "DOUGH_DEBUG",
                    "updated custom: $newMultiplier"
                )

                customMultiplier = newMultiplier
//                coroutineScope.launch {
//                    repository.updateRecipeCustomMultiplier(recipeId, newMultiplier)
//                    Log.d("DOUGH_DEBUG", "Auto-saved custom multiplier: $newMultiplier")
//                }
            }
        }
        else {
            Log.d(
                "DOUGH_DEBUG",
                "BLANKING custom"
            )
            // The new multiplier is not valid (and we assume it must be a custom-entered one),
            // so blank out the custom.

            //todo, refactor to make input controlled, too, but for now,
            // use resetTrigger to make sure it blanks out when not valid
            customMultiplier = null
            resetTrigger++

//            coroutineScope.launch {
//                repository.updateRecipeCustomMultiplier(recipeId, null)
//                Log.d("DOUGH_DEBUG", "Removing custom multiplier")
//            }
        }
    }

    RecipeDetailContent(
        recipeName = recipeWithIngredients?.recipe?.name ?: "Loading...",
        recipeYield = recipeWithIngredients?.recipe?.yield ?: "",
        multiplier = multiplier,
        customMultiplier = customMultiplier,
        onMultiplierInputChange = onMultiplierInputChange,
        resetTrigger = resetTrigger,
        commonMultipliers = commonMultipliers,
        ingredients = ingredients,
        onBack = onBack,

        onLevainPlannerClick = {
            val rawStarter = getTotalIngredientType(ingredients, IngredientType.STARTER)
            val scaledStarter = rawStarter * multiplier

            // Format: If it's 42.0, show "42". If 42.5, show "42.5"
            //todo, could simplify this... also we shouldn't need to format here??
            //well, could format to limit what's passed to the planner route (which
            //should have its own formatting anyway), but use same formatting function
            //as the text field
            val displayAmount = if (scaledStarter % 1.0 == 0.0) {
                scaledStarter.toInt().toString()
            } else {
                "%.1f".format(scaledStarter)
            }

            navController.navigate(Screen.LevainPlanner.createRoute(displayAmount))
        },
        modifier = modifier
    )
}


//todo, any refactoring needed to make this class "dumber"?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    recipeName: String,
    recipeYield: String,
    multiplier: Double,
    customMultiplier: Double?,
    onMultiplierInputChange: (String) -> Unit,
    resetTrigger: Int,
    commonMultipliers: List<Double>,
    ingredients: List<IngredientDisplayModel>,
    onBack: () -> Unit,
    onLevainPlannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val totalWeight = ingredients.sumOf { it.amount }

    // todo, const for now, allow changing later, w/ saved levains
    val starterHydration = 100
    val hydration = getHydration(ingredients, starterHydration)

    // The selected common multiplier (null if the multiplier isn't one of the commonMultipliers)
    val commonMultiplier : Double? = remember(multiplier) {
        multiplier.takeIf { it in commonMultipliers }
    }

    // The local input that syncs with customMultiplier
    var customMultiplierInput by remember(customMultiplier, resetTrigger) {
        Log.d(
            "DOUGH_DEBUG",
            "updating custom: $customMultiplier"
        )
        mutableStateOf(customMultiplier?.let { formatMultiplier(it) } ?: "")
    }

    var isEditingCustom by remember { mutableStateOf(false) }
    val isCustomSelected = isEditingCustom || !commonMultipliers.contains(multiplier)

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        DoughTopAppBar("Recipe Detail", onBack)

        //todo, lazycolumn?
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = recipeName,
                style = MaterialTheme.typography.headlineLarge,
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (recipeYield.isNotBlank()) {
                    Text(
                        text = recipeYield
                    )
                }
            }

            DoughSectionHeader(
                text = "Scale recipe"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                /*
                * The common multipliers act as regular FilterChips. The custom FilterChip
                * incorporates a text field that allows editing.
                * We use textFieldFocusRequester to let us conditionally focus the text field
                * inside the chip.
                * */

                val textFieldFocusRequester = remember { FocusRequester() }

                commonMultipliers.forEach {
                    DoughFilterChip(
                        //todo, consider if we want the fixed width or not
                        // might depend on the font size?
                        modifier = Modifier.width(54.dp),
                        selected = !isEditingCustom && commonMultiplier == it,
                        onClick = {
                            Log.d(
                                "DOUGH_DEBUG",
                                "tapped common, trying update: $it"
                            )
                            isEditingCustom = false
                            focusManager.clearFocus()
                            onMultiplierInputChange(it.toString())
                        },
                        label = { Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = formatMultiplier(it),
                            textAlign = TextAlign.Center
                        ) },
                    )
                }

                DoughFilterChipCustom(
                    selected = isCustomSelected,
                    onClick = {
                        // When the value is empty, a single tap should go into edit mode.
                        // If the value is NOT empty, the first tap will select it, and the
                        // second tap will go into edit mode.

                        if (customMultiplierInput.isNotEmpty() && !isCustomSelected) {
                            // Don't edit yet, just select by updating the multiplier
                            isEditingCustom = false
                            onMultiplierInputChange(customMultiplierInput)
                        }
                        else {
                            isEditingCustom = true
                            textFieldFocusRequester.requestFocus()
                        }
                    },
                    hasValue = customMultiplierInput.isNotEmpty(),
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = customMultiplierInput,
                                onValueChange = {
                                    Log.d(
                                        "DOUGH_DEBUG",
                                        "updating customMultiplierInput: $it"
                                    )

                                    // Update customMultiplierInput so the user sees what they type,
                                    // but do not update multiplier yet.
                                    customMultiplierInput = it

                                },
                                cursorBrush = SolidColor(Color.White), //todo, match text
                                modifier = Modifier
                                    .focusRequester(textFieldFocusRequester)
                                    .width(IntrinsicSize.Min)
                                    .widthIn(min = 40.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            isEditingCustom = true
                                        }
                                        if (!focusState.isFocused) {
                                            Log.d(
                                                "DOUGH_DEBUG",
                                                "Custom multiplier lost focus, trying update: $customMultiplierInput"
                                            )

                                            // No longer focused -- time to try to update the multiplier
                                            isEditingCustom = false
                                            onMultiplierInputChange(customMultiplierInput)
                                        }
                                    },
                                //todo, need to make sure we match dough filter chip
                                textStyle = MaterialTheme.typography.labelSmall.copy(
                                    textAlign = TextAlign.Center,
                                    color = if (isCustomSelected)
                                        MaterialTheme.colorScheme.onSecondary
                                    else
                                        MaterialTheme.colorScheme.secondary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                    }
                                ),
                                singleLine = true
                            )

                            // Transparent overlay to intercept taps when the chip isn't selected.
                            // This prevents BasicTextField from gaining focus on the first tap.
                            if (!isCustomSelected) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null // No ripple to avoid double-ripple with FilterChip
                                        ) {
                                            // Trigger the same logic as the FilterChip's onClick
                                            if (customMultiplierInput.isNotEmpty()) {
                                                isEditingCustom = false
                                                onMultiplierInputChange(customMultiplierInput)
                                            } else {
                                                isEditingCustom = true
                                                textFieldFocusRequester.requestFocus()
                                            }
                                        }
                                )
                            }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DoughIconCard(
                    modifier = Modifier.weight(1f),
                    containerColor = RedIconBG,
                    icon = Icons.Default.Scale,
                    iconTint = RedIconTint,
                    //dark mode version
                    //containerColor = Color(0xFF99442D), // Red50
                    //iconTint = Color(0x1AFFFFFF)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Weight".uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            //color = MaterialTheme.colorScheme.secondary
                            //color = Color.White
                        )

                        // --- EDITABLE TOTAL WEIGHT ---
                        val currentTotalDisplay = (totalWeight * multiplier)
                        val formattedValue = "%.0f".format(currentTotalDisplay)

                        var localWeightText by remember(currentTotalDisplay) {
                            mutableStateOf(formattedValue)
                        }

                        // not sure if I like the underline or not, but I'll worry about it later
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            //modifier = Modifier.wrapContentWidth()
                        ) {
                            BasicTextField(
                                value = localWeightText,
                                onValueChange = { newValue ->
                                    // Only allow numeric input, update local text immediately for typing feel
                                    if (newValue.all { it.isDigit() || it == '.' }) {
                                        localWeightText = newValue
                                    }
                                },
//                                onValueChange = { newValue ->
//                                    // Only allow numeric input
//                                    if (newValue.all { it.isDigit() || it == '.' }) {
//                                        localWeightText = newValue
//                                        val parsed = newValue.toDoubleOrNull()
//                                        if (parsed != null && totalWeight > 0) {
//                                            // Back-calculate the multiplier and update the parent state
//                                            onMultiplierInputChange((parsed / totalWeight).toString())
//                                        }
//                                    }
//                                },
                                textStyle = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Red50
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                cursorBrush = SolidColor(Red50),
                                singleLine = true,
                                modifier = Modifier
                                    // Apply the measured text width (plus a tiny buffer for the cursor)
                                    //.width(textWidth + 2.dp)
                                    //ok, this works to make it resize dynamically,
                                    //it's just that it's too wide by a fixed amount
                                    .width(IntrinsicSize.Min)
                                    //if we completely take out IntrinsicSize.Min,
                                    //the field is much too wide (and still all cyan)
                                    //and does NOT resize for text, stays within the card
                                    //in fact, stays within the card as if the g isn't there;
                                    //follows the card's margins
                                    //presumbly it acts that way because the "g" is inside
                                    //the text field, and not next to it?

                                    //further, if I LEAVE the intrinsicSize Min, but REMOVE
                                    //the g text box, the cyan is actually the correct size!
                                    //if I simply remove the g, it expands to the card exactly
                                    //as much as with the g

                                    //using the decorationBox for the "g" is supposed to allow
                                    //it to be tied to the width of the TextField, and act like
                                    //a decoration at the end of it.
                                    //Sounds great... but maybe a different approach would be better?
                                    //we might not be able to get the g to stay in the right spot
                                    //outside a decorationBox, is the idea
                                    .onFocusChanged { focusState ->
                                        // When focus is lost, commit the change
                                        if (!focusState.isFocused) {
                                            val parsed = localWeightText.toDoubleOrNull()
                                            if (parsed != null && totalWeight > 0) {
                                                onMultiplierInputChange((parsed / totalWeight).toString())
                                            }
                                        }
                                    }
                                    .drawBehind {
                                        val strokeWidth = 2f
                                        val y = size.height - strokeWidth / 2
                                        drawLine(
                                            color = Red50.copy(alpha = 0.3f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                decorationBox = { innerTextField ->
                                    Row(
                                        //todo -- debugging with background
                                        verticalAlignment = Alignment.CenterVertically,

                                        //yellow for testing
                                        //modifier = Modifier.background(Color.Yellow)

                                        //modifier = Modifier.width(IntrinsicSize.Min)
                                    ) {

                                        // WE USE A TARGETED BOX HERE
                                        //modified "ghost trick" from AI
                                        //oh hey, it DOES work, with or without intrinsicwidth
                                        //in the textfield parent
                                        //Box(modifier = Modifier.matchParentSize()) seemed to do it
                                        Box(
                                            //cyan for testing
                                            //modifier = Modifier.background(Color.Cyan)
                                        ) {
                                            // 1. This Text forces the Box to be the EXACT width of the numbers
                                            Text(
                                                text = if (localWeightText.isEmpty()) " " else localWeightText,
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.Transparent // We don't want to see it
                                            )

                                            // 2. We wrap the innerTextField in an even smaller box
                                            // to prevent it from forcing a 64dp width
                                            Box(modifier = Modifier.matchParentSize()) {
                                                innerTextField()
                                            }
                                        }

//                                      //innerTextField()

                                        Text(
                                            text = "g",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Red50,
                                            // Use a small padding to control the gap precisely
                                            modifier = Modifier.padding(start = 2.dp)
                                            //green for testing
                                                //.background(Color.Green)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                DoughIconCard(
                    modifier = Modifier.weight(1f),
                    containerColor = BrownIconBG,
                    icon = Icons.Default.WaterDrop,
                    iconTint = BrownIconTint
                    //dark mode version
                    //containerColor = Color(0xFF845300), // BrightBrown
                    //iconTint = Color(0x1AFFFFFF)
                    //Color(0xFFE3F2FD), // Light Blue
                    //Color(0xFFBBDEFB) // Slightly darker blue

                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Hydration".uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            //color = MaterialTheme.colorScheme.secondary,
                            //color = Color(0xFF1976D2) // Darker blue for text contrast
                            //color = Color.White
                            color = Brown40
                        )
                        Text(
                            text = if (hydration > 0) "$hydration%" else "--",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            //color = Color(0xFF0D47A1), //blue
                            //color = Color.White
                            color = Brown40
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            IngredientsTable(
                ingredients = ingredients,
                multiplier = multiplier,
                onMultiplierChange = { onMultiplierInputChange(it.toString()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DoughPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Levain Planner",
                onClick = onLevainPlannerClick
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Variations",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Process Steps",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Start Baking (timers)",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun IngredientsTable(
    ingredients: List<IngredientDisplayModel>,
    multiplier: Double,
    onMultiplierChange: (Double) -> Unit
) {
    Column {
        DoughSectionHeader("Ingredients")

        ingredients.forEach { ingredient ->
            Spacer(modifier = Modifier.height(8.dp))

            IngredientRow(
                ingredient = ingredient,
                multiplier = multiplier,
                onMultiplierChange = onMultiplierChange
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

//todo move
fun formatBakersPercentage(value: Double): String {
//    if (value % 1.0 == 0.0) {
//        // It's a whole number: show no decimals
//        return "${value.toInt()}%"
//    } else {
//        // It has decimals: show 1 decimal place (change to %.2f for two)
//        return "${"%.1f".format(value)}%"
//    }
//

    // Display without trailing 0s, and without a decimal if it's a whole number
    val formattedNumber = "%.2f".format(value)
        .trimEnd('0')
        .trimEnd('.')

    return "${formattedNumber}%"

}
//another option (applies to every Double in the project):
//val Double.formattedPercent: String
// get() = if (this % 1.0 == 0.0) this.toInt().toString() else "%.1f".format(this)
//text = "${ingredient.bakersPercent.formattedPercent}%"


//todo move
fun formatMultiplier(value: Double?): String {
    // Display without trailing 0s, and without a decimal if it's a whole number
    val formattedNumber = "%.2f".format(value)
        .trimEnd('0')
        .trimEnd('.')

    return formattedNumber
}


//todo, abstract into component
@Composable
fun IngredientRow(
    ingredient: IngredientDisplayModel,
    multiplier: Double,
    onMultiplierChange: (Double) -> Unit
) {
    DoughCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DoughTag(
                    text = formatBakersPercentage(ingredient.bakersPercent),
                    fixedWidth = 60.dp
                )

                Column {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (ingredient.type != null){
                        DoughIngredientTypeTag(ingredientType = ingredient.type)
                    }
                }
            }
            Column {
                IngredientValueField(
                    ingredient = ingredient,
                    multiplier = multiplier,
                    onMultiplierChange = onMultiplierChange
                )
            }
        }
    }
}

@Composable
fun IngredientValueField(
    ingredient: IngredientDisplayModel,
    multiplier: Double,
    onMultiplierChange: (Double) -> Unit
) {
    //todo, make formatting function
    // 1. Calculate the raw value
    val rawValue = ingredient.amount * multiplier

    // 2. Format: Up to 2 decimals, but strip trailing zeros and the dot if it's a whole number
    val displayValue = "%.2f".format(rawValue)
        .trimEnd('0')
        .trimEnd('.')

    var localText by remember { mutableStateOf(displayValue) }
    var isFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    //todo: is this the best way to do this?
    // Update local text when multiplier changes externally, but only if not focused
    LaunchedEffect(displayValue) {
        if (!isFocused) {
            localText = displayValue
        }
    }

    DoughAmountEditBox(
        modifier = Modifier,
        value = localText,
        onValueChange = { localText = it },
        unitText = "g",
        onDone = { focusManager.clearFocus() },
        onFocusChanged = {
            focusState ->
                // Logic: If we WERE focused and now we ARE NOT, commit the change
                if (isFocused && !focusState.isFocused) {
                    val newAmount = localText.toDoubleOrNull() ?: 0.0
                    if (ingredient.amount > 0) {
                        onMultiplierChange(newAmount / ingredient.amount)
                    }
                }
                isFocused = focusState.isFocused
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    DoughAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RecipeDetailContent(
                recipeName = "Sourdough Bread",
                recipeYield = "1 loaf",
                multiplier = 1.0,
                customMultiplier = null,
                onMultiplierInputChange = {},
                resetTrigger = 0,
                commonMultipliers = listOf(0.5, 1.0, 1.5, 2.0),
                ingredients = listOf(
                    IngredientDisplayModel(
                        "Bread Flour",
                        500.0,
                        "g",
                        100.0,
                        sortOrder = 1,
                        type = IngredientType.FLOUR
                    ),
                    IngredientDisplayModel(
                        "Water",
                        350.0,
                        "g",
                        70.0,
                        sortOrder = 2,
                        type = IngredientType.HYDRATION
                    ),
                    IngredientDisplayModel(
                        "Starter",
                        100.0, "g",
                        20.0,
                        sortOrder = 3,
                        type = IngredientType.STARTER
                    ),
                    IngredientDisplayModel(
                        "Salt",
                        12.5,
                        "g",
                        2.5,
                        sortOrder = 4,
                        type = IngredientType.SALT
                    ),
                ),
                onBack = {},
                onLevainPlannerClick = {}
            )
        }
    }
}
