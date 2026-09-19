package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.EXTENDED_UNITS
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.PRIMARY_UNITS
import com.mnfarzaneh.solalrchef.model.UnitCategory
import com.mnfarzaneh.solalrchef.model.findUnit
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.SolarChefTopBar
import com.mnfarzaneh.solalrchef.viewmodel.AddRecipeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ─── رنگ‌ها ───────────────────────────────────────────────


@Composable
fun AddRecipeScreen(
    navController: NavController,
    editRecipeId: String? = null,
    autoOpenParseDialog: Boolean = false   // ← پارامتر جدید

) {
    val viewModel: AddRecipeViewModel = hiltViewModel()

    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isEditMode = editRecipeId != null
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var showParseSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var focusNewIngredient by remember { mutableStateOf(false) }
    var focusNewEquipment by remember { mutableStateOf(false) }
    var focusNewStep by remember { mutableStateOf(false) }
    var focusTitleAfterValidation by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val ingredientAddButtonRequester = remember { BringIntoViewRequester() }
    val equipmentAddButtonRequester = remember { BringIntoViewRequester() }
    val stepAddButtonRequester = remember { BringIntoViewRequester() }
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var revealSectionJob by remember { mutableStateOf<Job?>(null) }

    fun revealSectionAction(requester: BringIntoViewRequester) {
        revealSectionJob?.cancel()
        revealSectionJob = coroutineScope.launch {
            // صبر می‌کنیم کیبورد و ردیف جدید اندازه نهایی خود را بگیرند؛ سپس فقط
            // دکمه همان بخش را وارد دید می‌کنیم، نه اینکه کل فرم تا انتها پرتاب شود.
            delay(260)
            requester.bringIntoView()
        }
    }

    LaunchedEffect(currentStep) {
        keyboardController?.hide()
        scrollState.scrollTo(0)
        if (currentStep == 0 && focusTitleAfterValidation) {
            // ابتدا مرحله اول کامل چیدمان می‌شود؛ سپس همان رفتار طبیعی فوکوس
            // فیلد عنوان، آن را بالای کیبورد و در دید کاربر قرار می‌دهد.
            delay(180)
            titleFocusRequester.requestFocus()
            keyboardController?.show()
            focusTitleAfterValidation = false
        }
    }

    fun saveOrRequestTitle() {
        if (state.title.isBlank()) {
            // اعتبارسنجی ViewModel پیام خطا را نمایش می‌دهد؛ رابط کاربری نیز
            // کاربر را مستقیماً به محل برطرف‌کردن خطا هدایت می‌کند.
            viewModel.saveRecipe()
            focusTitleAfterValidation = true
            currentStep = 0
        } else {
            viewModel.saveRecipe()
        }
    }

    // ── Snackbar برای پیام‌های یک‌بار مصرف (خطاها و اعلان‌ها) ──
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddRecipeViewModel.UiEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(state.parseSuccess) {
        if (state.parseSuccess) {
            showParseSuccessDialog = true
            viewModel.clearParseSuccess()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.updateImageUri(uri) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GlassColors.AccentOrange)
        }
        return
    }

    if (showParseSuccessDialog) {
        AlertDialog(
            // دیالوگ نباید با لمس اطراف بسته شود؛ کاربر باید مسیر بعدی را انتخاب کند.
            onDismissRequest = {},
            shape = RoundedCornerShape(20.dp),
            containerColor = GlassColors.GlassWhite,
            tonalElevation = 0.dp,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextDark,
            title = {
                AppText(
                    text = stringResource(R.string.recipe_parse_success_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            },
            text = {
                AppText(
                    text = stringResource(R.string.recipe_parse_success_message),
                    color = GlassColors.TextLight,
                    fontSize = 14.sp,
                    lineHeight = 23.sp
                )
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        showParseSuccessDialog = false
                        currentStep = 0
                        coroutineScope.launch { scrollState.scrollTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassColors.AccentOrange
                    )
                ) {
                    AppText(
                        text = stringResource(R.string.recipe_review_extraction),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        showParseSuccessDialog = false
                        saveOrRequestTitle()
                    },
                    enabled = !state.isSaving,
                    border = BorderStroke(1.dp, GlassColors.AccentOrange)
                ) {
                    AppText(
                        text = stringResource(R.string.recipe_save_extraction_directly),
                        color = GlassColors.AccentOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, emoji ->
                viewModel.addAndSelectCategory(name, emoji)
                showAddCategoryDialog = false
            }
        )
    }

    Scaffold(
        containerColor = GlassColors.BgLight,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialThemeErrorColor(),
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GlassColors.BgLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp)
            ) {
                SolarChefTopBar(
                    title = stringResource(if (isEditMode) R.string.recipe_edit_title else R.string.recipe_new_title),
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = {
                        if (currentStep > 0) currentStep-- else navController.popBackStack()
                    }
                )

                AddRecipeStepIndicator(currentStep = currentStep)
                Spacer(modifier = Modifier.height(16.dp))

                // ── انتخاب عکس ───────────────────────────────
                if (currentStep == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassColors.GlassCard)
                        .border(
                            width = 2.dp,
                            color = if (state.imageUri != null) GlassColors.AccentOrange else GlassColors.Divider,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.imageUri != null) {
                        AsyncImage(
                            model = state.imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GlassColors.AccentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = GlassColors.TextLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AppText(stringResource(R.string.recipe_image_select), color = GlassColors.TextLight, fontSize = 14.sp)
                            AppText(
                                stringResource(R.string.recipe_image_gallery_hint),
                                color = GlassColors.TextLight.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ARSmartParseSection(
                    isParsing = state.isParsing,
                    onParse = { text -> viewModel.parseRecipeFromText(text) },
                    initiallyOpen = autoOpenParseDialog   // ← این خط اضافه شد
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── اطلاعات اصلی ─────────────────────────────
                ARSection(title = stringResource(R.string.recipe_main_information)) {
                    ARTextField(
                        value = state.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = stringResource(R.string.recipe_title_required),
                        placeholder = stringResource(R.string.recipe_title_example),
                        modifier = Modifier.focusRequester(titleFocusRequester),
                        onFocused = {
                            coroutineScope.launch {
                                delay(180)
                                scrollState.animateScrollTo(
                                    with(density) { 520.dp.roundToPx() }
                                        .coerceAtMost(scrollState.maxValue)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ARTextField(
                        value = state.author,
                        onValueChange = { viewModel.updateAuthor(it) },
                        label = stringResource(R.string.recipe_author_name),
                        placeholder = stringResource(R.string.recipe_author_example)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ARTextField(
                        value = state.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = stringResource(R.string.recipe_description),
                        placeholder = stringResource(R.string.recipe_description_hint),
                        minLines = 3,
                        imeAction = ImeAction.Done
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── دسته‌بندی (چندانتخابی) ─────────────────
                ARSection(title = stringResource(R.string.recipe_category)) {
                    if (state.categories.isEmpty()) {
                        AppText(
                            stringResource(R.string.recipe_no_categories),
                            color = GlassColors.TextLight,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ARCategoryChip(
                            label = stringResource(R.string.category_add_inline),
                            selected = false,
                            onClick = { showAddCategoryDialog = true }
                        )
                        state.categories.forEach { category ->
                            ARCategoryChip(
                                label = "${category.emoji} ${category.name}",
                                selected = category.id in state.categoryIds,
                                onClick = { viewModel.toggleCategory(category.id) }
                            )
                        }
                    }
                }

                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── جزئیات ───────────────────────────────────
                if (currentStep == 2) {
                ARSection(title = stringResource(R.string.recipe_details)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ARTextField(
                            value = state.totalTime,
                            onValueChange = { viewModel.updateTotalTime(it) },
                            label = stringResource(R.string.recipe_total_time),
                            placeholder = stringResource(R.string.recipe_total_time_example),
                            modifier = Modifier.weight(1f)
                        )
                        ARTextField(
                            value = state.cookTime,
                            onValueChange = { viewModel.updateCookTime(it) },
                            label = stringResource(R.string.recipe_cook_time),
                            placeholder = stringResource(R.string.recipe_cook_time_example),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ARTextField(
                            value = state.yield,
                            onValueChange = { viewModel.updateYield(it) },
                            label = stringResource(R.string.recipe_servings),
                            placeholder = "4",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        ARTextField(
                            value = state.calories,
                            onValueChange = { viewModel.updateCalories(it) },
                            label = stringResource(R.string.recipe_calories),
                            placeholder = "300",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    ARDifficultySelector(
                        selected = state.difficulty,
                        onSelect = { viewModel.updateDifficulty(it) }
                    )
                }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── مواد لازم ────────────────────────────────
                if (currentStep == 1) {
                ARSection(title = stringResource(R.string.recipe_ingredients)) {
                    state.ingredients.forEachIndexed { index, ingredient ->
                        ARIngredientRow(
                            ingredient = ingredient,
                            onUpdate = { viewModel.updateIngredient(index, it) },
                            onRemove = { viewModel.removeIngredient(index) },
                            showRemove = state.ingredients.size > 1,
                            requestInitialFocus = focusNewIngredient && index == state.ingredients.lastIndex,
                            onInitialFocusHandled = { focusNewIngredient = false },
                            onFocused = { revealSectionAction(ingredientAddButtonRequester) }
                        )
                        if (index < state.ingredients.lastIndex) {
                            Divider(
                                color = GlassColors.Divider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ARAddButton(
                        text = stringResource(R.string.recipe_add_ingredient),
                        onClick = {
                            viewModel.addIngredient()
                            focusNewIngredient = true
                        },
                        modifier = Modifier.bringIntoViewRequester(ingredientAddButtonRequester)
                    )
                }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentStep == 2) {
                ARSection(title = stringResource(R.string.recipe_equipment)) {

                    state.equipment.forEachIndexed { index, item ->
                        val equipmentFocusRequester = remember(index) { FocusRequester() }
                        LaunchedEffect(focusNewEquipment, state.equipment.size) {
                            if (focusNewEquipment && index == state.equipment.lastIndex) {
                                equipmentFocusRequester.requestFocus()
                                focusNewEquipment = false
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                value = item,
                                onValueChange = {
                                    viewModel.updateEquipment(index, it)
                                },
                                placeholder = {
                                    AppText(stringResource(R.string.recipe_equipment_example))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(equipmentFocusRequester)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            revealSectionAction(equipmentAddButtonRequester)
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (index == state.equipment.lastIndex) {
                                            viewModel.addEquipment()
                                            focusNewEquipment = true
                                        }
                                    }
                                ),
                                singleLine = true
                            )

                            if (state.equipment.size > 1) {

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        viewModel.removeEquipment(index)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    ARAddButton(
                        text = stringResource(R.string.recipe_add_equipment),
                        modifier = Modifier.bringIntoViewRequester(equipmentAddButtonRequester)
                    ) {
                        viewModel.addEquipment()
                        focusNewEquipment = true
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // ── مراحل پخت ────────────────────────────────
                ARSection(title = stringResource(R.string.recipe_steps)) {
                    state.steps.forEachIndexed { index, step ->
                        ARStepRow(
                            index = index,
                            instruction = step.instruction,
                            onUpdate = { viewModel.updateStep(index, it) },
                            onRemove = { viewModel.removeStep(index) },
                            showRemove = state.steps.size > 1,
                            requestInitialFocus = focusNewStep && index == state.steps.lastIndex,
                            onInitialFocusHandled = { focusNewStep = false },
                            onFocused = { revealSectionAction(stepAddButtonRequester) },
                            onAddNext = {
                                viewModel.addStep()
                                focusNewStep = true
                            }
                        )
                        if (index < state.steps.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ARAddButton(
                        text = stringResource(R.string.recipe_add_step),
                        onClick = {
                            viewModel.addStep()
                            focusNewStep = true
                        },
                        modifier = Modifier.bringIntoViewRequester(stepAddButtonRequester)
                    )
                }
                }
            }

            // کنترل مرحله همیشه قابل دسترس و بالاتر از کیبورد است.
            if (!isKeyboardVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(GlassColors.BgLight.copy(alpha = 0.97f))
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.height(48.dp),
                            border = BorderStroke(1.dp, GlassColors.AccentOrange),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AppText(stringResource(R.string.action_previous), color = GlassColors.AccentOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 2) currentStep++ else saveOrRequestTitle()
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            AppText(
                                text = when {
                                    currentStep < 2 -> stringResource(R.string.recipe_next_step)
                                    isEditMode -> stringResource(R.string.recipe_save_changes)
                                    else -> stringResource(R.string.recipe_save)
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

// رنگ قرمز برای Snackbar خطا (به جای MaterialTheme.colorScheme.error مستقیم، برای سازگاری با تم فعلی پروژه)
@Composable
private fun MaterialThemeErrorColor(): Color = Color(0xFFD32F2F)

// ─── Section ─────────────────────────────────────────────
@Composable
fun ARSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassColors.GlassCard)
            .padding(16.dp)
    ) {
        AppText(
            text = title,
            color = GlassColors.AccentOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

// ─── TextField ───────────────────────────────────────────
@Composable
fun ARTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onFocused: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { AppText(label, color = GlassColors.TextLight, fontSize = 12.sp) },
        placeholder = {
            AppText(
                placeholder,
                color = GlassColors.TextLight.copy(0.5f),
                fontSize = 13.sp
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocused()
                    coroutineScope.launch {
                        delay(120)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (!focusManager.moveFocus(FocusDirection.Next)) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GlassColors.AccentOrange,
            unfocusedBorderColor = GlassColors.Divider,
            focusedTextColor = GlassColors.TextDark,
            unfocusedTextColor = GlassColors.TextDark,
            cursorColor = GlassColors.AccentOrange,
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

// ─── انتخاب سطح سختی ─────────────────────────────────────
@Composable
fun ARDifficultySelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "آسان" to stringResource(R.string.recipe_difficulty_easy),
        "متوسط" to stringResource(R.string.recipe_difficulty_medium),
        "سخت" to stringResource(R.string.recipe_difficulty_hard)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) GlassColors.AccentOrange else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) GlassColors.AccentOrange else GlassColors.Divider,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = label,
                    color = if (isSelected) Color.White else GlassColors.TextLight,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─── ردیف ماده لازم ──────────────────────────────────────
@Composable
fun ARIngredientRow(
    ingredient: Ingredient,
    onUpdate: (Ingredient) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean,
    requestInitialFocus: Boolean = false,
    onInitialFocusHandled: () -> Unit = {},
    onFocused: () -> Unit = {}
) {
    val ingUnit = findUnit(ingredient.unit)
    val isVolumeUnit = ingUnit?.category == UnitCategory.VOLUME

    var showUnitMenu by remember { mutableStateOf(false) }
    var showMoreUnits by remember { mutableStateOf(false) }
    val nameFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(requestInitialFocus) {
        if (requestInitialFocus) {
            nameFocusRequester.requestFocus()
            onInitialFocusHandled()
        }
    }

    // اگر واحدی در ALL_UNITS پیدا نشود، یعنی واحد دستی است
    var isCustomUnit by remember {
        mutableStateOf(
            ingredient.unit.isNotBlank() && findUnit(ingredient.unit) == null
        )
    }

    // رنگ و شکل مشترک همه فیلدها
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GlassColors.AccentOrange,
        unfocusedBorderColor = GlassColors.Divider,
        focusedTextColor = GlassColors.TextDark,
        unfocusedTextColor = GlassColors.TextDark,
        cursorColor = GlassColors.AccentOrange
    )

    val fieldShape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // ─── ردیف اصلی ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ─── نام ماده ───────────────────────────────
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = {
                    onUpdate(ingredient.copy(name = it))
                },
                placeholder = {
                    AppText(
                        stringResource(R.string.recipe_ingredient_name),
                        color = GlassColors.TextLight.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(nameFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) onFocused()
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                ),
                colors = fieldColors,
                shape = fieldShape,
                singleLine = true
            )

            // ─── واحد ───────────────────────────────────
            Box(
                modifier = Modifier.width(100.dp)
            ) {

                if (isCustomUnit) {

                    // واحد دستی
                    OutlinedTextField(
                        value = ingredient.unit,
                        onValueChange = {
                            onUpdate(ingredient.copy(unit = it))
                        },
                        placeholder = {
                            AppText(
                                stringResource(R.string.recipe_unit),
                                color = GlassColors.TextLight.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.recipe_clear_unit),
                                tint = GlassColors.TextLight,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        isCustomUnit = false
                                        onUpdate(
                                            ingredient.copy(unit = "")
                                        )
                                    }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        shape = fieldShape,
                        singleLine = true
                    )

                } else {

                    // Dropdown واحد
                    OutlinedTextField(
                        value = ingredient.unit,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            AppText(
                                stringResource(R.string.recipe_unit),
                                color = GlassColors.TextLight.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.recipe_select_unit),
                                tint = GlassColors.TextLight,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        showUnitMenu = true
                                    }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showUnitMenu = true
                            },
                        colors = fieldColors,
                        shape = fieldShape,
                        singleLine = true
                    )
                }

                // ─── منوی واحدها ────────────────────────
                DropdownMenu(
                    expanded = showUnitMenu,
                    onDismissRequest = {
                        showUnitMenu = false
                        showMoreUnits = false
                    }
                ) {
                    PRIMARY_UNITS.forEach { u ->

                        DropdownMenuItem(
                            text = {
                                AppText(
                                    u.displayName,
                                    color = GlassColors.TextDark,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                onUpdate(
                                    ingredient.copy(
                                        unit = u.displayName
                                    )
                                )

                                isCustomUnit = false
                                showUnitMenu = false
                                showMoreUnits = false
                            }
                        )
                    }

                    Divider(color = GlassColors.Divider)

                    DropdownMenuItem(
                        text = {
                            AppText(
                                stringResource(R.string.recipe_more_units),
                                color = GlassColors.TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                if (showMoreUnits) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = GlassColors.TextLight
                            )
                        },
                        onClick = { showMoreUnits = !showMoreUnits }
                    )

                    if (showMoreUnits) {
                        EXTENDED_UNITS.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    AppText(
                                        unit.displayName,
                                        color = GlassColors.TextDark,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onUpdate(ingredient.copy(unit = unit.displayName))
                                    isCustomUnit = false
                                    showUnitMenu = false
                                    showMoreUnits = false
                                }
                            )
                        }
                    }

                    Divider(
                        color = GlassColors.Divider
                    )

                    // ─── سایر ───────────────────────────
                    DropdownMenuItem(
                        text = {
                            AppText(
                                stringResource(R.string.recipe_custom_unit),
                                color = GlassColors.AccentOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            isCustomUnit = true

                            onUpdate(
                                ingredient.copy(unit = "")
                            )

                            showUnitMenu = false
                        }
                    )
                }
            }

            // ─── مقدار ─────────────────────────────────
            OutlinedTextField(
                value = ingredient.amount,
                onValueChange = {
                    onUpdate(ingredient.copy(amount = it))
                },
                placeholder = {
                    AppText(
                        stringResource(R.string.recipe_amount),
                        color = GlassColors.TextLight.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                colors = fieldColors,
                shape = fieldShape,
                singleLine = true
            )

            // ─── حذف ───────────────────────────────────
            if (showRemove) {

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Color.Red.copy(alpha = 0.1f)
                        )
                        .clickable {
                            onRemove()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_description_delete),
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ─── دکمه‌های سریع ─────────────────────────────
        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ─── کسرها ─────────────────────────────────
            if (isVolumeUnit || ingredient.unit.isBlank()) {

                listOf(
                    "¼" to 0.25f,
                    "⅓" to 0.333f,
                    "½" to 0.5f,
                    "⅔" to 0.667f,
                    "¾" to 0.75f
                ).forEach { (frac, value) ->

                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                GlassColors.AccentOrange.copy(
                                    alpha = 0.12f
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = GlassColors.AccentOrange.copy(
                                    alpha = 0.3f
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {

                                val current =
                                    ingredient.amount.toFloatOrNull()

                                val newVal =
                                    if (current != null && current > 0f) {
                                        String.format(
                                            "%.3f",
                                            current + value
                                        )
                                            .trimEnd('0')
                                            .trimEnd('.')
                                    } else {
                                        value.toString()
                                            .trimEnd('0')
                                            .trimEnd('.')
                                    }

                                onUpdate(
                                    ingredient.copy(
                                        amount = newVal
                                    )
                                )
                            }
                            .padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                    ) {

                        AppText(
                            frac,
                            color = GlassColors.AccentOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ─── به مقدار لازم ─────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        GlassColors.TextLight.copy(
                            alpha = 0.1f
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = GlassColors.TextLight.copy(
                            alpha = 0.25f
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {

                        onUpdate(
                            ingredient.copy(
                                amount = "به مقدار لازم",
                                unit = ""
                            )
                        )

                        isCustomUnit = false
                    }
                    .padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
            ) {

                AppText(
                    stringResource(R.string.recipe_as_needed),
                    color = GlassColors.TextLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



// ─── ردیف مرحله ──────────────────────────────────────────
@Composable
fun ARStepRow(
    index: Int,
    instruction: String,
    onUpdate: (String) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean,
    requestInitialFocus: Boolean = false,
    onInitialFocusHandled: () -> Unit = {},
    onFocused: () -> Unit = {},
    onAddNext: () -> Unit = {}
) {
    val instructionFocusRequester = remember { FocusRequester() }
    LaunchedEffect(requestInitialFocus) {
        if (requestInitialFocus) {
            instructionFocusRequester.requestFocus()
            onInitialFocusHandled()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(GlassColors.AccentOrange),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                "${index + 1}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        OutlinedTextField(
            value = instruction,
            onValueChange = onUpdate,
            placeholder = {
                AppText(
                    stringResource(R.string.recipe_step_hint),
                    color = GlassColors.TextLight.copy(0.5f),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(instructionFocusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) onFocused()
                },
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onAddNext() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor = GlassColors.TextDark,
                unfocusedTextColor = GlassColors.TextDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        if (showRemove) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.1f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Red.copy(0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── چیپ انتخاب دسته‌بندی ────────────────────────────────
@Composable
fun ARCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) GlassColors.AccentOrange else GlassColors.GlassCard)
            .border(
                width = 1.dp,
                color = if (selected) GlassColors.AccentOrange else GlassColors.Divider,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        AppText(
            text = label,
            color = if (selected) Color.White else GlassColors.TextDark,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── دکمه افزودن ─────────────────────────────────────────
@Composable
fun ARAddButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = GlassColors.AccentOrange,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = GlassColors.AccentOrange,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        AppText(
            text,
            color = GlassColors.AccentOrange,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun ARSmartParseSection(
    isParsing: Boolean,
    onParse: (String) -> Unit,
    initiallyOpen: Boolean = false   // ← پارامتر جدید
) {
    var showDialog by remember { mutableStateOf(initiallyOpen) }   // ← تغییر کرد
    var pasteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassColors.AccentOrange.copy(alpha = 0.12f))
            .border(1.dp, GlassColors.AccentOrange, RoundedCornerShape(16.dp))
            .clickable(enabled = !isParsing) { showDialog = true }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = GlassColors.AccentOrange,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                AppText(
                    stringResource(R.string.recipe_parsing),
                    color = GlassColors.AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                AppText("✨", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                AppText(
                    stringResource(R.string.recipe_smart_parse),
                    color = GlassColors.AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isParsing) showDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = GlassColors.GlassWhite,
            tonalElevation = 0.dp,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextDark,
            title = {
                AppText(
                    stringResource(R.string.recipe_paste_text_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassColors.AccentOrange.copy(alpha = 0.10f))
                            .padding(12.dp)
                    ) {
                        AppText(
                            text = stringResource(R.string.recipe_extraction_vpn_notice),
                            color = GlassColors.AccentOrange,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        placeholder = {
                            AppText(stringResource(R.string.recipe_paste_text_hint))
                        },
                        minLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassColors.AccentOrange,
                            unfocusedBorderColor = GlassColors.Divider,
                            focusedTextColor = GlassColors.TextDark,
                            unfocusedTextColor = GlassColors.TextDark,
                            cursorColor = GlassColors.AccentOrange,
                            focusedContainerColor = GlassColors.GlassCard,
                            unfocusedContainerColor = GlassColors.GlassCard
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        onParse(pasteText)
                        showDialog = false
                        pasteText = ""
                    },
                    enabled = pasteText.isNotBlank() && !isParsing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassColors.AccentOrange
                    )
                ) {
                    AppText(
                        stringResource(R.string.recipe_extract),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { showDialog = false },
                    border = BorderStroke(
                        1.dp,
                        GlassColors.AccentOrange
                    )
                ) {
                    AppText(
                        stringResource(R.string.action_cancel),
                        color = GlassColors.AccentOrange
                    )
                }
            }
        )
    }

}

@Composable
private fun AddRecipeStepIndicator(currentStep: Int) {
    val titles = listOf(
        stringResource(R.string.recipe_step_information),
        stringResource(R.string.recipe_ingredients),
        stringResource(R.string.recipe_step_method)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        titles.forEachIndexed { index, title ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentStep) GlassColors.AccentOrange
                            else GlassColors.Divider.copy(alpha = 0.65f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "${index + 1}",
                        color = if (index <= currentStep) Color.White else GlassColors.TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(5.dp))
                AppText(
                    text = title,
                    color = if (index == currentStep) GlassColors.AccentOrange else GlassColors.TextLight,
                    fontSize = 11.sp,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < titles.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) GlassColors.AccentOrange
                            else GlassColors.Divider
                        )
                )
            }
        }
    }
}
