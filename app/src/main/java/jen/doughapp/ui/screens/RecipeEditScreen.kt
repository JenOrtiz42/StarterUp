package jen.doughapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jen.doughapp.data.IngredientType
import jen.doughapp.theme.DoughAppTheme
import jen.doughapp.ui.IngredientDraft
import jen.doughapp.ui.RecipeViewModel
import jen.doughapp.ui.components.DoughIngredientTypePicker
import jen.doughapp.ui.components.DoughIngredientTypeTag
import jen.doughapp.ui.components.DoughPrimaryButton
import jen.doughapp.ui.components.DoughTextField
import jen.doughapp.ui.components.DoughTopAppBar
import jen.doughapp.ui.components.DoughUnsavedChangesAlertDialog

// @formatter:off
/*

-let's not display trailing 0s and .s

-screen flickers when loading a recipe to edit

-abstract out more visual components

-not sure if save coming up when adding blank ingredient, and then disabling as
soon as you type something in makes sense, so come back to this

-? also, when adding ingredient, don't show the tag until name has been entered?

-style tagging popup

-drag-and-drop re-ordering of ingredients (once everything else is working smoothly)

-future: optional yield text, notes?

-test the tagging, especially on first ingredient

future? for typos, "did you mean?" (for tagging)
common ingredients (from library?) (and templates)

*/

// @formatter:on

@Composable
fun RecipeEditScreen(
    recipeId: Long,
    onBack: () -> Unit,
    viewModel: RecipeViewModel
) {
    LaunchedEffect(recipeId) {
        viewModel.loadRecipeForEditing(recipeId)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Show toast when we have saved successfully
    var wasSaving by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSaving) {
        if (wasSaving && !uiState.isSaving) {
            // Logic: If we were saving, and now we aren't, it's successful
            // (assuming no error state is active)
            //todo: need error flag, maybe success flag
            Toast.makeText(context, "Recipe Saved", Toast.LENGTH_SHORT).show()
        }
        wasSaving = uiState.isSaving
    }

    // Show a confirmation if exiting without saving changes
    var showExitConfirmation by remember { mutableStateOf(false) }

    // Helper function to handle back navigation logic
    val handleBackNavigation = {
        if (uiState.isChanged) {
            showExitConfirmation = true
        } else {
            onBack()
        }
    }

    // Intercept system back button/gesture
    BackHandler(enabled = true) {
        handleBackNavigation()
    }

    if (showExitConfirmation) {
        DoughUnsavedChangesAlertDialog(
            onDiscardChanges = {
                showExitConfirmation = false
                onBack()
            },
            onSaveChanges = {
                showExitConfirmation = false
                viewModel.saveRecipe()
                onBack()
            },
            onCancel = { showExitConfirmation = false }
        )
    }

    val isSaveEnabled = uiState.isEntryValid && !uiState.isSaving && uiState.isChanged

    RecipeEditContent(
        recipeId = uiState.recipe.id,
        recipeName = uiState.recipe.name,
        onRecipeNameChange = { viewModel.updateRecipeName(it) },
        totalWeightInput = uiState.recipe.flourWeight,
        onFlourWeightChange = { viewModel.updateFlourWeight(it) },
        yieldInput = uiState.recipe.yield,
        onYieldChange =  { viewModel.updateYield(it) },
        ingredients = uiState.recipe.ingredients,
        onIngredientsChange = { ingredients ->
            viewModel.updateIngredients(ingredients)
        },
        onBack = handleBackNavigation,
        isSaveEnabled = isSaveEnabled,
        onSave = { viewModel.saveRecipe() }
    )
}


//todo, move to ... where?
//right we want it to happen when text is entered, assuming it's not assigned already
//it's simple/fragile because it's just based on a string
fun simpleAutoDetectType(ingredientName: String): IngredientType? {
    val name = ingredientName.lowercase()

    if (name.contains("flour")) return IngredientType.FLOUR
    if (name.contains("water")
        || name.contains("milk")) return IngredientType.HYDRATION
    if (name.contains("salt")) return IngredientType.SALT
    if (name.contains("yeast")
        || name.contains("IDY")
        || name.contains("ADY")) return IngredientType.YEAST
    if (name.contains("starter")
        || name.contains("levain")) return IngredientType.STARTER

    return null
}

