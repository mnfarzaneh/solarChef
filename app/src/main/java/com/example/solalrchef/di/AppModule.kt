package com.mnfarzaneh.solalrchef.di

import android.content.Context
import androidx.room.Room
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.data.local.RecipeDao
import com.mnfarzaneh.solalrchef.data.local.RecipeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeDatabase {
        // ← مستقیم Room.databaseBuilder، نه getInstance
        return Room.databaseBuilder(
            context.applicationContext,
            RecipeDatabase::class.java,
            "solar_chef_db"          // ← همون اسم قبلی تا داده‌ها حفظ بشن
        )
            .addMigrations(
                RecipeDatabase.MIGRATION_1_2,
                RecipeDatabase.MIGRATION_2_3   // ← این رو اضافه کن
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao {
        return db.recipeDao()
    }

    @Provides
    @Singleton
    fun provideUserRecipeRepository(dao: RecipeDao): UserRecipeRepository {
        return UserRecipeRepository(dao)
    }
}