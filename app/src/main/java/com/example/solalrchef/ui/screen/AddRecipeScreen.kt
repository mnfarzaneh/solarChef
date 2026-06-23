package com.example.solalrchef.ui.screen

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.solalrchef.model.Ingredient
import com.example.solalrchef.viewmodel.AddRecipeViewModel

// ─── رنگ‌ها ───────────────────────────────────────────────
private val ARBgLight      = Color(0xFFF5EFE6)
private val ARGlassCard    = Color(0xAAFFFFFF)
private val ARGlassWhite   = Color(0xCCFFFFFF)
private val ARAccentOrange = Color(0xFFFF6B35)
private val ARTextDark     = Color(0xFF2C1810)
private val ARTextLight    = Color(0xFF9E7B6A)
private val ARDivider      = Color(0x33000000)

@Composable
fun AddRecipeScreen(
    navController: NavController,
    editRecipeId: String? = null
) {
    val context = LocalContext.current
    val viewModel: AddRecipeViewModel = hiltViewModel()

    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isEditMode = editRecipeId != null


    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.updateImageUri(uri) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ARAccentOrange)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ARBgLight)
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
                        .background(ARGlassWhite)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ARTextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isEditMode) "ویرایش دستور" else "دستور جدید",
                    color = ARTextDark,
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
                    .background(ARGlassCard)
                    .border(
                        width = 2.dp,
                        color = if (state.imageUri != null) ARAccentOrange else ARDivider,
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
                            .background(ARAccentOrange),
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
                            tint = ARTextLight,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("انتخاب عکس", color = ARTextLight, fontSize = 14.sp)
                        Text(
                            "از گالری انتخاب کنید",
                            color = ARTextLight.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            color = ARDivider,
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
                                Text("مثلاً: فر، همزن، تابه")
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
                Text(
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
                        colors = listOf(Color.Transparent, ARBgLight)
                    )
                )
                .padding(20.dp)
        ) {
            Button(
                onClick = { viewModel.saveRecipe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ARAccentOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
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
            .background(ARGlassCard)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = ARAccentOrange,
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
        label = { Text(label, color = ARTextLight, fontSize = 12.sp) },
        placeholder = { Text(placeholder, color = ARTextLight.copy(0.5f), fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ARAccentOrange,
            unfocusedBorderColor = ARDivider,
            focusedTextColor     = ARTextDark,
            unfocusedTextColor   = ARTextDark,
            cursorColor          = ARAccentOrange,
        ),
        shape = RoundedCornerShape(12.dp)
    )
    if (isError) {
        Text(
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
                    .background(if (isSelected) ARAccentOrange else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) ARAccentOrange else ARDivider,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else ARTextLight,
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
            placeholder = { Text("مقدار", color = ARTextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ARAccentOrange,
                unfocusedBorderColor = ARDivider,
                focusedTextColor     = ARTextDark,
                unfocusedTextColor   = ARTextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = ingredient.unit,
            onValueChange = { onUpdate(ingredient.copy(unit = it)) },
            placeholder = { Text("واحد", color = ARTextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.width(70.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ARAccentOrange,
                unfocusedBorderColor = ARDivider,
                focusedTextColor     = ARTextDark,
                unfocusedTextColor   = ARTextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { onUpdate(ingredient.copy(name = it)) },
            placeholder = { Text("نام ماده", color = ARTextLight.copy(0.5f), fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ARAccentOrange,
                unfocusedBorderColor = ARDivider,
                focusedTextColor     = ARTextDark,
                unfocusedTextColor   = ARTextDark
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
                .background(ARAccentOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
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
                Text(
                    "مرحله را توضیح دهید...",
                    color = ARTextLight.copy(0.5f),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.weight(1f),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ARAccentOrange,
                unfocusedBorderColor = ARDivider,
                focusedTextColor     = ARTextDark,
                unfocusedTextColor   = ARTextDark
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
            .border(width = 1.dp, color = ARAccentOrange, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = ARAccentOrange,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = ARAccentOrange, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}