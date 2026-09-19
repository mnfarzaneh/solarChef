package com.mnfarzaneh.solalrchef.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.mnfarzaneh.solalrchef.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.data.remote.dto.ArticleContentDto
import com.mnfarzaneh.solalrchef.viewmodel.ArticleViewModel

@Composable
fun ArticleScreen(
    slug: String,
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(slug) { viewModel.load(slug) }

    Box(Modifier.fillMaxSize().background(GlassColors.BgLight)) {
        when {
            state.loading -> CircularProgressIndicator(
                color = GlassColors.AccentOrange,
                modifier = Modifier.align(Alignment.Center)
            )
            state.error != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppText(stringResource(R.string.article_connection_failed), color = GlassColors.TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                AppText(state.error.orEmpty(), color = GlassColors.TextLight)
                Button(
                    onClick = { viewModel.load(slug) },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    AppText(stringResource(R.string.action_retry), color = Color.White)
                }
            }
            state.article != null -> {
                val article = state.article!!
                val content = article.content ?: ArticleContentDto()
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (article.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = article.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(300.dp)
                        )
                    } else Spacer(Modifier.height(110.dp))

                    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp)) {
                        AppText(article.title, color = GlassColors.TextDark, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        if (article.summary.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            AppText(article.summary, color = GlassColors.TextLight, fontSize = 15.sp)
                        }
                        Spacer(Modifier.height(22.dp))
                        val details = listOf(
                            stringResource(R.string.article_detail_prep) to content.prepTime,
                            stringResource(R.string.article_detail_cook) to content.cookTime,
                            stringResource(R.string.article_detail_servings) to content.servings,
                            stringResource(R.string.article_detail_difficulty) to content.difficulty
                        ).filter { it.second.isNotBlank() }
                        if (details.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                details.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { (label, value) ->
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(78.dp)
                                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                                    .background(GlassColors.GlassCard)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                AppText(label, color = GlassColors.TextLight, fontSize = 11.sp, maxLines = 1)
                                                Spacer(Modifier.height(3.dp))
                                                AppText(value, color = GlassColors.TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(Modifier.weight(1f).height(78.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                        AppText(article.body, color = GlassColors.TextDark, fontSize = 16.sp, lineHeight = 29.sp)
                        if (content.ingredients.isNotEmpty()) {
                            Spacer(Modifier.height(28.dp)); AppText(stringResource(R.string.article_ingredients), color = GlassColors.TextDark, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            content.ingredients.forEach { item -> AppText(stringResource(R.string.article_ingredient_line, item.name, item.amount, item.unit), color = GlassColors.TextDark, fontSize = 16.sp, lineHeight = 28.sp) }
                        }
                        if (content.equipment.isNotEmpty()) {
                            Spacer(Modifier.height(28.dp)); AppText(stringResource(R.string.article_equipment), color = GlassColors.TextDark, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            AppText(content.equipment.joinToString("، "), color = GlassColors.TextDark, fontSize = 16.sp, lineHeight = 28.sp)
                        }
                        if (content.steps.isNotEmpty()) {
                            Spacer(Modifier.height(28.dp)); AppText(stringResource(R.string.article_steps), color = GlassColors.TextDark, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            content.steps.forEachIndexed { index, step ->
                                Spacer(Modifier.height(14.dp)); AppText(stringResource(R.string.article_step_number, index + 1, step.title), color = GlassColors.TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                AppText(step.text, color = GlassColors.TextDark, fontSize = 16.sp, lineHeight = 28.sp)
                                if (step.imageUrl.isNotBlank()) AsyncImage(model = step.imageUrl, contentDescription = step.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)))
                            }
                        }
                        content.sections.forEach { section ->
                            Spacer(Modifier.height(28.dp)); if (section.heading.isNotBlank()) AppText(section.heading, color = GlassColors.TextDark, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            if (section.text.isNotBlank()) AppText(section.text, color = GlassColors.TextDark, fontSize = 16.sp, lineHeight = 28.sp)
                            if (section.imageUrl.isNotBlank()) AsyncImage(model = section.imageUrl, contentDescription = section.heading, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(240.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)))
                        }
                        Spacer(Modifier.height(60.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp, start = 16.dp)
                .clip(CircleShape)
                .background(GlassColors.GlassWhite)
                .clickable { navController.popBackStack() }
                .padding(11.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowForward, "بازگشت", tint = GlassColors.TextDark, modifier = Modifier.size(22.dp))
        }

        if (state.article != null) {
            val article = state.article!!
            Row(
                modifier = Modifier
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(GlassColors.GlassWhite)
                    .clickable {
                        val publicUrl = "https://solarchef.ir/articles/${Uri.encode(article.slug)}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, article.title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${article.title}\n\n${article.summary}\n\n$publicUrl"
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "اشتراک‌گذاری مقاله")
                        )
                    }
                    .padding(11.dp)
                    .align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Share, "اشتراک‌گذاری", tint = GlassColors.TextDark, modifier = Modifier.size(22.dp))
            }
        }
    }
}
