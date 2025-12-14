package com.bup.ys.daitso.feature.cart.di

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.feature.cart.domain.CartRepository
import com.bup.ys.daitso.feature.cart.repository.CartRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Cart feature dependency injection.
 *
 * Provides cart-related dependencies:
 * - CartRepository implementation
 * - CartDao from database layer
 *
 * Uses SingletonComponent to ensure single instance of repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object CartModule {

    /**
     * Provides CartRepository implementation.
     *
     * Creates a singleton instance of CartRepositoryImpl
     * that implements the CartRepository interface.
     *
     * @param cartDao Data Access Object for cart operations
     * @return CartRepository instance
     */
    @Singleton
    @Provides
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepositoryImpl(cartDao)
    }
}
