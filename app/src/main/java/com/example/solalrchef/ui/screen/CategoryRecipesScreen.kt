package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.mnfarzaneh.solalrchef.R
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.CategoryRecipesViewModel

@Composable
fun CategoryRecipesScreen(
    categoryName: String,
    navController: NavController
) {
    val viewModel: CategoryRecipesViewModel = hiltViewModel()
    val recipes by viewModel.recipes.collectAsState()
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.BgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── هدر ──────────────────────────────────────
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
                        .background(GlassColors.GlassWhite)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back),
                        tint = GlassColors.TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                AppText(
                    text = categoryName,
                    color = GlassColors.TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText("🍽️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        AppText(stringResource(R.string.category_empty), color = GlassColors.TextLight)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        HomeRecipeCard(
                            recipe = recipe,
                            onClick = {
                                navController.navigate(
                                    NavGraph.Screen.RecipeDetail.createRoute(recipe.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
