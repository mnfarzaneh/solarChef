package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.AuthViewModel

// رنگ Snackbar خطا — هماهنگ با AddRecipeScreen
private val SnackbarErrorColor = Color(0xFFD32F2F)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }

    var showResetCodeDialog by remember { mutableStateOf(false) }

    var resetEmail by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetConfirmPassword by remember { mutableStateOf("") }
    var resetPasswordVisible by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val resetCodeSentMessage = stringResource(R.string.reset_code_sent)
    val resetCompletedMessage = stringResource(R.string.reset_completed)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthViewModel.UiEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)

                is AuthViewModel.UiEvent.NavigateToHome ->
                    onLoginSuccess()

                is AuthViewModel.UiEvent.OpenResetCodeDialog -> {
                    resetEmail = event.email
                    showResetDialog = false
                    showResetCodeDialog = true
                    snackbarHostState.showSnackbar(resetCodeSentMessage)
                }

                AuthViewModel.UiEvent.PasswordResetCompleted -> {
                    showResetCodeDialog = false
                    resetCode = ""
                    resetNewPassword = ""
                    resetConfirmPassword = ""
                    snackbarHostState.showSnackbar(resetCompletedMessage)
                }
            }
        }
    }

    Scaffold(
        containerColor = GlassColors.BgLight,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SnackbarErrorColor,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GlassColors.BgLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppText(
                    text = "🍲",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = stringResource(R.string.login_title),
                    color = GlassColors.TextDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                AppText(
                    text = stringResource(R.string.login_subtitle),
                    color = GlassColors.TextLight,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { AppText(stringResource(R.string.auth_email), color = GlassColors.TextLight, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = GlassColors.AccentOrange,
                        unfocusedBorderColor = GlassColors.Divider,
                        focusedTextColor     = GlassColors.TextDark,
                        unfocusedTextColor   = GlassColors.TextDark,
                        cursorColor          = GlassColors.AccentOrange,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { AppText(stringResource(R.string.auth_password), color = GlassColors.TextLight, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = stringResource(if (passwordVisible) R.string.auth_hide_password else R.string.auth_show_password),
                                tint = GlassColors.TextLight
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = GlassColors.AccentOrange,
                        unfocusedBorderColor = GlassColors.Divider,
                        focusedTextColor     = GlassColors.TextDark,
                        unfocusedTextColor   = GlassColors.TextDark,
                        cursorColor          = GlassColors.AccentOrange,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppText(
                        text = stringResource(R.string.login_forgot_password),
                        color = GlassColors.AccentOrange,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable(enabled = !state.isLoading) {
                                resetEmail = state.email
                                showResetDialog = true
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.login() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        AppText(stringResource(R.string.login_action), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(stringResource(R.string.login_no_account), color = GlassColors.TextLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    AppText(
                        text = stringResource(R.string.login_register_action),
                        color = GlassColors.AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToRegister() }
                    )
                }
            }
        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { /* فقط دکمهٔ انصراف پنجره را می‌بندد. */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = GlassColors.BgLight,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextMid,
            title = {
                AppText(
                    text = stringResource(R.string.reset_title),
                    modifier = Modifier.fillMaxWidth(),
                    color = GlassColors.TextDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppText(
                        stringResource(R.string.reset_description),
                        modifier = Modifier.fillMaxWidth(),
                        color = GlassColors.TextLight,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { AppText(stringResource(R.string.auth_email), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassColors.AccentOrange,
                            unfocusedBorderColor = GlassColors.Divider,
                            focusedTextColor = GlassColors.TextDark,
                            unfocusedTextColor = GlassColors.TextDark,
                            cursorColor = GlassColors.AccentOrange
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppText(
                        text = stringResource(R.string.reset_spam_hint),
                        modifier = Modifier.fillMaxWidth(),
                        color = GlassColors.TextMid,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { viewModel.requestPasswordReset(resetEmail) },
                    enabled = resetEmail.isNotBlank() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassColors.AccentOrange
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        AppText(stringResource(R.string.reset_send_code), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { showResetDialog = false },
                    enabled = !state.isLoading
                ) {
                    AppText(stringResource(R.string.action_cancel), color = GlassColors.TextMid)
                }
            }
        )
    }

    if (showResetCodeDialog) {
        AlertDialog(
            onDismissRequest = { /* فقط دکمهٔ انصراف پنجره را می‌بندد. */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = GlassColors.BgLight,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextMid,
            title = {
                AppText(
                    text = stringResource(R.string.reset_new_password_title),
                    modifier = Modifier.fillMaxWidth(),
                    color = GlassColors.TextDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppText(
                        text = stringResource(R.string.reset_code_description, resetEmail),
                        modifier = Modifier.fillMaxWidth(),
                        color = GlassColors.TextLight,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetCode,
                        onValueChange = { value ->
                            resetCode = value.filter { it.isDigit() }.take(8)
                        },
                        label = { AppText(stringResource(R.string.reset_code_label), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = resetNewPassword,
                        onValueChange = { resetNewPassword = it },
                        label = { AppText(stringResource(R.string.reset_new_password), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (resetPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { resetPasswordVisible = !resetPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (resetPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = stringResource(R.string.auth_toggle_password_visibility),
                                    tint = GlassColors.TextLight
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = resetConfirmPassword,
                        onValueChange = { resetConfirmPassword = it },
                        label = { AppText(stringResource(R.string.reset_confirm_new_password), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (resetPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        viewModel.resetPassword(
                            email = resetEmail,
                            code = resetCode,
                            newPassword = resetNewPassword,
                            confirmPassword = resetConfirmPassword
                        )
                    },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassColors.AccentOrange
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        AppText(stringResource(R.string.reset_change_password), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { showResetCodeDialog = false },
                    enabled = !state.isLoading
                ) {
                    AppText(stringResource(R.string.action_cancel), color = GlassColors.TextMid)
                }
            }
        )
    }
}
