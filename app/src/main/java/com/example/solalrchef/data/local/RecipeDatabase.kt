package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecipeEntity::class],
    version = 3,               // ← برگشت به 2 چون migration فقط 1→2 داریم
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN equipmentJson TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }
    }
}