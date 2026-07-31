package com.mnfarzaneh.solalrchef.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─── پالت رنگ مرکزی گلسمورفیزم ────────────────────────────
object GlassColors {
    val BgLight       = Color(0xFFF5EFE6)
    val GlassCard     = Color(0xAAFFFFFF)
    val GlassWhite    = Color(0xCCFFFFFF)
    val AccentOrange  = Color(0xFFFF6B35)
    val AccentGreen   = Color(0xFF4CAF50)
    val AccentBlue    = Color(0xFF4A90D9)
    val TextDark      = Color(0xFF2C1810)
    val TextMid       = Color(0xFF6B4C3B)
    val TextLight     = Color(0xFF9E7B6A)
    val Divider       = Color(0x33000000)
}

// ─── Modifier کمکی برای کارت شیشه‌ای ──────────────────────
fun Modifier.glassCard(cornerRadius: Int = 16): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(GlassColors.GlassCard)