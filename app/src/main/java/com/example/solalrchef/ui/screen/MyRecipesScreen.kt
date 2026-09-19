package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.model.Recipe
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.MyRecipesViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
// ─── رنگ‌ها ───────────────────────────────────────────────

// همون مقادیر نویگیشن‌بار شناور که توی HomeScreen.kt تعریف شده (اونجا private‌ه،
// پس اینجا دوباره تعریفش می‌کنیم؛ چون این صفحه هم زیر همون نویگیشن‌بار قرار می‌گیره)
private val NavBarHeight = 66.dp
private val NavBarVerticalMargin = 14.dp

private enum class RecipeSortOption(val title: String) {
    LAST_UPDATED("آخرین ویرایش"),
    NEWEST("جدیدترین"),
    OLDEST("قدیمی‌ترین"),
    NAME("نام دستور")
}

private fun Recipe.creationSortTime(): Long = when {
    createdAt > 0L -> createdAt
    updatedAt > 0L -> updatedAt
    else -> 0L
}

private fun Recipe.updateSortTime(): Long = when {
    updatedAt > 0L -> updatedAt
    createdAt > 0L -> createdAt
    else -> 0L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MyRecipesViewModel = hiltViewModel()
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var selectedSortName by rememberSaveable {
        mutableStateOf(RecipeSortOption.LAST_UPDATED.name)
    }
    val selectedSort = RecipeSortOption.entries.firstOrNull { it.name == selectedSortName }
        ?: RecipeSortOption.LAST_UPDATED
    val sortedRecipes = remember(recipes, selectedSort) {
        when (selectedSort) {
            RecipeSortOption.LAST_UPDATED -> recipes.sortedByDescending { it.updateSortTime() }
            RecipeSortOption.NEWEST -> recipes.sortedByDescending { it.creationSortTime() }
            RecipeSortOption.OLDEST -> recipes.sortedBy { it.creationSortTime() }
            RecipeSortOption.NAME -> recipes.sortedBy { it.title.trim() }
        }
    }

    // فاصله‌ی پایین: فقط به اندازه‌ی ارتفاع نویگیشن‌بار شناور + حاشیه‌هاش
    // به‌علاوه‌ی یه فضای اضافه‌ی قابل‌تنظیم (extraScrollSpace).
    // ← همین یه عدد رو برای آزمون‌وخطا تغییر بده (الان: دو برابر ارتفاع نویگیشن‌بار)
    val extraScrollSpace = (NavBarHeight + NavBarVerticalMargin * 2)
    val bottomInset = NavBarHeight + NavBarVerticalMargin * 2 + extraScrollSpace

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.BgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── هدر ──────────────────────────────────────
            MRHeader(navController = navController)

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refreshFromCloud,
                modifier = Modifier.fillMaxSize()
            ) {
                if (recipes.isEmpty()) {
                    MREmptyState(
                        onAddClick = {
                            navController.navigate(NavGraph.Screen.AddRecipe.createRoute())
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = bottomInset
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "sort_control") {
                            MRSortControl(
                                selectedSort = selectedSort,
                                onSortSelected = { selectedSortName = it.name }
                            )
                        }

                        items(sortedRecipes, key = { it.id }) { recipe ->
                            MRRecipeCard(
                                recipe = recipe,
                                onClick = {
                                    navController.navigate(
                                        NavGraph.Screen.RecipeDetail.createRoute(recipe.id)
                                    )
                                },
                                onEdit = {
                                    navController.navigate(
                                        NavGraph.Screen.EditRecipe.createRoute(recipe.id)
                                    )
                                },
                                onDelete = {
                                    recipeToDelete = recipe
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── دکمه افزودن ───────────────────────
//        FloatingActionButton(
//            onClick = {
//                navController.navigate(NavGraph.Screen.AddRecipe.createRoute())
//            },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(
//                    top = 20.dp,
//                    end = 20.dp
//                ),
//            containerColor = GlassColors.AccentOrange,
//            contentColor = Color.White,
//            shape = CircleShape
//        ) {
//            Icon(
//                Icons.Default.Add,
//                contentDescription = "افزودن دستور جدید",
//                modifier = Modifier.size(24.dp)
//            )
//        }
    }
    // ── دیالوگ تایید حذف ─────────────────────────────────
    recipeToDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = {
                AppText(
                    "حذف دستور",
                    color = GlassColors.TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { AppText("«${recipe.title}» حذف شود؟", color = GlassColors.TextLight) },
            confirmButton = {
                TextButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                    viewModel.deleteRecipe(recipe.id)
                    recipeToDelete = null
                    }
                ) {
                    AppText("حذف", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { recipeToDelete = null }
                ) {
                    AppText("انصراف", color = GlassColors.TextLight)
                }
            },
            containerColor = GlassColors.GlassWhite
        )
    }
}

@Composable
private fun MRSortControl(
    selectedSort: RecipeSortOption,
    onSortSelected: (RecipeSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassColors.GlassWhite,
                contentColor = GlassColors.TextDark
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AppText("مرتب‌سازی: ${selectedSort.title}", fontSize = 13.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RecipeSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        AppText(
                            text = option.title,
                            color = if (option == selectedSort) {
                                GlassColors.AccentOrange
                            } else {
                                GlassColors.TextDark
                            }
                        )
                    },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─── هدر ─────────────────────────────────────────────────
@Composable
fun MRHeader(navController: NavController) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = statusBarHeight + 8.dp, start = 16.dp,
                end = 16.dp, bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape)
//                .background(GlassColors.GlassWhite)
//                .clickable { navController.popBackStack() },
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(Icons.Default.ArrowBack, contentDescription = "Back",
//                tint = GlassColors.TextDark, modifier = Modifier.size(20.dp))
//        }
        Spacer(modifier = Modifier.width(12.dp))
        AppText(
            text = "دستورهای من",
            color = GlassColors.TextDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── حالت خالی ───────────────────────────────────────────
@Composable
fun MREmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppText("🍳", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        AppText(
            text = "هنوز دستوری ندارید",
            color = GlassColors.TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppText(
            text = "اولین دستور خود را اضافه کنید",
            color = GlassColors.TextLight,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            AppText("افزودن دستور", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── کارت دستور ──────────────────────────────────────────
@Composable
fun MRRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassColors.AccentOrange.copy(alpha = 0.20f),
                        GlassColors.AccentSecondary.copy(alpha = 0.14f),
                        GlassColors.GlassCard.copy(alpha = 0.72f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f)
                )
            )
            .border(
                1.dp,
                if (GlassColors.darkMode) {
                    GlassColors.Divider.copy(alpha = 0.80f)
                } else {
                    Color.Transparent
                },
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── عکس ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        GlassColors.AccentOrange.copy(alpha = 0.15f)
                    )
            ) {
                if (recipe.imagePath.isNotEmpty()) {
                    AsyncImage(
                        model = Uri.parse(recipe.imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText("🍽️", fontSize = 28.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── اطلاعات ──────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = recipe.title,
                    color = GlassColors.TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                AppText(
                    text = recipe.author,
                    color = GlassColors.AccentOrange,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MRChip(recipe.totalTime)
                    MRChip("${recipe.yield} تعداد خروجی ")
                }
            }

            // ── دکمه‌های ویرایش و حذف ────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GlassColors.AccentOrange.copy(alpha = 0.20f),
                                    GlassColors.GlassCard.copy(alpha = 0.35f)
                                )
                            )
                        )
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit, contentDescription = "ویرایش",
                        tint = GlassColors.AccentOrange, modifier = Modifier.size(16.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete, contentDescription = "حذف",
                        tint = Color.Red.copy(0.7f), modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MRChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GlassColors.Divider)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        AppText(text, color = GlassColors.TextLight, fontSize = 10.sp)
    }
}
