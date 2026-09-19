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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.viewmodel.AiRecipeExtractorViewModel

private val AIBg          = Color(0xFFF5EFE6)
private val AICard        = Color(0xAAFFFFFF)
private val AIWhite       = Color(0xCCFFFFFF)
private val AIOrange      = Color(0xFFFF6B35)
private val AITextDark    = Color(0xFF2C1810)
private val AITextLight   = Color(0xFF9E7B6A)
private val AIDivider     = Color(0x33000000)
private val AIGreen       = Color(0xFF4CAF50)
private val AIRed         = Color(0xFFE53935)

@Composable
fun AiRecipeExtractorScreen(
    navController: NavController,
    viewModel: AiRecipeExtractorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ذخیره موفق
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar(
                message = "«${state.parsedTitle}» با موفقیت ذخیره شد ✓",
                duration = SnackbarDuration.Short
            )
            viewModel.onSaveHandled()
            navController.popBackStack()
        }
    }

    // خطا
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.onErrorHandled()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("✓")
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isSuccess) AIGreen else AIRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(data.visuals.message, fontWeight = FontWeight.Medium)
                }
            }
        },
        containerColor = AIBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Spacer(modifier = Modifier.height(statusBarHeight + 8.dp))

            // ── هدر ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AIWhite)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = AITextDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("استخراج با هوش مصنوعی",
                        color = AITextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("متن دستور را وارد کن",
                        color = AITextLight, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── راهنما ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AIOrange.copy(0.1f))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, null,
                            tint = AIOrange, modifier = Modifier.size(18.dp))
                        Text("چطور کار میکنه؟",
                            color = AIOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "متن دستور غذا رو (فارسی یا انگلیسی) اینجا بنویس یا paste کن.\n" +
                                "هوش مصنوعی مواد لازم، مراحل، زمان و دسته‌بندی رو استخراج میکنه\n" +
                                "و مستقیم توی دستورات تو ذخیره میشه.",
                        color = AITextDark, fontSize = 13.sp, lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.recipe_extraction_vpn_notice),
                        color = AIRed,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── فیلد متن ─────────────────────────────────
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                label = { Text("متن دستور غذا", color = AITextLight) },
                placeholder = {
                    Text(
                        "مثلاً:\nبرای تهیه کیک اسفنجی، ۳ عدد تخم‌مرغ را با ۱ پیمانه شکر هم بزنید...",
                        color = AITextLight.copy(0.5f), fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                minLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AIOrange,
                    unfocusedBorderColor = AIDivider,
                    focusedTextColor     = AITextDark,
                    unfocusedTextColor   = AITextDark,
                    cursorColor          = AIOrange
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // تعداد کاراکتر
            Text(
                "${state.inputText.length} کاراکتر",
                color = AITextLight, fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── دکمه استخراج ─────────────────────────────
            Button(
                onClick = { viewModel.extractAndSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading && state.inputText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AIOrange,
                    disabledContainerColor = AIOrange.copy(0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("در حال استخراج...", color = Color.White, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استخراج و ذخیره",
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // نکته
            Text(
                "⚠️ ${stringResource(R.string.recipe_extraction_vpn_help)} نتایج ممکن است نیاز به ویرایش داشته باشند.",
                color = AITextLight, fontSize = 11.sp, lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun androidx.compose.foundation.layout.BoxScope.clickable(function: () -> Unit): Modifier {
    return Modifier
}
