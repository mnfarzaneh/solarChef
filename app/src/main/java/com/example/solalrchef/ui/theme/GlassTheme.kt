package com.mnfarzaneh.solalrchef.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppAccent(val storageKey: String, val color: Color, val secondary: Color) {
    ORANGE("orange", Color(0xFFFF6B35), Color(0xFFFF9A5C)),
    SAFFRON("saffron", Color(0xFFE9A400), Color(0xFFFFC94A)),
    GREEN("green", Color(0xFF3E9B68), Color(0xFF75C796)),
    PINK("pink", Color(0xFFD94F7D), Color(0xFFF08CAB));

    companion object {
        fun fromStorageKey(key: String?): AppAccent = entries.firstOrNull { it.storageKey == key } ?: ORANGE
    }
}

// ─── پالت رنگ مرکزی گلسمورفیزم ────────────────────────────
object GlassColors {
    var activeAccent by mutableStateOf(AppAccent.ORANGE)
        private set
    var darkMode by mutableStateOf(false)
        private set

    fun setAppearance(accent: AppAccent = activeAccent, isDark: Boolean = darkMode) {
        activeAccent = accent
        darkMode = isDark
    }

    val BgLight get() = if (darkMode) Color(0xFF171310) else Color(0xFFF5EFE6)
    val GlassCard get() = if (darkMode) Color(0xD92A231F) else Color(0xAAFFFFFF)
    val GlassWhite get() = if (darkMode) Color(0xEE362D28) else Color(0xCCFFFFFF)
    val AccentOrange get() = activeAccent.color
    val AccentSecondary get() = activeAccent.secondary
    val AccentGreen   = Color(0xFF4CAF50)
    val AccentBlue    = Color(0xFF4A90D9)
    val TextDark get() = if (darkMode) Color(0xFFFFF7F1) else Color(0xFF2C1810)
    val TextMid get() = if (darkMode) Color(0xFFD9C3B7) else Color(0xFF6B4C3B)
    val TextLight get() = if (darkMode) Color(0xFFA98F82) else Color(0xFF9E7B6A)
    val Divider get() = if (darkMode) Color(0x33FFFFFF) else Color(0x33000000)
}

// ─── Modifier کمکی برای کارت شیشه‌ای ──────────────────────
fun Modifier.glassCard(cornerRadius: Int = 16): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(GlassColors.GlassCard)
