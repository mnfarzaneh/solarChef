package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mnfarzaneh.solalrchef.viewmodel.MyRecipesViewModel

// ─── رنگ‌ها ───────────────────────────────────────────────
private val MRBgLight      = Color(0xFFF5EFE6)
private val MRGlassCard    = Color(0xAAFFFFFF)
private val MRGlassWhite   = Color(0xCCFFFFFF)
private val MRAccentOrange = Color(0xFFFF6B35)
private val MRTextDark     = Color(0xFF2C1810)
private val MRTextLight    = Color(0xFF9E7B6A)
private val MRDivider      = Color(0x33000000)

@Composable
fun MyRecipesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MyRecipesViewModel = hiltViewModel()
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MRBgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── هدر ──────────────────────────────────────
            MRHeader(navController = navController)

            if (recipes.isEmpty()) {
                // ── حالت خالی ────────────────────────────
                MREmptyState(
                    onAddClick = {
                        navController.navigate(NavGraph.Screen.AddRecipe.route)
                    }
                )
            } else {
                // ── لیست دستورات ─────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp,
                        top = 8.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
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
                            onDelete = { recipeToDelete = recipe }
                        )
                    }
                }
            }
        }

        // ── دکمه افزودن ثابت پایین ───────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MRBgLight)
                    )
                )
                .padding(20.dp)
        ) {
            Button(
                onClick = { navController.navigate(NavGraph.Screen.AddRecipe.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MRAccentOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("افزودن دستور جدید", color = Color.White,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // ── دیالوگ تایید حذف ─────────────────────────────────
    recipeToDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("حذف دستور", color = MRTextDark, fontWeight = FontWeight.Bold) },
            text = { Text("«${recipe.title}» حذف شود؟", color = MRTextLight) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe(recipe.id)
                    recipeToDelete = null
                }) {
                    Text("حذف", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text("انصراف", color = MRTextLight)
                }
            },
            containerColor = MRBgLight
        )
    }
}

// ─── هدر ─────────────────────────────────────────────────
@Composable
fun MRHeader(navController: NavController) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarHeight + 8.dp, start = 16.dp,
                end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MRGlassWhite)
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                tint = MRTextDark, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "دستورهای من",
            color = MRTextDark,
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
        Text("🍳", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "هنوز دستوری ندارید",
            color = MRTextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "اولین دستور خود را اضافه کنید",
            color = MRTextLight,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = MRAccentOrange),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("افزودن دستور", color = Color.White, fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(16.dp))
            .background(MRGlassCard)
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
                    .background(MRDivider)
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
                        Text("🍽️", fontSize = 28.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── اطلاعات ──────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    color = MRTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.author,
                    color = MRAccentOrange,
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
                        .background(MRAccentOrange.copy(alpha = 0.15f))
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش",
                        tint = MRAccentOrange, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف",
                        tint = Color.Red.copy(0.7f), modifier = Modifier.size(16.dp))
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
            .background(MRDivider)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = MRTextLight, fontSize = 10.sp)
    }
}