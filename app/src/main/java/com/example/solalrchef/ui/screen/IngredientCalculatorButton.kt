package com.mnfarzaneh.solalrchef.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.ALL_UNITS
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.MeasurementUnit
import com.mnfarzaneh.solalrchef.model.UnitCategory
import com.mnfarzaneh.solalrchef.model.findUnit
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

// ═══════════════════════════════════════════════════════════
// ─── رنگ‌ها ───────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════
private val CalcBg        get() = GlassColors.BgLight
private val CalcCard      get() = GlassColors.GlassCard
private val CalcOrange    get() = GlassColors.AccentOrange
private val CalcGreen     = Color(0xFF4CAF50)
private val CalcBlue      = Color(0xFF4A90D9)
private val CalcPurple    = Color(0xFF9C27B0)
private val CalcTextDark  get() = GlassColors.TextDark
private val CalcTextLight get() = GlassColors.TextLight
private val CalcDivider   get() = GlassColors.Divider
private val CalcRed       = Color(0xFFE53935)

// ═══════════════════════════════════════════════════════════
// ─── ضریب تبدیل به پایه ───────────────────────────────────
// ═══════════════════════════════════════════════════════════
private fun MeasurementUnit.toBaseValue(): Float = when (id) {
    "cup"   -> 240f;  "tbsp"  -> 15f;   "tsp"   -> 5f
    "ml"    -> 1f;    "l"     -> 1000f; "pinch" -> 0.3f
    "g"     -> 1f;    "kg"    -> 1000f; "oz"    -> 28.35f
    else    -> 1f
}

// ═══════════════════════════════════════════════════════════
// ─── جدول چگالی (g/ml) ────────────────────────────────────
// ═══════════════════════════════════════════════════════════
private val DENSITY_TABLE: Map<String, Float> = mapOf(
    "آرد"         to 0.50f, "آرد گندم"   to 0.50f,
    "آرد برنج"    to 0.58f, "نشاسته"     to 0.63f,
    "پودر کاکائو" to 0.42f, "شکر"        to 0.85f,
    "شکر قهوه‌ای" to 0.72f, "پودر قند"   to 0.56f,
    "عسل"         to 1.40f, "شیر"        to 1.03f,
    "خامه"        to 1.01f, "ماست"       to 1.05f,
    "کره"         to 0.91f, "روغن"       to 0.92f,
    "روغن زیتون"  to 0.91f, "برنج"       to 0.78f,
    "جو"          to 0.72f, "عدس"        to 0.78f,
    "نخود"        to 0.77f, "نمک"        to 1.22f,
    "جوش شیرین"   to 0.88f, "بکینگ پودر" to 0.72f,
    "وانیل"       to 0.88f, "آب"         to 1.00f,
    "سرکه"        to 1.01f, "آبلیمو"     to 1.03f,
)

private fun getDensity(name: String): Float? {
    val n = name.trim().lowercase()
    return DENSITY_TABLE.entries.firstOrNull {
        n.contains(it.key.lowercase()) || it.key.lowercase().contains(n)
    }?.value
}

// ═══════════════════════════════════════════════════════════
// ─── نمایش عدد هوشمند ─────────────────────────────────────
// ═══════════════════════════════════════════════════════════
private val FRACTIONS = listOf(
    0.125f to "⅛", (1f / 6f) to "⅙",
    0.25f to "¼", (1f / 3f) to "⅓", 0.5f to "½",
    (2f / 3f) to "⅔", 0.75f to "¾", 0.875f to "⅞",
)

private fun formatPreciseDecimal(value: Float): String {
    if (!value.isFinite()) return value.toString()
    if (value == 0f) return "0"

    val absolute = abs(value)
    val scale = when {
        absolute >= 100f -> 3
        absolute >= 1f -> 4
        absolute >= 0.01f -> 5
        else -> 6
    }
    val rounded = BigDecimal(value.toString())
        .setScale(scale, RoundingMode.HALF_UP)
        .stripTrailingZeros()

    // هیچ مقدار مثبتی صرفاً به‌خاطر نمایش به صفر تبدیل نمی‌شود.
    if (rounded.compareTo(BigDecimal.ZERO) == 0) {
        return if (value > 0f) "<0.000001" else ">-0.000001"
    }
    return rounded.toPlainString()
}

