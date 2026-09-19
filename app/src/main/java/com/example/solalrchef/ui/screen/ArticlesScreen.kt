package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.SolarChefTopBar
import com.mnfarzaneh.solalrchef.viewmodel.ArticlesViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import com.mnfarzaneh.solalrchef.R

@Composable
fun ArticlesScreen(
    navController: NavController,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().background(GlassColors.BgLight)) {
        SolarChefTopBar(
            title = stringResource(R.string.articles_screen_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = { navController.popBackStack() }
        )

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GlassColors.AccentOrange)
            }
            state.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppText(state.error.orEmpty(), color = GlassColors.TextLight)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = viewModel::load,
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
                ) { AppText(stringResource(R.string.action_retry), color = androidx.compose.ui.graphics.Color.White) }
            }
            state.articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppText(stringResource(R.string.articles_empty), color = GlassColors.TextLight)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    AppText(
                        stringResource(R.string.articles_latest_subtitle),
                        color = GlassColors.TextLight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(state.articles, key = { it.id }) { article ->
                    ArticleListCard(article) {
                        navController.navigate(NavGraph.Screen.Article.createRoute(article.slug))
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleListCard(article: HeroArticleDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.GlassCard)
            .clickable(onClick = onClick)
    ) {
        if (article.imageUrl.isNotBlank()) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(170.dp)
            )
        }
        Column(Modifier.padding(16.dp)) {
            AppText(
                article.title,
                color = GlassColors.TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (article.summary.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                AppText(
                    article.summary,
                    color = GlassColors.TextLight,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            AppText(
                article.callToAction.ifBlank { stringResource(R.string.action_view_article) },
                color = GlassColors.AccentOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
