package com.mnfarzaneh.solalrchef.model

// ─── دسته‌بندی واحدهای اندازه‌گیری ────────────────────────
enum class UnitCategory {
    WEIGHT,   // وزنی (گرم، کیلوگرم)
    VOLUME,   // حجمی (پیمانه، قاشق، میلی‌لیتر)
    COUNT     // شمارشی (عدد، حبه)
}

data class MeasurementUnit(
    val id: String,          // شناسه‌ی کوتاه انگلیسی، مثلاً "tbsp"
    val displayName: String, // نام فارسی که نمایش داده می‌شه
    val category: UnitCategory,
    // واحدهای محاوره‌ای مثل لیوان و مشت نباید با ضریب حدسی تبدیل شوند.
    val isConvertible: Boolean = true
)

// این فهرست کوتاه در منوی اولیه نمایش داده می‌شود تا انتخاب واحد شلوغ نشود.
val PRIMARY_UNITS = listOf(
    MeasurementUnit("g", "گرم", UnitCategory.WEIGHT),
    MeasurementUnit("kg", "کیلوگرم", UnitCategory.WEIGHT),
    MeasurementUnit("ml", "میلی‌لیتر", UnitCategory.VOLUME),
    MeasurementUnit("l", "لیتر", UnitCategory.VOLUME),
    MeasurementUnit("tsp", "قاشق چای‌خوری", UnitCategory.VOLUME),
    MeasurementUnit("tbsp", "قاشق غذاخوری", UnitCategory.VOLUME),
    MeasurementUnit("cup", "پیمانه", UnitCategory.VOLUME),
    MeasurementUnit("pinch", "نوک قاشقی", UnitCategory.VOLUME),
    MeasurementUnit("piece", "عدد", UnitCategory.COUNT),
)

// واحدهای رایج در آشپزی فارسی؛ فقط با انتخاب «واحدهای بیشتر» دیده می‌شوند.
val EXTENDED_UNITS = listOf(
    MeasurementUnit("glass", "لیوان", UnitCategory.VOLUME, false),
    MeasurementUnit("tea_glass", "استکان", UnitCategory.VOLUME, false),
    MeasurementUnit("coffee_cup", "فنجان", UnitCategory.VOLUME, false),
    MeasurementUnit("dessert_spoon", "قاشق مرباخوری", UnitCategory.VOLUME, false),
    MeasurementUnit("spoon", "قاشق", UnitCategory.VOLUME, false),
    MeasurementUnit("clove", "حبه", UnitCategory.COUNT, false),
    MeasurementUnit("sprig", "شاخه", UnitCategory.COUNT, false),
    MeasurementUnit("leaf", "برگ", UnitCategory.COUNT, false),
    MeasurementUnit("seed", "دانه", UnitCategory.COUNT, false),
    MeasurementUnit("bunch", "دسته", UnitCategory.COUNT, false),
    MeasurementUnit("handful", "مشت", UnitCategory.COUNT, false),
    MeasurementUnit("pack", "بسته", UnitCategory.COUNT, false),
    MeasurementUnit("can", "قوطی", UnitCategory.COUNT, false),
    MeasurementUnit("slice", "برش", UnitCategory.COUNT, false),
    MeasurementUnit("piece_part", "تکه", UnitCategory.COUNT, false),
    MeasurementUnit("drop", "قطره", UnitCategory.COUNT, false),
)

// این‌ها در منو نمایش داده نمی‌شوند، اما داده استخراج‌شده با این واحدها شناخته می‌شود.
private val RECOGNIZED_UNITS = listOf(
    MeasurementUnit("bulb", "بوته", UnitCategory.COUNT, false),
    MeasurementUnit("palm", "کف دست", UnitCategory.COUNT, false),
    MeasurementUnit("finger", "بند انگشت", UnitCategory.COUNT, false),
    MeasurementUnit("block", "قالب", UnitCategory.COUNT, false),
    MeasurementUnit("sheet", "ورق", UnitCategory.COUNT, false),
    MeasurementUnit("pod", "پر", UnitCategory.COUNT, false),
)

val ALL_UNITS = PRIMARY_UNITS + EXTENDED_UNITS + RECOGNIZED_UNITS

// ← هم با id (مثل "tbsp") هم با نام فارسی (مثل "قاشق غذاخوری") قابل جستجوئه.
// علاوه بر این، چندتا مترادف/مخفف رایج (مثل "ق.چ") رو هم می‌شناسه —
// برای وقتی که کاربر یا داده‌ی قدیمی به‌جای اسم کامل، مخفف نوشته.
private val unitAliases: Map<String, String> = mapOf(
    "ق.چ" to "tsp", "ق چ" to "tsp", "قاشق چایخوری" to "tsp",
    "ق.غ" to "tbsp", "ق غ" to "tbsp", "قاشق غذاخوری" to "tbsp",
    "میلی لیتر" to "ml", "میلی‌لیتر" to "ml",
    "گرم" to "g", "کیلو" to "kg", "کیلوگرم" to "kg",
    "لیتر" to "l", "پیمانه" to "cup",
    "عدد" to "piece", "نوک قاشقی" to "pinch",
    "فنجان" to "coffee_cup", "لیوان" to "glass", "استکان" to "tea_glass",
    "قاشق مربا خوری" to "dessert_spoon", "قاشق مرباخوری" to "dessert_spoon",
    "قاشق سوپخوری" to "tbsp", "قاشق" to "spoon",
    "حبه" to "clove", "شاخه" to "sprig", "برگ" to "leaf", "دانه" to "seed",
    "دسته" to "bunch", "مشت" to "handful", "بسته" to "pack", "قوطی" to "can",
    "برش" to "slice", "تکه" to "piece_part", "قطره" to "drop",
    "بوته" to "bulb", "کف دست" to "palm", "بند انگشت" to "finger",
    "قالب" to "block", "ورق" to "sheet", "پر" to "pod",
)

fun findUnit(idOrDisplayName: String): MeasurementUnit? {
    val trimmed = idOrDisplayName.trim()
    ALL_UNITS.firstOrNull { it.id == trimmed || it.displayName == trimmed }?.let { return it }
    val aliasedId = unitAliases[trimmed] ?: return null
    return ALL_UNITS.firstOrNull { it.id == aliasedId }
}
