package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.data.remote.ApiResult
import com.mnfarzaneh.solalrchef.data.remote.SolarChefApiRepository
import com.mnfarzaneh.solalrchef.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mnfarzaneh.solalrchef.data.CategoryRepository

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String = "",
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiRepository: SolarChefApiRepository,
    private val userRecipeRepository: UserRecipeRepository,
    private val categoryRepository: CategoryRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = apiRepository.isLoggedIn,
            userEmail  = apiRepository.currentUser?.email ?: ""
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        object NavigateToHome : UiEvent()

        data class OpenResetCodeDialog(val email: String) : UiEvent()
        object PasswordResetCompleted : UiEvent()
    }

    init {
        // ← گوش دادن به تغییرات وضعیت لاگین
        viewModelScope.launch {
            apiRepository.authState.collect { user ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = user != null,
                    userEmail  = user?.email ?: ""
                )
            }
        }

        // ── شبکه‌ی اطمینان: موقع باز شدن اپ اگه لاگین و آنلاینیم
        viewModelScope.launch {
            if (apiRepository.isLoggedIn && networkMonitor.isConnected()) {
                syncEverythingAfterAuthentication()
            }
        }
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    private suspend fun syncEverythingAfterAuthentication() {
        // دسته‌بندی‌ها باید قبل از دستورها Sync شوند؛ چون هر دستور ممکن است
        // به یک دسته‌بندی شخصی وابسته باشد.
        apiRepository.userId?.let { userId ->
            categoryRepository.syncFromCloud()
            categoryRepository.claimUnclaimedCategories(userId)
            categoryRepository.syncPendingOperations()
            categoryRepository.syncFromCloud()
        }

        // ── دستورها ───────────────────────────────────────
        userRecipeRepository.syncFromCloud()
        userRecipeRepository.pushAllLocalRecipesToCloud()
        userRecipeRepository.syncPendingOperations()
        userRecipeRepository.syncFromCloud()
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            sendMessage("ایمیل و رمز عبور را وارد کنید")
            return
        }
        if (!isValidEmail(state.email)) {
            sendMessage("فرمت ایمیل درست نیست. لطفاً بررسی کنید")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = apiRepository.login(state.email.trim(), state.password)) {
                is ApiResult.Success -> {
                    syncEverythingAfterAuthentication()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.NavigateToHome)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendMessage(result.message)
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            sendMessage("ایمیل و رمز عبور را وارد کنید")
            return
        }
        if (!isValidEmail(state.email)) {
            sendMessage("فرمت ایمیل درست نیست. لطفاً قبل از ادامه دوباره بررسی کنید")
            return
        }
        if (state.password.length < 10) {
            sendMessage("رمز عبور باید حداقل ۱۰ کاراکتر باشد")
            return
        }
        if (state.password != state.confirmPassword) {
            sendMessage("رمز عبور و تکرار آن یکسان نیستند")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = apiRepository.register(state.email.trim(), state.password)) {
                is ApiResult.Success -> {
                    syncEverythingAfterAuthentication()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendMessage("ثبت‌نام موفق بود!")
                    _uiEvent.send(UiEvent.NavigateToHome)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendMessage(result.message)
                }
            }
        }
    }
    fun requestPasswordReset(email: String) {
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            sendMessage("فرمت ایمیل درست نیست")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = apiRepository.requestPasswordReset(cleanEmail)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.OpenResetCodeDialog(cleanEmail))
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendMessage(result.message)
                }
            }
        }
    }

    fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val cleanCode = code.trim()

        if (cleanCode.length != 8 || cleanCode.any { it !in '0'..'9' }) {
            sendMessage("کد بازیابی باید ۸ رقم باشد")
            return
        }

        if (newPassword.length < 10) {
            sendMessage("رمز عبور باید حداقل ۱۰ کاراکتر باشد")
            return
        }

        if (newPassword != confirmPassword) {
            sendMessage("رمز عبور و تکرار آن یکسان نیستند")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (
                val result = apiRepository.resetPassword(
                    email = email,
                    code = cleanCode,
                    newPassword = newPassword
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        email = email.trim(),
                        password = "",
                        isLoading = false
                    )
                    _uiEvent.send(UiEvent.PasswordResetCompleted)
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendMessage(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            apiRepository.logout()
        }
    }

    fun changeEmail(newEmail: String) {
        if (!isValidEmail(newEmail)) {
            sendMessage("فرمت ایمیل درست نیست")
            return
        }
        sendMessage("برای تغییر ایمیل، از طریق پشتیبانی اقدام کنید")
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch { _uiEvent.send(UiEvent.ShowMessage(message)) }
    }
}