fun formatCookingAmount(amount: Float, unit: MeasurementUnit?): String {
    if (amount == 0f) return "0"
    // وزنی، شمارشی و واحدهای غیرقابل‌تبدیل → اعشار دقیق
    if (unit == null || unit.category != UnitCategory.VOLUME) {
        return formatPreciseDecimal(amount)
    }
    // حجمی → کسر زیبا
    val whole   = amount.toInt()
    val decimal = amount - whole
    if (abs(decimal) < 0.0005f) return whole.toString()
    if (abs(decimal - 1f) < 0.0005f) return (whole + 1).toString()
    val closest = FRACTIONS.minByOrNull { abs(it.first - decimal) }
    return if (closest != null && abs(closest.first - decimal) < 0.005f) {
        if (whole > 0) "$whole ${closest.second}" else closest.second
    } else formatPreciseDecimal(amount)
}

private fun formatYield(y: Float): String = formatPreciseDecimal(y)

// ═══════════════════════════════════════════════════════════
// ─── تبدیل بین واحدها ─────────────────────────────────────
// ═══════════════════════════════════════════════════════════
private fun convertAmount(
    amount: Float,
    fromUnit: MeasurementUnit,
    toUnit: MeasurementUnit,
    density: Float?
): Float? {
    if (fromUnit.id == toUnit.id) return amount
    if (!fromUnit.isConvertible || !toUnit.isConvertible) return null
    return when {
        fromUnit.category == UnitCategory.VOLUME && toUnit.category == UnitCategory.VOLUME ->
            amount * fromUnit.toBaseValue() / toUnit.toBaseValue()
        fromUnit.category == UnitCategory.WEIGHT && toUnit.category == UnitCategory.WEIGHT ->
            amount * fromUnit.toBaseValue() / toUnit.toBaseValue()
        fromUnit.category == UnitCategory.VOLUME && toUnit.category == UnitCategory.WEIGHT ->
            density?.let { amount * fromUnit.toBaseValue() * it / toUnit.toBaseValue() }
        fromUnit.category == UnitCategory.WEIGHT && toUnit.category == UnitCategory.VOLUME ->
            density?.let { amount * fromUnit.toBaseValue() / it / toUnit.toBaseValue() }
        else -> null
    }
}

// ═══════════════════════════════════════════════════════════
// ─── محاسبه نسبت ──────────────────────────────────────────
// ═══════════════════════════════════════════════════════════
private fun calculateRatio(
    origAmount: Float, origUnit: MeasurementUnit,
    newAmount: Float,  newUnit: MeasurementUnit,
    density: Float?
): Float? {
    if (origUnit.id == newUnit.id) {
        return if (origAmount <= 0f) null else newAmount / origAmount
    }
    if (!origUnit.isConvertible || !newUnit.isConvertible) return null
    // شمارشی → فقط تقسیم ساده
    if (origUnit.category == UnitCategory.COUNT || newUnit.category == UnitCategory.COUNT) {
        if (origUnit.id != newUnit.id) return null
        return if (origAmount <= 0f) null else newAmount / origAmount
    }
    // هم‌نوع → تبدیل به پایه مشترک
    if (origUnit.category == newUnit.category) {
        val origBase = origAmount * origUnit.toBaseValue()
        val newBase  = newAmount  * newUnit.toBaseValue()
        return if (origBase <= 0f) null else newBase / origBase
    }
    // متفاوت (حجم↔وزن) → نیاز به چگالی
    density ?: return null
    val origMl = when (origUnit.category) {
        UnitCategory.VOLUME -> origAmount * origUnit.toBaseValue()
        UnitCategory.WEIGHT -> origAmount * origUnit.toBaseValue() / density
        else -> return null
    }
    val newMl = when (newUnit.category) {
        UnitCategory.VOLUME -> newAmount * newUnit.toBaseValue()
        UnitCategory.WEIGHT -> newAmount * newUnit.toBaseValue() / density
        else -> return null
    }
    return if (origMl <= 0f) null else newMl / origMl
}


