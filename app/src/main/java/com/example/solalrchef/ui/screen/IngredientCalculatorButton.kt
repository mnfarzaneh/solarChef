package com.mnfarzaneh.solalrchef.ui.screen

import LtrText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import kotlin.math.abs

// ═══════════════════════════════════════════════════════════
// ─── رنگ‌ها ───────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════

private val CalcBg          = Color(0xFFF5EFE6)
private val CalcCard        = Color(0xAAFFFFFF)
private val CalcOrange      = Color(0xFFFF6B35)
private val CalcOrangeLight = Color(0xFFFFF0EB)
private val CalcGreen       = Color(0xFF4CAF50)
private val CalcGreenLight  = Color(0xFFEDF7EE)
private val CalcTextDark    = Color(0xFF2C1810)
private val CalcTextLight   = Color(0xFF9E7B6A)
private val CalcDivider     = Color(0x22000000)

// ═══════════════════════════════════════════════════════════
// ─── کسرهای رایج آشپزی ───────────────────────────────────
// ═══════════════════════════════════════════════════════════

private val commonFractions = listOf(
    0.125f to "⅛",
    0.25f  to "¼",
    0.333f to "⅓",
    0.5f   to "½",
    0.667f to "⅔",
    0.75f  to "¾",
)

fun formatAmount(amount: Float): String {
    if (amount <= 0f) return "0"
    val whole   = amount.toInt()
    val decimal = amount - whole
    if (abs(decimal) < 0.05f) return whole.toString()
    val closest = commonFractions.minByOrNull { abs(it.first - decimal) }
    return if (closest != null && abs(closest.first - decimal) < 0.08f) {
        if (whole > 0) "$whole${closest.second}" else closest.second
    } else {
        String.format("%.1f", amount)
    }
}

fun formatYield(yield: Float): String =
    if (yield == yield.toLong().toFloat()) yield.toLong().toString()
    else String.format("%.1f", yield)

// ═══════════════════════════════════════════════════════════
// ─── دکمه باز کردن ───────────────────────────────────────
// ═══════════════════════════════════════════════════════════

