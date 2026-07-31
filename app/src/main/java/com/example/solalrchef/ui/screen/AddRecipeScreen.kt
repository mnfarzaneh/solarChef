package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.viewmodel.AddRecipeViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import androidx.compose.foundation.layout.navigationBarsPadding
// ─── رنگ‌ها ───────────────────────────────────────────────


@Composable
fun AddRecipeScreen(
    navController: NavController,
    editRecipeId: String? = null,
    autoOpenParseDialog: Boolean = false   // ← پارامتر جدید

) {
    val context = LocalContext.current
    val viewModel: AddRecipeViewModel = hiltViewModel()

    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isEditMode = editRecipeId != null

    LaunchedEffect(state.parseSuccess) {
        if (state.parseSuccess) {
            android.widget.Toast.makeText(context, "با موفقیت استخراج شد ✅", android.widget.Toast.LENGTH_SHORT).show()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // ── هدر ──────────────────────────────────────
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = statusBarHeight + 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassColors.GlassWhite)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GlassColors.TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                AppText(
                    text = if (isEditMode) "ویرایش دستور" else "دستور جدید",
                    color = GlassColors.TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── انتخاب عکس ───────────────────────────────
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
                        AppText("انتخاب عکس", color = GlassColors.TextLight, fontSize = 14.sp)
                        AppText(
                            "از گالری انتخاب کنید",
                            color = GlassColors.TextLight.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ARSmartParseSection(
                isParsing = state.isParsing,
                parseError = state.parseError,
                onParse = { text -> viewModel.parseRecipeFromText(text) },
                initiallyOpen = autoOpenParseDialog   // ← این خط اضافه شد
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── اطلاعات اصلی ─────────────────────────────
            ARSection(title = "اطلاعات اصلی") {
                ARTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = "عنوان دستور (الزامی)",
                    placeholder = "مثلاً: کباب کوبیده",
                    isError = state.error != null

                )
                Spacer(modifier = Modifier.height(10.dp))
                ARTextField(
                    value = state.author,
                    onValueChange = { viewModel.updateAuthor(it) },
                    label = "نام شما",
                    placeholder = "مثلاً: مامان"
                )
                Spacer(modifier = Modifier.height(10.dp))
                ARTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = "توضیحات",
                    placeholder = "درباره این غذا بنویسید...",
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── جزئیات ───────────────────────────────────
            ARSection(title = "جزئیات") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ARTextField(
                        value = state.totalTime,
                        onValueChange = { viewModel.updateTotalTime(it) },
                        label = "زمان کل",
                        placeholder = "۱ ساعت",
                        modifier = Modifier.weight(1f)
                    )
                    ARTextField(
                        value = state.cookTime,
                        onValueChange = { viewModel.updateCookTime(it) },
                        label = "زمان پخت",
                        placeholder = "۳۰ دقیقه",
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
                        label = "تعداد وعده",
                        placeholder = "4",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    ARTextField(
                        value = state.calories,
                        onValueChange = { viewModel.updateCalories(it) },
                        label = "کالری",
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

            Spacer(modifier = Modifier.height(16.dp))

            // ── مواد لازم ────────────────────────────────
            ARSection(title = "مواد لازم") {
                state.ingredients.forEachIndexed { index, ingredient ->
                    ARIngredientRow(
                        ingredient = ingredient,
                        onUpdate = { viewModel.updateIngredient(index, it) },
                        onRemove = { viewModel.removeIngredient(index) },
                        showRemove = state.ingredients.size > 1
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
                ARAddButton(text = "افزودن ماده", onClick = { viewModel.addIngredient() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            ARSection(title = "وسایل لازم") {

                state.equipment.forEachIndexed { index, item ->

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
                                AppText("مثلاً: فر، همزن، تابه")
                            },
                            modifier = Modifier.weight(1f)
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
                    text = "افزودن وسیله"
                ) {
                    viewModel.addEquipment()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ── مراحل پخت ────────────────────────────────
            ARSection(title = "مراحل پخت") {
                state.steps.forEachIndexed { index, step ->
                    ARStepRow(
                        index = index,
                        instruction = step.instruction,
                        onUpdate = { viewModel.updateStep(index, it) },
                        onRemove = { viewModel.removeStep(index) },
                        showRemove = state.steps.size > 1
                    )
                    if (index < state.steps.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ARAddButton(text = "افزودن مرحله", onClick = { viewModel.addStep() })
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = error,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // ── دکمه ذخیره ثابت پایین ────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GlassColors.BgLight)
                    )
                )
                .padding(50.dp)
        ) {
            Button(
                onClick = { viewModel.saveRecipe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                AppText(
                    text = if (isEditMode) "ذخیره تغییرات" else "ذخیره دستور",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

}

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
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { AppText(label, color = GlassColors.TextLight, fontSize = 12.sp) },
        placeholder = { AppText(placeholder, color = GlassColors.TextLight.copy(0.5f), fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GlassColors.AccentOrange,
            unfocusedBorderColor = GlassColors.Divider,
            focusedTextColor     = GlassColors.TextDark,
            unfocusedTextColor   = GlassColors.TextDark,
            cursorColor          = GlassColors.AccentOrange,
        ),
        shape = RoundedCornerShape(12.dp)
    )
    if (isError) {
        AppText(
            text = "عنوان دستور الزامی است",
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp
        )
    }

}

// ─── انتخاب سطح سختی ─────────────────────────────────────
@Composable
fun ARDifficultySelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("آسان", "متوسط", "سخت")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
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
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = option,
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
    showRemove: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = ingredient.amount,
            onValueChange = { onUpdate(ingredient.copy(amount = it)) },
            placeholder = { AppText("مقدار", color = GlassColors.TextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor     = GlassColors.TextDark,
                unfocusedTextColor   = GlassColors.TextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = ingredient.unit,
            onValueChange = { onUpdate(ingredient.copy(unit = it)) },
            placeholder = { AppText("واحد", color = GlassColors.TextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.width(70.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor     = GlassColors.TextDark,
                unfocusedTextColor   = GlassColors.TextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { onUpdate(ingredient.copy(name = it)) },
            placeholder = { AppText("نام ماده", color = GlassColors.TextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor     = GlassColors.TextDark,
                unfocusedTextColor   = GlassColors.TextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        if (showRemove) {
            Box(
                modifier = Modifier
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

// ─── ردیف مرحله ──────────────────────────────────────────
@Composable
fun ARStepRow(
    index: Int,
    instruction: String,
    onUpdate: (String) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean
) {
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
                    "مرحله را توضیح دهید...",
                    color = GlassColors.TextLight.copy(0.5f),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.weight(1f),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor     = GlassColors.TextDark,
                unfocusedTextColor   = GlassColors.TextDark
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

// ─── دکمه افزودن ─────────────────────────────────────────
@Composable
fun ARAddButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(width = 1.dp, color = GlassColors.AccentOrange, shape = RoundedCornerShape(10.dp))
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
        AppText(text, color = GlassColors.AccentOrange, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}



@Composable
fun ARSmartParseSection(
    isParsing: Boolean,
    parseError: String?,
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
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GlassColors.AccentOrange, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                AppText("در حال استخراج...", color = GlassColors.AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                AppText("✨", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                AppText("استخراج خودکار از متن دستور پخت", color = GlassColors.AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isParsing) showDialog = false },
            title = { AppText("متن دستور پخت را اینجا بگذارید") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        placeholder = { AppText("متن دستور پخت را کپی و اینجا پیست کنید...") },
                        minLines = 6
                    )
                    parseError?.let {
                        Spacer(Modifier.height(8.dp))
                        AppText(it, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onParse(pasteText)
                        showDialog = false
                        pasteText = ""
                    },
                    enabled = pasteText.isNotBlank() && !isParsing,   // ← isParsing هم اضافه شد
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
                ) {
                    AppText("استخراج")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { AppText("انصراف") }
            }
        )
    }

}