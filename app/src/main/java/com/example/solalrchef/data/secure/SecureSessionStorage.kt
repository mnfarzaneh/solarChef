package com.mnfarzaneh.solalrchef.data.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String
)

@Singleton
class SecureSessionStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(
        "solarchef_secure_session",
        Context.MODE_PRIVATE
    )

    fun save(session: StoredSession) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(session.accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(session.refreshToken))
            .putString(KEY_USER_ID, encrypt(session.userId))
            .putString(KEY_EMAIL, encrypt(session.email))
            .apply()
    }

    fun read(): StoredSession? {
        val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)?.let(::decrypt)
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)?.let(::decrypt)
        val userId = preferences.getString(KEY_USER_ID, null)?.let(::decrypt)
        val email = preferences.getString(KEY_EMAIL, null)?.let(::decrypt)

        if (
            accessToken == null ||
            refreshToken == null ||
            userId == null ||
            email == null
        ) {
            clear()
            return null
        }

        return StoredSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = email
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encrypted = Base64.encodeToString(
            cipher.doFinal(value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

        return "$iv:$encrypted"
    }

    private fun decrypt(value: String): String? {
        return try {
            val parts = value.split(":", limit = 2)
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )

            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existingKey = (keyStore.getEntry(KEY_ALIAS, null)
                as? KeyStore.SecretKeyEntry)
            ?.secretKey

        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )

        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "solarchef_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128

        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
    }
}