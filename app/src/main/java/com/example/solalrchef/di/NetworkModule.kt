package com.mnfarzaneh.solalrchef.di

import android.content.Context
import com.mnfarzaneh.solalrchef.data.remote.ApiService
import com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
import com.mnfarzaneh.solalrchef.util.AndroidNetworkMonitor
import com.mnfarzaneh.solalrchef.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import com.mnfarzaneh.solalrchef.BuildConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SolarChefApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecipeParserRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // فعلاً Ktor روی کامپیوتر خودت
    private const val API_BASE_URL = "https://solarchef.ir/api/v1/"
    // سرویس قدیمیِ استخراج هوشمند
    private const val PARSER_BASE_URL = "https://mnfarzaneh.pythonanywhere.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .callTimeout(3, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    @SolarChefApiRetrofit
    fun provideSolarChefRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @RecipeParserRetrofit
    fun provideRecipeParserRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(PARSER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(
        @SolarChefApiRetrofit retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideRecipeParserApi(
        @RecipeParserRetrofit retrofit: Retrofit
    ): RecipeParserApi = retrofit.create(RecipeParserApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = AndroidNetworkMonitor(context)
}