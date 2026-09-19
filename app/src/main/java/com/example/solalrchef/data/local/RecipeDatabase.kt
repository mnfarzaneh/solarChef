package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RecipeEntity::class,
        CategoryEntity::class,
        PendingRecipeSyncEntity::class,
        PendingCategorySyncEntity::class,
        SystemFavoriteEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pendingRecipeSyncDao(): PendingRecipeSyncDao
    abstract fun pendingCategorySyncDao(): PendingCategorySyncDao
    abstract fun systemFavoriteDao(): SystemFavoriteDao

    class SeedDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            db.execSQL(
                "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) " +
                        "VALUES ('cat_bread', 'نانها', '🍞', 0, NULL)"
            )
            db.execSQL(
                "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) " +
                        "VALUES ('cat_cake', 'شیرینی‌جات', '🍰', 1, NULL)"
            )
            db.execSQL(
                "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) " +
                        "VALUES ('cat_pizza', 'پیتزا', '🍕', 2, NULL)"
            )
        }
    }

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN ownerId TEXT DEFAULT NULL"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        emoji TEXT NOT NULL DEFAULT '🍽️',
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        ownerId TEXT DEFAULT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN categoryId TEXT DEFAULT NULL"
                )

                db.execSQL(
                    "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) VALUES ('cat_bread', 'نان خانگی', '🍞', 0, NULL)"
                )
                db.execSQL(
                    "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) VALUES ('cat_cake', 'کیک اسفنجی', '🍰', 1, NULL)"
                )
                db.execSQL(
                    "INSERT INTO categories (id, name, emoji, sortOrder, ownerId) VALUES ('cat_pizza', 'نان پیتزا', '🍕', 2, NULL)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN categoryIdsJson TEXT NOT NULL DEFAULT '[]'"
                )

                db.execSQL(
                    """
                    UPDATE user_recipes
                    SET categoryIdsJson = '["' || categoryId || '"]'
                    WHERE categoryId IS NOT NULL AND categoryId != ''
                    """.trimIndent()
                )

                db.execSQL("UPDATE categories SET name = 'نانها' WHERE id = 'cat_bread'")
                db.execSQL("UPDATE categories SET name = 'شیرینی‌جات' WHERE id = 'cat_cake'")
                db.execSQL("UPDATE categories SET name = 'پیتزا' WHERE id = 'cat_pizza'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN imageKey TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pending_recipe_sync (
                        recipeId TEXT NOT NULL PRIMARY KEY,
                        ownerId TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_pending_recipe_sync_ownerId
                    ON pending_recipe_sync(ownerId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pending_category_sync (
                        categoryId TEXT NOT NULL PRIMARY KEY,
                        ownerId TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_pending_category_sync_ownerId
                    ON pending_category_sync(ownerId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE user_recipes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    """
                    UPDATE user_recipes
                    SET createdAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000,
                        updatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000
                    WHERE createdAt = 0 OR updatedAt = 0
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS system_favorites (
                        recipeId TEXT NOT NULL,
                        ownerKey TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(recipeId, ownerKey)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_system_favorites_ownerKey ON system_favorites(ownerKey)"
                )
            }
        }

        /** Rebuild legacy recipe tables without deleting user data. */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_recipes RENAME TO user_recipes_legacy")
                db.execSQL(
                    """
                    CREATE TABLE user_recipes (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        imagePath TEXT NOT NULL,
                        imageKey TEXT NOT NULL,
                        author TEXT NOT NULL,
                        source TEXT NOT NULL,
                        totalTime TEXT NOT NULL,
                        cookTime TEXT NOT NULL,
                        yield TEXT NOT NULL,
                        calories INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        rating REAL NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        ingredientsJson TEXT NOT NULL,
                        stepsJson TEXT NOT NULL,
                        equipmentJson TEXT NOT NULL,
                        ownerId TEXT DEFAULT NULL,
                        categoryIdsJson TEXT NOT NULL DEFAULT '[]',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO user_recipes (
                        id, title, description, imagePath, imageKey, author, source,
                        totalTime, cookTime, yield, calories, difficulty, rating,
                        isFavorite, ingredientsJson, stepsJson, equipmentJson,
                        ownerId, categoryIdsJson, createdAt, updatedAt
                    )
                    SELECT
                        id, title, description, imagePath, imageKey, author, source,
                        totalTime, cookTime, yield, calories, difficulty, rating,
                        isFavorite, ingredientsJson, stepsJson, equipmentJson,
                        ownerId, categoryIdsJson, createdAt, updatedAt
                    FROM user_recipes_legacy
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE user_recipes_legacy")
            }
        }
    }
}
