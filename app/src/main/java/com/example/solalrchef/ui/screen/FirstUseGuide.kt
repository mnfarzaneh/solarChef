package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors

private data class GuidePage(
    val icon: String,
    val title: String,
    val description: String
)

@Composable
fun FirstUseGuide(onFinished: () -> Unit) {
    val pages = listOf(
            GuidePage(
                icon = "👋",
                title = stringResource(R.string.guide_welcome_title),
                description = stringResource(R.string.guide_welcome_description)
            ),
            GuidePage(
                icon = "✨",
                title = stringResource(R.string.guide_add_title),
                description = stringResource(R.string.guide_add_description)
            ),
            GuidePage(
                icon = "❤️",
                title = stringResource(R.string.guide_cooking_title),
                description = stringResource(R.string.guide_cooking_description)
            ),
            GuidePage(
                icon = "☁️",
                title = stringResource(R.string.guide_backup_title),
                description = stringResource(R.string.guide_backup_description)
            )
        )
    var pageIndex by remember { mutableIntStateOf(0) }
    val page = pages[pageIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB329160F))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(GlassColors.BgLight.copy(alpha = 0.98f))
                .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFinished) {
                    AppText(stringResource(R.string.action_skip), color = GlassColors.TextLight, fontSize = 13.sp)
                }
                AppText(
                    stringResource(R.string.guide_title),
                    color = GlassColors.TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(GlassColors.AccentOrange.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                AppText(page.icon, fontSize = 42.sp)
            }
            Spacer(Modifier.height(20.dp))
            AppText(
                page.title,
                color = GlassColors.TextDark,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            AppText(
                page.description,
                color = GlassColors.TextLight,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 25.sp
            )
            Spacer(Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pageIndex) 22.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pageIndex) GlassColors.AccentOrange
                                else GlassColors.Divider
                            )
                    )
                }
            }
            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pageIndex > 0) {
                    TextButton(onClick = { pageIndex-- }) {
                        AppText(stringResource(R.string.action_previous), color = GlassColors.TextLight, fontSize = 13.sp)
                    }
                }
                Button(
                    onClick = {
                        if (pageIndex < pages.lastIndex) pageIndex++ else onFinished()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
                ) {
                    AppText(
                        if (pageIndex == pages.lastIndex) stringResource(R.string.action_start)
                        else stringResource(R.string.action_next),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
