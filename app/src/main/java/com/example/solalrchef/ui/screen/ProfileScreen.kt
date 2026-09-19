package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var newEmailInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.BgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // پروفایل یکی از تب‌های اصلی است و به دکمه بازگشت نیاز ندارد.
            // همین هدر در «دستورهای من» و «علاقه‌مندی‌ها» هم استفاده می‌شود.
            HomeListHeader(title = stringResource(R.string.profile_title))

            Spacer(modifier = Modifier.height(24.dp))

            // ── آواتار ─────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(GlassColors.AccentOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = GlassColors.AccentOrange,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.isLoggedIn) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppText(
                            text = state.userEmail.ifBlank { stringResource(R.string.profile_user_fallback) },
                            color = GlassColors.TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.profile_edit_email),
                            tint = GlassColors.AccentOrange,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    newEmailInput = state.userEmail
                                    showEditEmailDialog = true
                                }
                        )
                    }
                } else {
                    AppText(
                        text = stringResource(R.string.profile_guest),
                        color = GlassColors.TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (state.isLoggedIn) {
                    // ── حالت لاگین: دکمه‌ی خروج ─────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassColors.GlassCard)
                            .clickable { showLogoutDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AppText(
                            text = stringResource(R.string.profile_logout),
                            color = Color(0xFFD32F2F),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // ── حالت مهمان: دعوت به پشتیبان‌گیری ────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassColors.AccentOrange.copy(alpha = 0.10f))
                            .border(1.dp, GlassColors.AccentOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = GlassColors.AccentOrange,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppText(
                            text = stringResource(R.string.profile_local_only_title),
                            color = GlassColors.TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = stringResource(R.string.profile_local_only_description),
                            color = GlassColors.TextLight,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AppText(stringResource(R.string.profile_login_or_register), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showEditEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = GlassColors.GlassWhite,
            tonalElevation = 0.dp,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextDark,
            title = { AppText(stringResource(R.string.profile_edit_email), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    AppText(
                        stringResource(R.string.profile_edit_email_description),
                        fontSize = 12.sp,
                        color = GlassColors.TextLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { newEmailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GlassColors.GlassCard,
                            unfocusedContainerColor = GlassColors.GlassCard,
                            focusedBorderColor = GlassColors.AccentOrange,
                            unfocusedBorderColor = Color(0xFFFFC107),
                            cursorColor = GlassColors.AccentOrange
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        viewModel.changeEmail(newEmailInput.trim())
                        showEditEmailDialog = false
                    },
                    enabled = newEmailInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
                ) {
                    AppText(stringResource(R.string.profile_send_verification), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { showEditEmailDialog = false },
                    border = BorderStroke(1.dp, GlassColors.AccentOrange)
                ) {
                    AppText(stringResource(R.string.action_cancel), color = GlassColors.AccentOrange)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = GlassColors.GlassWhite,
            tonalElevation = 0.dp,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextDark,
            title = { AppText(stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold) },
            text = { AppText(stringResource(R.string.profile_logout_confirmation), color = GlassColors.TextDark, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    AppText(stringResource(R.string.profile_logout_confirm), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { showLogoutDialog = false },
                    border = BorderStroke(1.dp, GlassColors.AccentOrange)
                ) {
                    AppText(stringResource(R.string.action_cancel), color = GlassColors.AccentOrange)
                }
            }
        )
    }
}
