//package com.mnfarzaneh.solalrchef.data.remote
//
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//import javax.inject.Singleton
//
//// ─── نتیجه عملیات Auth ───────────────────────────────────
//sealed class AuthResult {
//    object Success : AuthResult()
//    data class Error(val message: String) : AuthResult()
//}
//
//@Singleton
//class FirebaseAuthRepository @Inject constructor(
//    private val auth: FirebaseAuth
//) {
//    // ── کاربر فعلی ───────────────────────────────────────
//    val currentUser: FirebaseUser?
//        get() = auth.currentUser
//
//    val isLoggedIn: Boolean
//        get() = auth.currentUser != null
//
//    val userId: String?
//        get() = auth.currentUser?.uid
//
//    // ── Flow وضعیت ورود (live) ───────────────────────────
//    val authState: Flow<FirebaseUser?> = callbackFlow {
//        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            trySend(firebaseAuth.currentUser)
//        }
//        auth.addAuthStateListener(listener)
//        awaitClose { auth.removeAuthStateListener(listener) }
//    }
//
//    // ── ثبت‌نام ───────────────────────────────────────────
//    suspend fun register(email: String, password: String): AuthResult {
//        return try {
//            auth.createUserWithEmailAndPassword(email, password).await()
//            AuthResult.Success
//        } catch (e: Exception) {
//            AuthResult.Error(getFriendlyError(e.message))
//        }
//    }
//
//    // ── ورود ─────────────────────────────────────────────
//    suspend fun login(email: String, password: String): AuthResult {
//        return try {
//            auth.signInWithEmailAndPassword(email, password).await()
//            AuthResult.Success
//        } catch (e: Exception) {
//            AuthResult.Error(getFriendlyError(e.message))
//        }
//    }
//
//    // ── خروج ─────────────────────────────────────────────
//    fun logout() {
//        auth.signOut()
//    }
//
//    // ── فراموشی پسورد ────────────────────────────────────
//    suspend fun resetPassword(email: String): AuthResult {
//        return try {
//            auth.sendPasswordResetEmail(email).await()
//            AuthResult.Success
//        } catch (e: Exception) {
//            AuthResult.Error(getFriendlyError(e.message))
//        }
//    }
//
//    // ── جدید: ارسال ایمیل تأییدیه ─────────────────────────
//    // بعد از ثبت‌نام صدا زده می‌شه؛ اگه کاربر ایمیل رو اشتباه تایپ کرده باشه،
//    // هیچ‌وقت این ایمیل رو دریافت نمی‌کنه و متوجه اشتباهش می‌شه.
//    suspend fun sendEmailVerification(): AuthResult {
//        return try {
//            auth.currentUser?.sendEmailVerification()?.await()
//            AuthResult.Success
//        } catch (e: Exception) {
//            AuthResult.Error(getFriendlyError(e.message))
//        }
//    }
//
//    // ── جدید: تغییر ایمیل حساب فعلی ───────────────────────
//    // «راه برگشت» برای وقتی کاربر موقع ثبت‌نام ایمیل رو اشتباه وارد کرده.
//    suspend fun updateEmail(newEmail: String): AuthResult {
//        return try {
//            auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
//            AuthResult.Success
//        } catch (e: Exception) {
//            AuthResult.Error(getFriendlyError(e.message))
//        }
//    }
//
//    // ── تبدیل خطاهای Firebase به فارسی ──────────────────
//    private fun getFriendlyError(message: String?): String {
//        return when {
//            message == null -> "خطای ناشناخته"
//            message.contains("email address is already in use") ->
//                "این ایمیل قبلاً ثبت شده"
//            message.contains("email address is badly formatted") ->
//                "فرمت ایمیل اشتباه است"
//            message.contains("password is invalid") ||
//                    message.contains("INVALID_LOGIN_CREDENTIALS") ->
//                "ایمیل یا رمز عبور اشتباه است"
//            message.contains("no user record") ->
//                "حساب کاربری با این ایمیل یافت نشد"
//            message.contains("password should be at least") ->
//                "رمز عبور باید حداقل ۶ کاراکتر باشد"
//            message.contains("network") ||
//                    message.contains("unable to resolve host") ->
//                "خطای اتصال به اینترنت"
//            message.contains("too many requests") ->
//                "تعداد تلاش‌ها زیاد است. بعداً امتحان کنید"
//            message.contains("requires recent authentication") ->
//                "برای این کار باید دوباره وارد حساب بشی (خروج و ورود مجدد)"
//            else -> "خطا در ورود. دوباره تلاش کنید"
//        }
//    }
//}