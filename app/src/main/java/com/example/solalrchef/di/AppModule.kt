package com.mnfarzaneh.solalrchef.di

import android.content.Context
import androidx.room.Room
import com.mnfarzaneh.solalrchef.data.local.CategoryDao
import com.mnfarzaneh.solalrchef.data.local.RecipeDao
import com.mnfarzaneh.solalrchef.data.local.RecipeDatabase
import com.mnfarzaneh.solalrchef.data.local.SystemFavoriteDao
import com.mnfarzaneh.solalrchef.data.remote.ApiService
import com.mnfarzaneh.solalrchef.data.remote.SolarChefApiRepository
import com.mnfarzaneh.solalrchef.data.secure.SecureSessionStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.mnfarzaneh.solalrchef.data.local.PendingRecipeSyncDao
import com.mnfarzaneh.solalrchef.data.local.PendingCategorySyncDao
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RecipeDatabase::class.java,
            "solar_chef_db"
        )
            .addMigrations(
                RecipeDatabase.MIGRATION_1_2,
                RecipeDatabase.MIGRATION_2_3,
                RecipeDatabase.MIGRATION_3_4,
                RecipeDatabase.MIGRATION_4_5,
                RecipeDatabase.MIGRATION_5_6,
                RecipeDatabase.MIGRATION_6_7,
                RecipeDatabase.MIGRATION_7_8,
                RecipeDatabase.MIGRATION_8_9,
                RecipeDatabase.MIGRATION_9_10,
                RecipeDatabase.MIGRATION_10_11,
                RecipeDatabase.MIGRATION_11_12
            )
            .addCallback(RecipeDatabase.SeedDatabaseCallback())
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao = db.recipeDao()

    @Provides
    @Singleton
    fun provideSystemFavoriteDao(db: RecipeDatabase): SystemFavoriteDao = db.systemFavoriteDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: RecipeDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun providePendingCategorySyncDao(
        db: RecipeDatabase
    ): PendingCategorySyncDao = db.pendingCategorySyncDao()

    @Provides
    @Singleton
    fun providePendingRecipeSyncDao(
        db: RecipeDatabase
    ): PendingRecipeSyncDao = db.pendingRecipeSyncDao()

    @Provides
    @Singleton
    fun provideSolarChefApiRepository(
        apiService: ApiService,
        sessionStorage: SecureSessionStorage,
        @ApplicationContext context: Context
    ): SolarChefApiRepository =
        SolarChefApiRepository(apiService, sessionStorage, context)
}
