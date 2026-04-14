package jen.doughapp.ui.recipe

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
import jen.doughapp.ui.components.DoughAmountEditBox
import jen.doughapp.ui.components.DoughCard
import jen.doughapp.ui.components.DoughFilterChipRow
import jen.doughapp.ui.components.DoughIconCard
import jen.doughapp.ui.components.DoughIngredientTypeTag
import jen.doughapp.ui.components.DoughPrimaryButton
import jen.doughapp.ui.components.DoughSectionHeader
import jen.doughapp.ui.components.DoughTag
import jen.doughapp.ui.components.DoughTopAppBar
import jen.doughapp.ui.recipe.IngredientDisplayModel
import jen.doughapp.ui.navigation.Screen
import jen.doughapp.ui.utils.formatAmount
import jen.doughapp.ui.utils.formatBakersPercentage
import jen.doughapp.ui.utils.formatHydration
import jen.doughapp.ui.utils.formatMultiplier

//todo: cleanup, this screen is a mess

@Composable
fun RecipeDetailScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: RecipeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Recipe...")
        }
        return
    }

    // Multiplier that drives all the scale and weight calculations
    val multiplier by viewModel.multiplier.collectAsStateWithLifecycle()

    // The custom multiplier if there is one
    val customMultiplier by viewModel.customMultiplier.collectAsStateWithLifecycle()

    // The custom multiplier input text
    val customMultiplierInput by viewModel.customMultiplierInput.collectAsStateWithLifecycle()

    // A list of multipliers we want to make available for quick selection
    val commonMultipliers = listOf(
        0.5, 1.0, 1.5, 2.0
    )

    val onMultiplierInputChange: (String) -> Unit = { input ->
        viewModel.updateMultiplier(input, commonMultipliers)
    }

    val onCustomMultiplierInputChange: (String) -> Unit = { input ->
        viewModel.onCustomMultiplierInputChange(input)
    }

    RecipeDetailContent(
        recipeName = uiState.recipe.name,
        recipeYield = uiState.recipe.yield,
        multiplier = uiState.multiplier,
        customMultiplierInput = customMultiplierInput,
        onMultiplierInputChange = onMultiplierInputChange,
        onCustomMultiplierInputChange = onCustomMultiplierInputChange,
        commonMultipliers = commonMultipliers,
        ingredients = uiState.displayIngredients,
        hydration = uiState.hydration,
        onBack = onBack,
        onLevainPlannerClick = {
            val rawStarter = getTotalIngredientType(uiState.displayIngredients, IngredientType.STARTER)
            val scaledStarter = rawStarter * uiState.multiplier

            // Limit to 2 decimal points
            val starterStr = "%.2f".format(scaledStarter)
            navController.navigate(
                Screen.LevainPlanner.createRoute(starterStr))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    recipeName: String,
    recipeYield: String,
    multiplier: Double,
    customMultiplierInput: String,
    onMultiplierInputChange: (String) -> Unit,
    onCustomMultiplierInputChange: (String) -> Unit,
    commonMultipliers: List<Double>,
    ingredients: List<IngredientDisplayModel>,
    hydration: Double,
    onBack: () -> Unit,
    onLevainPlannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val totalWeight = ingredients.sumOf { it.amount }

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

            val formattedMultipliers = commonMultipliers.map {
                it.formatMultiplier()
            }

            DoughFilterChipRow(
                modifier = Modifier.fillMaxWidth(),
                focusManager = focusManager,
                staticChips = formattedMultipliers,
                selectedValue = multiplier.formatMultiplier(),
                onCommitValueChange = onMultiplierInputChange,
                showCustomChip = true,
                customInputValue = customMultiplierInput,
                onCustomInputValueChange = { newValue ->
                    // Ignore invalid inputs like "#"
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        onCustomMultiplierInputChange(newValue)
                    }
               },
                keyboardType = KeyboardType.Decimal
            )

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
                            text = if (hydration > 0) hydration.formatHydration() else "--",
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
                text = "Variations (planned)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Process Steps (planned)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Start Baking (timers) (planned)",
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
                    text = ingredient.bakersPercent.formatBakersPercentage(),
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
    val rawValue = ingredient.amount * multiplier
    val displayValue = rawValue.formatAmount()

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
                customMultiplierInput = "",
                onMultiplierInputChange = {},
                onCustomMultiplierInputChange = {},
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
                hydration = 70.0,
                onBack = {},
                onLevainPlannerClick = {}
            )
        }
    }
}
