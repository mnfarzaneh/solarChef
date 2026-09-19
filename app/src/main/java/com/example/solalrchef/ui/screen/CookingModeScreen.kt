package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.Recipe
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.GlassIconButton
import com.mnfarzaneh.solalrchef.viewmodel.CookingViewModel

private const val REFERENCE_NONE = 0
private const val REFERENCE_INGREDIENTS = 1
private const val REFERENCE_EQUIPMENT = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(recipeId: String, navController: NavController) {
    val viewModel: CookingViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current
    var reference by remember { mutableIntStateOf(REFERENCE_NONE) }

    DisposableEffect(view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GlassColors.AccentOrange)
        }
        return
    }
    val recipe = state.recipe ?: run {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    val steps = recipe.steps
    val index = state.currentStep.coerceIn(0, steps.lastIndex.coerceAtLeast(0))

    Column(Modifier.fillMaxSize().background(GlassColors.BgLight).navigationBarsPadding()) {
        CookTopBar(recipe.title) { navController.popBackStack() }
        if (steps.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                AppText(stringResource(R.string.cooking_no_steps), color = GlassColors.TextMid, textAlign = TextAlign.Center)
            }
        } else {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                CookProgress(index, steps.size)
                Spacer(Modifier.height(22.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(GlassColors.GlassCard).padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp).clip(CircleShape).background(GlassColors.AccentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            AppText("${index + 1}", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        AppText(stringResource(R.string.cooking_current_step), color = GlassColors.AccentOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))
                    AppText(steps[index].instruction, color = GlassColors.TextDark, fontSize = 20.sp, lineHeight = 32.sp)
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CookReferenceButton(stringResource(R.string.recipe_ingredients), Icons.Default.RestaurantMenu,
                        recipe.ingredients.isNotEmpty(), Modifier.weight(1f)) { reference = REFERENCE_INGREDIENTS }
                    CookReferenceButton(stringResource(R.string.recipe_equipment), Icons.Default.Kitchen,
                        recipe.equipment.isNotEmpty(), Modifier.weight(1f)) { reference = REFERENCE_EQUIPMENT }
                }
            }
            CookNavigation(
                index = index,
                lastIndex = steps.lastIndex,
                onPrevious = viewModel::previousStep,
                onNext = viewModel::nextStep,
                onFinish = {
                    if (!state.completedSteps.getOrElse(index) { false }) viewModel.toggleStep(index)
                    navController.popBackStack()
                }
            )
        }
    }

    if (reference != REFERENCE_NONE) {
        ModalBottomSheet(onDismissRequest = { reference = REFERENCE_NONE }, containerColor = GlassColors.BgLight) {
            CookReferenceSheet(recipe, reference)
        }
    }
}

@Composable
private fun CookTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.content_description_back), tint = GlassColors.TextDark)
        }
        AppText(title, color = GlassColors.TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun CookProgress(index: Int, total: Int) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AppText(stringResource(R.string.cooking_progress, index + 1, total), color = GlassColors.TextDark, fontWeight = FontWeight.SemiBold)
            AppText("${(index + 1) * 100 / total}٪", color = GlassColors.AccentOrange, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(GlassColors.AccentOrange.copy(.16f))) {
            Box(Modifier.fillMaxWidth((index + 1f) / total).height(7.dp).background(GlassColors.AccentOrange))
        }
    }
}

@Composable
private fun CookNavigation(index: Int, lastIndex: Int, onPrevious: () -> Unit, onNext: () -> Unit, onFinish: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onPrevious, enabled = index > 0, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null); Spacer(Modifier.width(6.dp)); AppText(stringResource(R.string.cooking_previous), fontWeight = FontWeight.Bold)
        }
        Button(if (index == lastIndex) onFinish else onNext, modifier = Modifier.weight(1.6f).height(54.dp),
            shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)) {
            AppText(stringResource(if (index == lastIndex) R.string.cooking_finish else R.string.cooking_next), color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp)); Icon(if (index == lastIndex) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
        }
    }
}

@Composable
private fun CookReferenceButton(label: String, icon: ImageVector, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.height(52.dp).clip(RoundedCornerShape(15.dp)).background(GlassColors.GlassCard)
        .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) GlassColors.AccentOrange else GlassColors.TextLight)
        Spacer(Modifier.width(7.dp)); AppText(label, color = if (enabled) GlassColors.TextDark else GlassColors.TextLight, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CookReferenceSheet(recipe: Recipe, reference: Int) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)) {
        AppText(stringResource(if (reference == REFERENCE_INGREDIENTS) R.string.recipe_ingredients else R.string.recipe_equipment),
            color = GlassColors.TextDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        if (reference == REFERENCE_INGREDIENTS) recipe.ingredients.forEach { ingredient ->
            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
                AppText(ingredient.name, color = GlassColors.TextDark, modifier = Modifier.weight(1f))
                AppText(listOf(ingredient.amount, ingredient.unit).filter { it.isNotBlank() }.joinToString(" "), color = GlassColors.AccentOrange, fontWeight = FontWeight.SemiBold)
            }
        } else recipe.equipment.forEach { AppText("• $it", color = GlassColors.TextDark, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        Spacer(Modifier.height(30.dp))
    }
}