@Composable
fun IngredientCalculatorButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CalcOrange.copy(alpha = 0.15f))
            .border(1.dp, CalcOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null,
                tint = CalcOrange, modifier = Modifier.size(18.dp))
            AppText("ماشین حساب مواد", color = CalcOrange,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ─── Bottom Sheet ─────────────────────────────────────────
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientCalculatorSheet(
    ingredients: List<Ingredient>,
    baseYield: Int,
    onDismiss: () -> Unit
) {
    // فقط موادی که مقدار عددی دارن
    val selectableIngredients = remember(ingredients) {
        ingredients.filter { it.amount.toFloatOrNull() != null }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var newAmountText by remember { mutableStateOf("") }

    val selectedIngredient = selectableIngredients.getOrNull(selectedIndex)

    val ratio by remember(selectedIndex, newAmountText) {
        derivedStateOf {
            val orig = selectedIngredient?.amount?.toFloatOrNull() ?: return@derivedStateOf null
            val new  = newAmountText.toFloatOrNull()               ?: return@derivedStateOf null
            if (orig <= 0f) return@derivedStateOf null
            new / orig
        }
    }

    val newYield by remember(ratio) {
        derivedStateOf { ratio?.let { baseYield * it } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = CalcBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CalcTextLight.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp)
        ) {

            // ══════════════════════════════════════════════
            // ─── هدر ──────────────────────────────────────
            // ══════════════════════════════════════════════

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(CalcOrange.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null,
                            tint = CalcOrange, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        AppText("ماشین حساب مواد", color = CalcTextDark,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        AppText("بر اساس مقدار موجودت حساب کن",
                            color = CalcTextLight, fontSize = 12.sp)
                    }
                }
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape)
                        .background(CalcDivider).clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null,
                        tint = CalcTextLight, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ══════════════════════════════════════════════
            // ─── انتخاب ماده با Chip ──────────────────────
            // ══════════════════════════════════════════════

            AppText(
                "بر اساس کدام ماده حساب کنم؟",
                color = CalcTextDark, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectableIngredients.forEachIndexed { index, ing ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CalcOrange else CalcCard)
                            .border(
                                1.dp,
                                if (isSelected) CalcOrange else CalcDivider,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                selectedIndex = index
                                newAmountText = ""
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(13.dp)
                                )
                            }
                            // نام ماده فارسیه، Text معمولی
                            AppText(
                                ing.name,
                                color = if (isSelected) Color.White else CalcTextDark,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ══════════════════════════════════════════════
            // ─── فیلد ورودی ──────────────────────────────
            // ══════════════════════════════════════════════

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                AppText(
                    "من چقدر ${selectedIngredient?.name ?: "این ماده"} دارم؟",
                    color = CalcTextDark, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it },
                        placeholder = {
                            // placeholder = مقدار اصلی ماده به عنوان راهنما
                            selectedIngredient?.let { ing ->
                                LtrText(
                                    "${ing.amount} ${ing.unit}",
                                    color = CalcTextLight.copy(0.5f), fontSize = 13.sp
                                )
                            } ?: AppText("مقدار موجود",
                                color = CalcTextLight.copy(0.5f), fontSize = 13.sp)
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = CalcOrange,
                            unfocusedBorderColor    = CalcDivider,
                            focusedTextColor        = CalcTextDark,
                            unfocusedTextColor      = CalcTextDark,
                            cursorColor             = CalcOrange,
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = CalcCard,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    // واحد — LtrText چون ممکنه انگلیسی باشه (cup, tbsp, g, ...)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CalcCard)
                            .border(1.dp, CalcDivider, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        LtrText(
                            selectedIngredient?.unit ?: "",
                            color = CalcTextDark, fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // راهنمای مقدار اصلی زیر فیلد
                selectedIngredient?.let { ing ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("مقدار اصلی دستور:", color = CalcTextLight, fontSize = 11.sp)
                        LtrText(
                            "${ing.amount} ${ing.unit}",
                            color = CalcOrange, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // خطا
                if (newAmountText.isNotEmpty() && newAmountText.toFloatOrNull() == null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AppText("عدد معتبر وارد کن (مثلاً: 2 یا 1.5)",
                        color = Color(0xFFE53935), fontSize = 12.sp)
                }
            }

            // ══════════════════════════════════════════════
            // ─── نتیجه ────────────────────────────────────
            // ══════════════════════════════════════════════

            AnimatedVisibility(
                visible = ratio != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit  = fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = CalcDivider)
                    Spacer(modifier = Modifier.height(20.dp))

                    // ── کارت خلاصه نتیجه ─────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CalcGreenLight)
                            .border(1.dp, CalcGreen.copy(0.25f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                AppText("وعده جدید", color = CalcTextLight, fontSize = 11.sp)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppText(
                                        formatYield(newYield ?: 0f),
                                        color = CalcGreen,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 34.sp
                                    )
                                    AppText(
                                        "وعده",
                                        color = CalcGreen, fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                AppText("از $baseYield وعده اصلی",
                                    color = CalcTextLight, fontSize = 11.sp)
                            }

                            // ضریب تغییر — LtrText چون عدد انگلیسی
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CalcGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    LtrText(
                                        "×${String.format("%.2f", ratio ?: 1f)}",
                                        color = CalcGreen,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                AppText("ضریب تغییر", color = CalcTextLight, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AppText("مواد جدید", color = CalcTextDark,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // ── لیست مواد ────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CalcCard)
                            .animateContentSize()
                    ) {
                        ingredients.forEachIndexed { index, ingredient ->
                            val origAmount = ingredient.amount.toFloatOrNull()
                            val newAmt     = origAmount?.let { it * (ratio ?: 1f) }
                            val isSelected = selectableIngredients.getOrNull(selectedIndex) == ingredient

                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) CalcOrangeLight
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // نام ماده
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) CalcOrange else CalcDivider
                                                )
                                        )
                                        // نام ماده فارسیه، Text معمولی
                                        AppText(
                                            ingredient.name,
                                            color = if (isSelected) CalcOrange else CalcTextDark,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold
                                            else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // مقادیر
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // مقدار اصلی — LtrText چون عدد + واحد انگلیسی
                                        AppText(
                                            "${ingredient.amount} ${ingredient.unit}",
                                            color = CalcTextLight, fontSize = 12.sp
                                        )
                                        // فلش — LtrText تا جهتش برعکس نشه
                                        LtrText("←", color = CalcTextLight, fontSize = 12.sp)
                                        // مقدار جدید — LtrText چون عدد + واحد انگلیسی
                                        if (newAmt != null) {
                                            val fmt = if (newAmt == newAmt.toLong().toFloat())
                                                newAmt.toLong().toString()
                                            else String.format("%.1f", newAmt)

                                            AppText(
                                                "$fmt ${ingredient.unit}",
                                                color = if (isSelected) CalcOrange else CalcGreen,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            AppText(
                                                "${ingredient.amount} ${ingredient.unit}",
                                                color = CalcTextLight, fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                if (index < ingredients.lastIndex) {
                                    Divider(
                                        color = CalcDivider, thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    AppText(
                        "⚠️ این محاسبات تقریبی هستن. ممکنه زمان پخت هم تغییر کنه.",
                        color = CalcTextLight, fontSize = 11.sp, lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}