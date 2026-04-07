package jen.doughapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.math.roundToInt

//todo: cleanup, this screen is a mess

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
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

    // LOAD: Fetch the last used multiplier when the screen opens
    LaunchedEffect(recipeId) {
        repository.getSavedMultiplier(recipeId).collect { savedValue ->
            multiplier = savedValue
        }
    }

    // SAVE: Persist the value when the user leaves the screen
    DisposableEffect(recipeId) {
        onDispose {
            // We use a CoroutineScope from the Application or a background thread
            // because the ViewModel scope might be canceled immediately on disposal
            (context.applicationContext as DoughApplication).applicationScope.launch {
                repository.updateRecipeMultiplier(recipeId, multiplier)
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

    //todo, ensure valid value
    val onNewMultiplierInputChange: (String) -> Unit = {
        //behavior:
        //currently, tries to get a valid double, and reverts to 1 if it's not valid
        //(e.g. ##)
        //common filter chips handle their own highlighting... so does custom,
        //but it gets screwed up because it's not blanked out

        //desired behavior:
        //if it's a common value, blank out custom
        //if it's invalid*, make sure custom is blanked out too
        //  invalid = null (can't be converted) or not > 0
        //if it's valid, just set the multiplier


        //todo -- the way this is done, makes it so if an invalid value is in custom,
        // it can't be edited again
        // should we blank out custom here if it's common?
        multiplier = it.toDoubleOrNull() ?: 1.0
    }

    RecipeDetailContent(
        recipeName = recipeWithIngredients?.recipe?.name ?: "Loading...",
        recipeYield = recipeWithIngredients?.recipe?.yield ?: "",
        multiplier = multiplier,
        onMultiplierInputChange = onNewMultiplierInputChange,
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
    onMultiplierInputChange: (String) -> Unit,
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

    val commonMultipliers = listOf(
        0.5, 1.0, 1.5, 2.0
    )

    // The selected common multiplier (null if the multiplier isn't one of the commonMultipliers)
    val commonMultiplier : Double? = remember(multiplier) {
        multiplier.takeIf { it in commonMultipliers }
    }


    //todo... also set customMultiplier in pref?
    // it does get saved as is, but only if it's the last used multiplier
    // consider saving on its own, too, but we need a separate value to track that
    // customMultiplier here changes while typing
    var customMultiplier by remember { mutableStateOf("") }

    //ok, the limitation of customMultiplier as is,
    //is that it's both used for typing, and saving
    //which means if we start typing, the previous value is lost
    //need the previous value to be able to "revert" if typed invalid (or a common)

    //options:
    //1. when invoking edit, save the "previous value" until done
    //  customMultiplier works the same, but if edit is done and value is invalid, it gets reverted to prev
    //  might be a big trickier to coordinate with updateMultiplier function
    //2. keep a separate value for the purpose of typing
    //  customMultiplier is pesistent, and doesn't actually get updated until edit is done
    //

    //alternative UI approach: make the custom box different,
    //don't style it as chip, but ALWAYS show the actual multiplier there
    //(except when editing, show the editing multiplier)
    //hmm, disadvantage because it wouldn't save that custom value
    //although might consider it if we choose our own scalings?


    LaunchedEffect(multiplier) {
        Log.d(
            "DOUGH_DEBUG",
            "LaunchedEffect, customMultiplier=$customMultiplier; multiplier=$multiplier, commonMultiplier=$commonMultiplier"
        )

        if (commonMultiplier == null) {
            customMultiplier = multiplier.toString()

            Log.d(
                "DOUGH_DEBUG",
                "Updated customMultiplier: $customMultiplier"
            )

        }
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

                //todo, document what we did
                //we need the text focus requester so when the chip is selcted, it goes to the text
                val textFieldFocusRequester = remember { FocusRequester() }

                commonMultipliers.forEach {
                    DoughFilterChip(
                        //todo, consider if we want the fixed width or not
                        //might depend on the font size?
                        modifier = Modifier.width(54.dp),
                        selected = !isEditingCustom && commonMultiplier == it,
                        onClick = {
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

                /*
                todo
                issues:
                -if I type something like "1" in custom, the common chip properly gets highlighted,
                    but the custom value does not blank out, and I can't get back to custom
                    (after typing, need to blank it out if it's a common one)
                    do it in multiplier input, or in Custom?
                -when invalid value entered, it goes back to 1, but I want it to revert to
                    previous value. I think we'll need to track the previous value though,
                    since customMultiplier is what's being typed
                    (either that, or change to blank)

                -save in prefs even if not selected?
                */

                DoughFilterChipCustom(
                    selected = isCustomSelected,
                    onClick = {
                        // When the value is empty, a single tap should go into edit mode.
                        // If the value is NOT empty, the first tap will select it, and the
                        // second tap will go into edit mode.

                        if (customMultiplier.isNotEmpty() && !isCustomSelected) {
                            // Don't edit yet, just select by updating the multiplier
                            isEditingCustom = false
                            onMultiplierInputChange(customMultiplier)
                        }
                        else {
                            isEditingCustom = true
                            textFieldFocusRequester.requestFocus()
                        }
                    },
                    hasValue = customMultiplier.isNotEmpty(),
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val isTextFieldFocused = remember { mutableStateOf(false) }

                            //todo, note: minor issue, but sometimes the display value can appear like
                            //1, 1.5, 2, etc
                            // Logic: If user is typing, show raw string. If not focused, show formatted.
                            val displayMultiplierText =
                                remember(customMultiplier, isTextFieldFocused.value) {
                                    if (isTextFieldFocused.value || customMultiplier.isEmpty()) {
                                        customMultiplier
                                    } else {
                                        customMultiplier
                                        //todo, need to fix so doesn't happen while editing
//                                        val num = customMultiplier.toDoubleOrNull()
//                                        if (num != null) {
//                                            // "%.2g" or similar logic to strip trailing zeros while keeping max 2 decimals
//                                            if (num % 1.0 == 0.0) "%.0f".format(num) else "%.2f".format(
//                                                num
//                                            ).trimEnd('0').trimEnd('.')
//                                        } else {
//                                            customMultiplier
//                                        }
                                    }
                                }

                            //note, text field always visible, so we don't have
                            //to worry about a delay when requesting focus
                            BasicTextField(
                                value = displayMultiplierText,
                                onValueChange = {
                                    customMultiplier = it
                                },
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    // bind this requester to this field
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
                                                "Trying update custom multiplier: $customMultiplier"
                                            )

                                            //todo, ok, if we've typed like "1", something matching
                                            //a common multiplier, we want this to go back to custom

                                            isEditingCustom = false
                                            onMultiplierInputChange(customMultiplier)
                                        }
                                    },
                                //todo, need to borrow our fccolors
                                textStyle = MaterialTheme.typography.labelLarge.copy(
                                    textAlign = TextAlign.Center,
                                    color = if (isCustomSelected)
                                        Color.White
                                    //MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
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

                            // NEW: Transparent overlay to intercept taps when the chip isn't selected.
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
                                            if (customMultiplier.isNotEmpty()) {
                                                isEditingCustom = false
                                                onMultiplierInputChange(customMultiplier)
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

                        /*

                        //"nuclear" option?
                        //technically, it DOES work, although it is too narrow
                        //by the same amount (about half the g)

                        Layout(
                            content = {
                                // Child 0: The TextField
                                BasicTextField(
                                    value = localWeightText,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() || it == '.' }) {
                                            localWeightText = newValue
                                            val parsed = newValue.toDoubleOrNull()
                                            if (parsed != null && totalWeight > 0) {
                                                onMultiplierInputChange((parsed / totalWeight).toString())
                                            }
                                        }
                                    },
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
                                        .width(IntrinsicSize.Min)
                                        .drawBehind {
                                            val strokeWidth = 2f
                                            val y = size.height - strokeWidth / 2
                                            drawLine(
                                                color = Red50.copy(alpha = 0.3f),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                )

                                // Child 1: The "g" suffix
                                Text(
                                    text = "g",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Red50,
                                )
                            }
                        ) { measurables, constraints ->
                            val textFieldPlaceable = measurables[0].measure(constraints)
                            val gPlaceable = measurables[1].measure(constraints)

                            // We subtract a fixed amount (approx 8-10dp depending on font)
                            // from the TextField's reported width to snap the 'g' inward.
                            val gapAdjustment = 12.dp.roundToPx()

                            layout(
                                width = textFieldPlaceable.width + gPlaceable.width - gapAdjustment,
                                height = kotlin.comparisons.maxOf(textFieldPlaceable.height, gPlaceable.height)
                            ) {
                                textFieldPlaceable.placeRelative(0, 0)
                                // Place the 'g' exactly where the text ends,
                                // ignoring the internal trailing padding of the TextField
                                gPlaceable.placeRelative(textFieldPlaceable.width - gapAdjustment, 0)
                            }
                        }

                        */

                        //ok, sticking with this for now, but it has a gap problem
                        // also not sure if I like the underline or not, but I'll worry about it later
                        // it actally helps me see the width of the field
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
                onMultiplierInputChange = {},
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