// ─── تبدیل هوشمند مقدار به Float ────────────────────────
fun String.toFloatSmart(): Float? {
    val s = this.trim()
        .replace('۰','0').replace('۱','1').replace('۲','2')
        .replace('۳','3').replace('۴','4').replace('۵','5')
        .replace('۶','6').replace('۷','7').replace('۸','8')
        .replace('۹','9')
        .replace('٠','0').replace('١','1').replace('٢','2')
        .replace('٣','3').replace('٤','4').replace('٥','5')
        .replace('٦','6').replace('٧','7').replace('٨','8')
        .replace('٩','9')
        .replace('٫', '.')
        .replace(',', '.')
        .replace("نصف", "0.5")
        .replace("یک دوم", "0.5")
        .replace("یک چهارم", "0.25")
        .replace("سه چهارم", "0.75")
        .replace("یک سوم", "0.33")
        .replace("دو سوم", "0.67")

    // ← "به مقدار لازم" → null
    if (s.contains("لازم") || s.contains("دلخواه") || s == "0") return null

    val rangeRegex = Regex("""(\d+\.?\d*)\s*(تا|-|to)\s*(\d+\.?\d*)""")
    // بازه یک عدد قطعی نیست؛ انتخاب خودکار عدد اول نتیجه محاسبه را گمراه‌کننده می‌کرد.
    if (rangeRegex.containsMatchIn(s)) return null

    s.toFloatOrNull()?.let { return it }

    val numRegex = Regex("""(\d+\.?\d*)""")
    numRegex.find(s)?.let { return it.groupValues[1].toFloatOrNull() }

    return null
}
// ═══════════════════════════════════════════════════════════
// ─── همه معادل‌ها ─────────────────────────────────────────
// ═══════════════════════════════════════════════════════════
data class AllEquivalents(
    val cup: String?, val tbsp: String?, val tsp: String?,
    val ml: String?,  val gram: String?,
    val hasDensity: Boolean = false
)

private fun calculateAllEquivalents(
    amount: Float,
    fromUnit: MeasurementUnit,
    ingredientName: String,
    customDensity: Float?
): AllEquivalents {
    // شمارشی و واحد محاوره‌ای → معادل دقیقی ندارد
    if (fromUnit.category == UnitCategory.COUNT || !fromUnit.isConvertible)
        return AllEquivalents(null, null, null, null, null)

    val density = customDensity ?: getDensity(ingredientName)

    fun conv(toId: String): Float? {
        val toUnit = findUnit(toId) ?: return null
        return convertAmount(amount, fromUnit, toUnit, density)
    }
    fun fmt(v: Float?, unit: MeasurementUnit, label: String): String? =
        v?.let { "${formatCookingAmount(it, unit)} $label" }

    return AllEquivalents(
        cup  = fmt(conv("cup"),  findUnit("cup")!!,  "پیمانه"),
        tbsp = fmt(conv("tbsp"), findUnit("tbsp")!!, "ق غ"),
        tsp  = fmt(conv("tsp"),  findUnit("tsp")!!,  "ق چ"),
        ml   = fmt(conv("ml"),   findUnit("ml")!!,   "ml"),
        gram = fmt(conv("g"),    findUnit("g")!!,    "گرم"),
        hasDensity = density != null
    )
}

