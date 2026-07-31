package com.mnfarzaneh.solalrchef.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

// ─── دکمه‌ی گرد شیشه‌ای (آیکون تنها) ──────────────────────
// اگه hazeState داده بشه، بلور واقعی نشون میده؛ وگرنه رنگ نیمه‌شفاف ساده (fallback قدیمی)
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    backgroundColor: Color = GlassColors.GlassWhite,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    val base = modifier
        .size(size)
        .clip(CircleShape)

    val styled = if (hazeState != null) {
        base.hazeEffect(state = hazeState) {
            blurRadius = 16.dp
        }
    } else {
        base.background(backgroundColor)
    }

    Box(
        modifier = styled.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}

// ─── دکمه‌ی کشیده با آیکون + برچسب (طرز تهیه / اشتراک‌گذاری و...) ──
@Composable
fun GlassActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    tint: Color = GlassColors.TextMid,
    height: androidx.compose.ui.unit.Dp = 58.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isPrimary)
                    Brush.linearGradient(listOf(GlassColors.AccentOrange, Color(0xFFFF8C42)))
                else
                    Brush.linearGradient(listOf(GlassColors.GlassCard, GlassColors.GlassCard))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon, contentDescription = label,
                tint = if (isPrimary) Color.White else tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            AppText(
                label,
                color = if (isPrimary) Color.White else GlassColors.TextMid,
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}

// ─── کارت شیشه‌ای پایه (برای استفاده‌ی عمومی به‌جای Box+background تکراری) ──
// اگه hazeState داده بشه، پشت کارت واقعاً بلور میشه؛ وگرنه رنگ نیمه‌شفاف ساده (fallback قدیمی)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clipped = modifier.clip(RoundedCornerShape(cornerRadius))

    val styled = if (hazeState != null) {
        clipped.hazeEffect(state = hazeState) {
            blurRadius = 20.dp
        }
    } else {
        clipped.background(GlassColors.GlassCard)
    }

    Box(
        modifier = if (onClick != null) styled.clickable { onClick() } else styled
    ) {
        content()
    }
}