@Composable
fun RecipeEditContent(
    recipeId: Long,
    recipeName: String,
    onRecipeNameChange: (String) -> Unit,
    totalWeightInput: String,
    onFlourWeightChange: (String) -> Unit,
    yieldInput: String,
    onYieldChange: (String) -> Unit,
    ingredients: List<IngredientDraft>,
    onIngredientsChange: (List<IngredientDraft>) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        // This prevents the Scaffold from adding its own padding for the keyboard
        // which often causes that "double space" look.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DoughTopAppBar(
                text = if (recipeId > 0) "Edit Recipe" else "Add Recipe",
                onBack = onBack,
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSave()
                        },
                        enabled = isSaveEnabled
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // This handles the TopBar and system offsets
                // 1. Only take the TOP padding from the Scaffold
                //.padding(top = innerPadding.calculateTopPadding())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .padding(horizontal = 16.dp) // Internal margin for your content
                .navigationBarsPadding()
                .imePadding() //make sure this is after the other padding
                // 4. Add a small bottom padding so the Save button
                // isn't physically touching the keyboard keys.
                .padding(bottom = 16.dp)
        ) {
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                // Add some bottom padding inside the list so the last item
                // isn't flush against the keyboard/Save button
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    DoughTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = recipeName,
                        onValueChange = onRecipeNameChange,
                        label = "Recipe Name",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                item {
                    Row {
                        //todo, make clear it's g
                        DoughTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            value = totalWeightInput,
                            onValueChange = onFlourWeightChange,
                            label = "Flour Weight",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        //todo, how to make clear it's string, and optional?
                        DoughTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            value = yieldInput,
                            onValueChange = onYieldChange,
                            label = "Yield (Optional)",
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ingredients".uppercase(), style = MaterialTheme.typography.titleMedium)
                }

                itemsIndexed(ingredients) { index, ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        isEditing = ingredient.isEditing,
                        //todo, is this used anymore?
                        onEditRequest = {
                            // Set this one to true, all others to false
                            onIngredientsChange(ingredients.mapIndexed { i, item ->
                                item.copy(isEditing = i == index)
                            })
                        },
                        onEditComplete = {
                            onIngredientsChange(ingredients.map {
                                it.copy(
                                    isEditing = false,
                                    //todo, does the auto detect work on first ingredient?
                                    //if it's not, according to AI might be because it's only called on complete,
                                    //and not on update
                                    type = simpleAutoDetectType(it.name)
                                    //prevent overwrite of dropdown selected
                                    //type = it.type.ifBlank { FragileAutoDetectType(it.name) }
                                )
                            })
                        },
                        onUpdate = { updated ->
                            val newList = ingredients.toMutableList()
                            newList[index] = updated
                            onIngredientsChange(newList)
                        },
                        onDelete = {
                            val newList = ingredients.toMutableList()
                            newList.removeAt(index)
                            onIngredientsChange(newList)
                        }
                    )
                }

                val isCreatingNew = ingredients.any { it.isEditing && it.name.isBlank() }

                if (!isCreatingNew) {
                    item {
                        AddIngredientCard(onClick = {
                            // Add a new item that is already in editing mode
                            onIngredientsChange(ingredients.map { it.copy(isEditing = false) } +
                                    IngredientDraft(isEditing = true))
                        })
                    }
                }
            }

            DoughPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Save",
                icon = Icons.Default.Save,
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                enabled = isSaveEnabled
            )
        }
    }
}

