package com.bup.ys.daitso.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.dao.ProductDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.database.entity.ProductEntity

/**
 * Room Database for Daitso application.
 *
 * Manages the local SQLite database with entities and DAOs.
 *
 * Database schema version: 2
 * Entities: CartItemEntity, ProductEntity
 */
@Database(
    entities = [CartItemEntity::class, ProductEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DaitsoDatabase : RoomDatabase() {

    /**
     * Provides access to CartDao.
     */
    abstract fun cartDao(): CartDao

    /**
     * Provides access to ProductDao.
     */
    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "daitso.db"

        /**
         * Migration from schema version 1 to 2.
         * Creates the products table.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `products` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `price` REAL NOT NULL,
                        `imageUrl` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `stock` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Creates or returns existing singleton instance of DaitsoDatabase.
         *
         * @param context Android application context
         * @return Singleton instance of DaitsoDatabase
         */
        @Volatile
        private var INSTANCE: DaitsoDatabase? = null

        fun getInstance(context: Context): DaitsoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DaitsoDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
