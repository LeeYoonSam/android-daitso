package com.bup.ys.daitso.core.data.di

import com.bup.ys.daitso.core.data.repository.ProductRepository
import com.bup.ys.daitso.core.data.repository.ProductRepositoryImpl
import com.bup.ys.daitso.core.network.NetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for data layer dependency injection.
 *
 * Provides Repository implementations and binds interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Provides ProductRepository implementation.
     *
     * @param networkDataSource The network data source
     * @return ProductRepository instance
     */
    @Singleton
    @Provides
    fun provideProductRepository(
        networkDataSource: NetworkDataSource
    ): ProductRepository {
        return ProductRepositoryImpl(networkDataSource)
    }
}