//todo, make into component (doughadditem ?)
// (or addingredient specifically? because add recipe and add levain will be buttons)
// maybe this is fine here
// seems like we could abstract some of the style though...
@Composable
fun AddIngredientCard(onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                drawRoundRect(
                    color = outlineColor,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Add Ingredient",
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

@Composable
fun IngredientRow(
    ingredient: IngredientDraft,
    isEditing: Boolean,
    //todo clean up... but note we're still using the isediting
    onEditRequest: () -> Unit,
    onEditComplete: () -> Unit,
    onUpdate: (IngredientDraft) -> Unit,
    onDelete: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Add a FocusRequester to manually trigger focus when switching modes
    val focusRequester = remember { FocusRequester() }
//
//    // 1. Data-driven check for completion
//    val isDataValid = ingredient.name.isNotBlank() &&
//            ingredient.percentage.toDoubleOrNull() != null

    // 2. Track if the row actually has focus
    var hasFocus by remember { mutableStateOf(false) }

    var showTypePicker by remember { mutableStateOf(false) }


// @formatter:off
    /*
    //DoughAmountEditBox

    //todo, reorganize, use doughcard for view, elevation for edit

    //refining isEditing and validation
    */
// @formatter:on



    Card(
        modifier = Modifier
            .fillMaxWidth(),
        //.clickable { onEditRequest() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                //.padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 0.dp)
                .onFocusChanged { state ->
                    hasFocus = state.isFocused

                    if (!state.isFocused && isEditing) {
                        // Only mark edit as complete if we actually HAD focus and lost it
                        // and we aren't currently in the middle of a requested focus
                        onEditComplete()
                    }
                }
        ) {
            LaunchedEffect(isEditing) {
                if (isEditing) {
                    focusRequester.requestFocus()
                }
            }

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 0.dp)
                    .onFocusChanged { state ->
                        if (!state.isFocused && isEditing) {
                            onEditComplete()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1.5f)
                ) {
                    BasicTextField(
                        value = ingredient.name,
                        onValueChange = {
                            onUpdate(ingredient.copy(name = it))
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                        ),
                        //implement placeholder
                        decorationBox = { innerTextField ->
                            Box {
                                if (ingredient.name.isEmpty()) {
                                    Text(
                                        text = "Ingredient Name",
                                        style = MaterialTheme.typography.bodyLarge, // Match your input style
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                // You MUST call innerTextField() somewhere for the actual typing to appear
                                innerTextField()
                            }
                        },
                        // Ensure the text style matches the placeholder for alignment
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        DoughIngredientTypeTag(
                            ingredientType = ingredient.type,
                            modifier = Modifier
                                .clickable { showTypePicker = true }
                        )

                        if (showTypePicker) {
                            DoughIngredientTypePicker(
                                currentType = ingredient.type,
                                onSelectType = { type ->
                                    onUpdate(ingredient.copy(type = type))
                                    showTypePicker = false
                                },
                                onCancel = { showTypePicker = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BasicTextField(
                        value = ingredient.percentage,
                        onValueChange = { onUpdate(ingredient.copy(percentage = it)) },
                        modifier = Modifier.width(50.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                // onFocusChanged will handle calling onEditComplete
                                //onEditComplete()
                            }
                        ),
                        // This is how you implement a placeholder in BasicTextField
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterEnd) {
                                if (ingredient.percentage.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = MaterialTheme.typography.bodyLarge, // Match your input style
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                }

                                innerTextField()
                            }
                        },
                        // Ensure the text style matches the placeholder for alignment
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        singleLine = true // Ensures the field doesn't expand vertically
                    )
                    Text(
                        text = "%",
                        modifier = Modifier.padding(start = 2.dp), // Small gap before the symbol
                        //modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        //fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    IconButton(onClick = onDelete) {
                        //todo: if delete a valid ingredient, allow undo
                        //need to decide UI for that, e.g. temporary toast, persistent on screen
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete")
                        //tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeEditScreenPreview() {
    DoughAppTheme {
        RecipeEditContent(
            recipeId = 0,
            recipeName = "",
            onRecipeNameChange = {},
            totalWeightInput = "",
            onFlourWeightChange = {},
            yieldInput = "",
            onYieldChange = {},
            ingredients = emptyList(),
            onIngredientsChange = {},
            isSaveEnabled = true,
            onBack = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode (Empty/New)")
@Composable
fun PreviewIngredientRowEditMode() {
    DoughAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            IngredientRow(
                ingredient = IngredientDraft(0, ""),
                isEditing = true,
                onEditRequest = {},
                onEditComplete = {},
                onUpdate = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Mode (Pre-filled)")
@Composable
fun PreviewIngredientRowEditModeFilled() {
    DoughAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            IngredientRow(
                ingredient = IngredientDraft(
                    name = "Bread Flour",
                    percentage = "100",
                    type = IngredientType.FLOUR,
                ),
                isEditing = true,
                onEditRequest = {},
                onEditComplete = {},
                onUpdate = {},
                onDelete = {}
            )
        }
    }
}
