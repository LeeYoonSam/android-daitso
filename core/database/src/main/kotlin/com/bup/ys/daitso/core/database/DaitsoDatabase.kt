package com.bup.ys.daitso.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity

/**
 * Room Database for Daitso application.
 *
 * Manages the local SQLite database with entities and DAOs.
 *
 * Database schema version: 1
 * Entities: CartItemEntity
 */
@Database(
    entities = [CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DaitsoDatabase : RoomDatabase() {

    /**
     * Provides access to CartDao.
     */
    abstract fun cartDao(): CartDao

    companion object {
        private const val DATABASE_NAME = "daitso.db"

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
