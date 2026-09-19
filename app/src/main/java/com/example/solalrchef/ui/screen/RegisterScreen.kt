package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.AuthViewModel

// رنگ Snackbar خطا — هماهنگ با LoginScreen / AddRecipeScreen
private val SnackbarErrorColor = Color(0xFFD32F2F)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthViewModel.UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is AuthViewModel.UiEvent.NavigateToHome -> {
                    onRegisterSuccess()
                }

                is AuthViewModel.UiEvent.OpenResetCodeDialog -> Unit

                AuthViewModel.UiEvent.PasswordResetCompleted -> Unit
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
            Column(modifier = Modifier.fillMaxSize()) {

                // ── هدر با دکمه‌ی بازگشت ──────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back),
                        tint = GlassColors.TextDark,
                        modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateBack() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AppText(
                        text = stringResource(R.string.register_title),
                        color = GlassColors.TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    AppText(
                        text = stringResource(R.string.register_subtitle),
                        color = GlassColors.TextLight,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        label = { AppText(stringResource(R.string.auth_confirm_password), color = GlassColors.TextLight, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = stringResource(if (confirmPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password),
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.register() },
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
                            AppText(stringResource(R.string.register_action), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(stringResource(R.string.register_has_account), color = GlassColors.TextLight, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        AppText(
                            text = stringResource(R.string.register_login_action),
                            color = GlassColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateBack() }
                        )
                    }
                }
            }
        }
    }
}
