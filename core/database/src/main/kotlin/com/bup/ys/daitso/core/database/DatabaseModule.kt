package com.bup.ys.daitso.core.database

import android.content.Context
import com.bup.ys.daitso.core.database.dao.CartDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for database dependency injection.
 *
 * Provides Room database instance and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides singleton instance of DaitsoDatabase.
     *
     * @param context Android application context
     * @return Singleton DaitsoDatabase instance
     */
    @Singleton
    @Provides
    fun provideDaitsoDatabase(
        @ApplicationContext context: Context
    ): DaitsoDatabase {
        return DaitsoDatabase.getInstance(context)
    }

    /**
     * Provides CartDao for database operations.
     *
     * @param database The DaitsoDatabase instance
     * @return CartDao instance
     */
    @Singleton
    @Provides
    fun provideCartDao(database: DaitsoDatabase): CartDao {
        return database.cartDao()
    }
}