// ═══════════════════════════════════════════════════════════
// ─── دکمه باز کردن ────────────────────────────────────────
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
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Calculate, null, tint = CalcOrange, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.calculator_title), color = CalcOrange,
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
    val selectableIngredients = ingredients.filter {
        it.amount.toFloatSmart() != null && it.amount.isNotBlank()
    }

    var selectedIndex     by remember { mutableIntStateOf(0) }
    var newAmountText     by remember { mutableStateOf("") }
    var selectedInputUnit by remember { mutableStateOf<MeasurementUnit?>(null) }
    var customDensity     by remember { mutableStateOf<Float?>(null) }
    var customDensityText by remember { mutableStateOf("") }
    var showDensityInput  by remember { mutableStateOf(false) }
    var showIngDropdown   by remember { mutableStateOf(false) }
    var showInputUnitMenu by remember { mutableStateOf(false) }
    var expandedIndex     by remember { mutableStateOf<Int?>(null) }

    val selectedIngredient = selectableIngredients.getOrNull(selectedIndex)
    val ingredientUnit     = selectedIngredient?.let { findUnit(it.unit) }
    // واحد ورودی: پیش‌فرض همون واحد ماده، کاربر میتونه عوض کنه
    val effectiveInputUnit = selectedInputUnit ?: ingredientUnit
    val density = customDensity ?: getDensity(selectedIngredient?.name ?: "")
    val compatibleInputUnits = remember(ingredientUnit) {
        when {
            ingredientUnit == null -> emptyList()
            !ingredientUnit.isConvertible -> listOf(ingredientUnit)
            ingredientUnit.category == UnitCategory.COUNT -> listOf(ingredientUnit)
            else -> ALL_UNITS.filter {
                it.isConvertible && it.category != UnitCategory.COUNT
            }
        }
    }

    // ── نسبت ──────────────────────────────────────────────
    val ratio by remember(selectedIndex, newAmountText, effectiveInputUnit, density) {
        derivedStateOf {
            // ← اصلاح شد: toFloatOrNull() به toFloatSmart() تغییر کرد تا مقادیری
            // مثل "9 تا 12" هم درست خونده بشن (قبلاً اینجا همیشه null برمی‌گشت)
            val origAmt  = selectedIngredient?.amount?.toFloatSmart() ?: return@derivedStateOf null
            val newAmt   = newAmountText.toFloatSmart()                 ?: return@derivedStateOf null
            val origUnit = ingredientUnit                              ?: return@derivedStateOf null
            val inUnit   = effectiveInputUnit                          ?: return@derivedStateOf null
            calculateRatio(origAmt, origUnit, newAmt, inUnit, density)
        }
    }
    val newYield = ratio?.let { baseYield * it }

    // آیا برای محاسبه نسبت چگالی لازمه؟
    val needsDensityForRatio = effectiveInputUnit != null && ingredientUnit != null &&
            effectiveInputUnit.category != ingredientUnit.category &&
            effectiveInputUnit.category != UnitCategory.COUNT &&
            ingredientUnit.category != UnitCategory.COUNT &&
            effectiveInputUnit.isConvertible && ingredientUnit.isConvertible

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = CalcBg,
        dragHandle = {
            Box(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                .width(40.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CalcTextLight.copy(0.4f)))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── هدر ──────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(CalcOrange.copy(0.15f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Calculate, null, tint = CalcOrange,
                            modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(stringResource(R.string.calculator_title), color = CalcTextDark,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.calculator_subtitle), color = CalcTextLight, fontSize = 12.sp)
                    }
                }
                Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(CalcDivider).clickable { onDismiss() },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, null, tint = CalcTextLight, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // راهنما
            Box(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CalcOrange.copy(0.08f))
                .border(1.dp, CalcOrange.copy(0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp)) {
                Text(
                    stringResource(R.string.calculator_help),
                    color = CalcTextDark, fontSize = 13.sp, lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── انتخاب ماده ──────────────────────────────
            Text(stringResource(R.string.calculator_base_ingredient),
                color = CalcTextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CalcCard)
                .border(1.dp, CalcDivider, RoundedCornerShape(12.dp))
                .clickable { showIngDropdown = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedIngredient?.let {
                            // ← اصلاح شد: toFloatSmart() به‌جای toFloatOrNull()
                            // (که "9 تا 12" رو "0" نشون می‌داد)
                            "${it.name}  (" +
                                    "${formatCookingAmount(it.amount.toFloatSmart() ?: 0f, ingredientUnit)}" +
                                    " ${it.unit})"
                        } ?: stringResource(R.string.calculator_select),
                        color = CalcTextDark, fontSize = 14.sp
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = CalcTextLight)
                }
                DropdownMenu(expanded = showIngDropdown,
                    onDismissRequest = { showIngDropdown = false }) {
                    selectableIngredients.forEachIndexed { idx, ing ->
                        val u = findUnit(ing.unit)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    // ← اصلاح شد: toFloatSmart() به‌جای toFloatOrNull()
                                    "${ing.name}  (${formatCookingAmount(
                                        ing.amount.toFloatSmart() ?: 0f, u)} ${ing.unit})",
                                    color = CalcTextDark
                                )
                            },
                            onClick = {
                                selectedIndex = idx
                                newAmountText = ""
                                selectedInputUnit = null
                                customDensity = null
                                customDensityText = ""
                                showDensityInput = false
                                expandedIndex = null
                                showIngDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── ورود مقدار + واحد ────────────────────────
            // کاربر میتونه واحد رو عوض کنه (مثلاً ماده با پیمانه ثبت شده، ولی کاربر گرم داره)
            Text(
                stringResource(
                    R.string.calculator_available_amount,
                    selectedIngredient?.name ?: stringResource(R.string.calculator_this_ingredient)
                ),
                color = CalcTextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {

                OutlinedTextField(
                    value = newAmountText,
                    onValueChange = { newAmountText = it },
                    placeholder = { Text(stringResource(R.string.calculator_amount_hint), color = CalcTextLight.copy(0.5f), fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalcOrange, unfocusedBorderColor = CalcDivider,
                        focusedTextColor = CalcTextDark, unfocusedTextColor = CalcTextDark,
                        cursorColor = CalcOrange),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )

                // ← واحد ورودی (قابل تغییر)
                Box {
                    Box(modifier = Modifier.height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalcCard)
                        .border(1.dp, CalcOrange.copy(0.5f), RoundedCornerShape(12.dp))
                        .clickable { showInputUnitMenu = true }
                        .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(effectiveInputUnit?.displayName ?: stringResource(R.string.calculator_unit_hint),
                                color = CalcOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowDown, null,
                                tint = CalcOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(expanded = showInputUnitMenu,
                        onDismissRequest = { showInputUnitMenu = false }) {
                        compatibleInputUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u.displayName, color = CalcTextDark) },
                                onClick = { selectedInputUnit = u; showInputUnitMenu = false }
                            )
                        }
                    }
                }
            }

            // ── کسرهای سریع (فقط برای واحدهای حجمی) ──────
            if (effectiveInputUnit?.category == UnitCategory.VOLUME &&
                effectiveInputUnit.isConvertible) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.calculator_fraction), color = CalcTextLight, fontSize = 11.sp)
                    listOf("¼" to 0.25f, "⅓" to 0.333f, "½" to 0.5f,
                        "⅔" to 0.667f, "¾" to 0.75f).forEach { (frac, value) ->
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CalcOrange.copy(0.12f))
                            .border(1.dp, CalcOrange.copy(0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                val cur = newAmountText.toFloatOrNull()
                                newAmountText = if (cur != null && cur > 0f)
                                    String.format("%.3f", cur + value).trimEnd('0').trimEnd('.')
                                else value.toString().trimEnd('0').trimEnd('.')
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(frac, color = CalcOrange,
                                fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── چگالی (فقط وقتی واحد ورودی با واحد ماده فرق داره) ──
            if (needsDensityForRatio) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CalcBlue.copy(0.08f))
                    .border(1.dp, CalcBlue.copy(0.2f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        val autoDensity = getDensity(selectedIngredient?.name ?: "")
                        Text(
                            if (autoDensity != null) stringResource(R.string.calculator_auto_density, autoDensity)
                            else stringResource(R.string.calculator_density_missing),
                            color = CalcBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                        customDensity?.let { density ->
                            Text(stringResource(R.string.calculator_custom_density, density),
                                color = CalcGreen, fontSize = 11.sp)
                        }
                        if (autoDensity == null && customDensity == null)
                            Text(stringResource(R.string.calculator_density_required),
                                color = CalcRed, fontSize = 11.sp)
                    }
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(CalcBlue.copy(0.15f))
                        .clickable { showDensityInput = !showDensityInput },
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint = CalcBlue, modifier = Modifier.size(16.dp))
                    }
                }
                if (showDensityInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customDensityText,
                            onValueChange = { customDensityText = it; customDensity = it.toFloatOrNull() },
                            label = { Text(stringResource(R.string.calculator_density_label), color = CalcTextLight, fontSize = 11.sp) },
                            placeholder = { Text(stringResource(R.string.calculator_density_example),
                                color = CalcTextLight.copy(0.5f)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalcBlue, unfocusedBorderColor = CalcDivider,
                                focusedTextColor = CalcTextDark, unfocusedTextColor = CalcTextDark),
                            shape = RoundedCornerShape(12.dp), singleLine = true
                        )
                        TextButton(onClick = {
                            customDensity = null; customDensityText = ""; showDensityInput = false
                        }) {
                            Text(stringResource(R.string.action_reset), color = CalcRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════
            // ─── نتیجه ─────────────────────────────────────
            // ═══════════════════════════════════════════════
            if (ratio != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = CalcDivider)
                Spacer(modifier = Modifier.height(16.dp))

                // وعده
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalcGreen.copy(0.1f))
                    .border(1.dp, CalcGreen.copy(0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(R.string.calculator_final_output), color = CalcTextLight, fontSize = 12.sp)
                        Text(stringResource(R.string.calculator_yield_change, baseYield, formatYield(newYield ?: 0f)),
                            color = CalcGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("×${formatPreciseDecimal(ratio ?: 0f)}",
                        color = CalcGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.calculator_new_ingredients), color = CalcTextDark,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.calculator_equivalents_hint), color = CalcPurple, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalcCard)
                    .animateContentSize()) {

                    ingredients.forEachIndexed { index, ingredient ->
                        val origAmount = ingredient.amount.toFloatSmart()
                        val ingUnit    = findUnit(ingredient.unit)
                        val isSelected = selectableIngredients.getOrNull(selectedIndex) == ingredient
                        val isExpanded = expandedIndex == index
                        // مقدار جدید با همون واحد اصلی
                        val newAmount   = origAmount?.let { it * (ratio ?: 1f) }
                        // آیا این ماده قابل expand هست؟
                        // شمارشی → expand نداره (چون معادلی نداره)
                        val canExpand  = newAmount != null && ingUnit != null &&
                                ingUnit.category != UnitCategory.COUNT && ingUnit.isConvertible

                        Column(modifier = Modifier.fillMaxWidth()
                            .background(if (isSelected) CalcOrange.copy(0.08f) else Color.Transparent)) {

                            Row(modifier = Modifier.fillMaxWidth()
                                .clickable(enabled = canExpand) {
                                    expandedIndex = if (isExpanded) null else index
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {

                                // نام ماده
                                Row(modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (isSelected) Box(modifier = Modifier.size(6.dp)
                                        .clip(CircleShape).background(CalcOrange))
                                    Text(ingredient.name,
                                        color = if (isSelected) CalcOrange else CalcTextDark,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                }

                                // مقدار قبل ← بعد (با همون واحد اصلی)
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (origAmount != null && newAmount != null) {
                                        Text(
                                            "${formatCookingAmount(origAmount, ingUnit)} ${ingredient.unit}",
                                            color = CalcTextLight,
                                            fontSize = 12.sp
                                        )

                                        Text(
                                            "←",
                                            color = CalcTextLight,
                                            fontSize = 11.sp
                                        )

                                        Text(
                                            // ← خروجی با همان واحد اصلی ماده
                                            "${formatCookingAmount(newAmount, ingUnit)} ${ingredient.unit}",
                                            color = if (isSelected) CalcOrange else CalcGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (canExpand) {
                                            Icon(
                                                if (isExpanded) Icons.Default.ExpandLess
                                                else Icons.Default.ExpandMore,
                                                null,
                                                tint = CalcPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else if (
                                        ingredient.amount.contains("لازم") ||
                                        ingredient.amount.contains("دلخواه")
                                    ) {
                                        Text(
                                            "${ingredient.name}: ${ingredient.amount}",
                                            color = CalcTextLight,
                                            fontSize = 13.sp,
                                            fontStyle = FontStyle.Italic
                                        )
                                    } else {
                                        Text(
                                            "${ingredient.amount} ${ingredient.unit}",
                                            color = CalcTextLight,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // ── معادل‌ها ──────────────────
                            AnimatedVisibility(
                                visible = isExpanded && canExpand,
                                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                                exit  = shrinkVertically(tween(200)) + fadeOut(tween(200))
                            ) {
                                if (newAmount != null && ingUnit != null) {
                                    val ingDensity = customDensity ?: getDensity(ingredient.name)
                                    val eq = calculateAllEquivalents(
                                        newAmount, ingUnit, ingredient.name, ingDensity
                                    )
                                    Column(modifier = Modifier.fillMaxWidth()
                                        .background(CalcPurple.copy(0.05f))
                                        .padding(start = 28.dp, end = 14.dp,
                                            bottom = 12.dp, top = 4.dp)) {
                                        Text(stringResource(R.string.calculator_all_equivalents), color = CalcPurple,
                                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 6.dp))
                                        // حجمی
                                        if (eq.cup != null || eq.tbsp != null || eq.tsp != null) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                eq.cup?.let  { CalcChip(it, CalcBlue) }
                                                eq.tbsp?.let { CalcChip(it, CalcBlue) }
                                                eq.tsp?.let  { CalcChip(it, CalcBlue) }
                                            }
                                        }
                                        // وزنی + ml
                                        if (eq.ml != null || eq.gram != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                eq.ml?.let   { CalcChip(it, CalcGreen) }
                                                eq.gram?.let { CalcChip(it, CalcGreen) }
                                            }
                                        }
                                        if (!eq.hasDensity &&
                                            ingUnit.category == UnitCategory.VOLUME) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(stringResource(R.string.calculator_gram_density_required),
                                                color = CalcRed, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (index < ingredients.lastIndex)
                            Divider(color = CalcDivider, thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.calculator_approximation_warning),
                    color = CalcTextLight, fontSize = 11.sp, lineHeight = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CalcChip(text: String, color: Color) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(color.copy(alpha = 0.12f))
        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
