plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)               // ← اضافه شد
    alias(libs.plugins.google.services)  // ← اضافه

}

val solarChefKeystorePath = providers.environmentVariable("SOLARCHEF_KEYSTORE_PATH")
val solarChefStorePassword = providers.environmentVariable("SOLARCHEF_STORE_PASSWORD")
val solarChefKeyAlias = providers.environmentVariable("SOLARCHEF_KEY_ALIAS")
val solarChefKeyPassword = providers.environmentVariable("SOLARCHEF_KEY_PASSWORD")
val solarChefReleaseSigningReady = listOf(
    solarChefKeystorePath,
    solarChefStorePassword,
    solarChefKeyAlias,
    solarChefKeyPassword
).all { it.isPresent }

android {
    namespace = "com.mnfarzaneh.solalrchef"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.mnfarzaneh.solalrchef"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (solarChefReleaseSigningReady) {
            create("release") {
                storeFile = file(solarChefKeystorePath.get())
                storePassword = solarChefStorePassword.get()
                keyAlias = solarChefKeyAlias.get()
                keyPassword = solarChefKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            if (solarChefReleaseSigningReady) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)


    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.work.compiler)
    // ── Hilt ─────────────────────────────────────────
    implementation(libs.hilt.android)                  // ← اضافه شد
    ksp(libs.hilt.compiler)                            // ← اضافه شد
    implementation(libs.hilt.navigation.compose)       // ← اضافه شد
    // ── Retrofit / Network ────────────────────────────
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.haze)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // ── Test ─────────────────────────────────────────
    testImplementation(libs.junit)
// ── Test ─────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)   // ← اضافه شد
    testImplementation(libs.mockk)                     // ← اضافه شد
    testImplementation(libs.turbine)                   // ← اضافه شد
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
