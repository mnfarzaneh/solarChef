# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit و Gson در زمان اجرا از reflection و نام فیلدهای JSON استفاده می‌کنند.
# بدون این قواعد، R8 در نسخه Release نام DTOها را تغییر می‌دهد و درخواست/پاسخ
# ورود، Hero، مقاله‌ها، دستورها و دسته‌بندی‌ها خراب می‌شوند.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# مدل‌های قرارداد API نباید rename یا حذف شوند.
-keep class com.mnfarzaneh.solalrchef.data.remote.dto.** { *; }
-keep class com.mnfarzaneh.solalrchef.data.remote.ParseRecipeRequest { *; }
-keep class com.mnfarzaneh.solalrchef.data.remote.ParsedIngredient { *; }
-keep class com.mnfarzaneh.solalrchef.data.remote.ParsedRecipeResponse { *; }

# متدها و annotationهای Retrofit باید برای ساخت پیاده‌سازی runtime باقی بمانند.
-keep,allowoptimization,allowshrinking interface com.mnfarzaneh.solalrchef.data.remote.ApiService
-keep,allowoptimization,allowshrinking interface com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
-keepclasseswithmembers,allowshrinking,allowoptimization,includedescriptorclasses class * {
    @retrofit2.http.* <methods>;
}

# اطلاعات خط Release برای تبدیل stack trace با mapping.txt.
-keepattributes SourceFile,LineNumberTable
