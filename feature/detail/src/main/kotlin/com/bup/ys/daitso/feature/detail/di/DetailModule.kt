package com.bup.ys.daitso.feature.detail.di

import com.bup.ys.daitso.feature.detail.repository.CartRepository
import com.bup.ys.daitso.feature.detail.repository.CartRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for the Detail feature.
 *
 * Provides bindings for repositories and other dependencies used in the detail feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DetailModule {

    /**
     * Provide CartRepository implementation.
     *
     * @param impl The CartRepositoryImpl instance
     * @return CartRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository
}
