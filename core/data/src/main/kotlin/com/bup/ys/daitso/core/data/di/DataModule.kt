package com.bup.ys.daitso.core.data.di

import com.bup.ys.daitso.core.common.Dispatcher
import com.bup.ys.daitso.core.common.DaitsoDispatchers
import com.bup.ys.daitso.core.data.datasource.LocalDataSource
import com.bup.ys.daitso.core.data.datasource.LocalDataSourceImpl
import com.bup.ys.daitso.core.data.repository.CartRepository
import com.bup.ys.daitso.core.data.repository.CartRepositoryImpl
import com.bup.ys.daitso.core.data.repository.ProductRepository
import com.bup.ys.daitso.core.data.repository.ProductRepositoryImpl
import com.bup.ys.daitso.core.network.NetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt Module for data layer dependency injection.
 *
 * Provides Repository implementations, binds interfaces to implementations,
 * and provides coroutine dispatchers for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindLocalDataSource(
        impl: LocalDataSourceImpl
    ): LocalDataSource

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    companion object {

        /**
         * Provides ProductRepository implementation.
         *
         * @param networkDataSource The network data source
         * @param localDataSource The local data source
         * @param ioDispatcher The IO dispatcher
         * @return ProductRepository instance
         */
        @Singleton
        @Provides
        fun provideProductRepository(
            networkDataSource: NetworkDataSource,
            localDataSource: LocalDataSource,
            @Dispatcher(DaitsoDispatchers.IO) ioDispatcher: CoroutineDispatcher
        ): ProductRepository {
            return ProductRepositoryImpl(networkDataSource, localDataSource, ioDispatcher)
        }

        /**
         * Provides IO Dispatcher for network and database operations.
         *
         * @return IO CoroutineDispatcher
         */
        @Singleton
        @Provides
        @Dispatcher(DaitsoDispatchers.IO)
        fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

        /**
         * Provides Default Dispatcher for CPU-intensive operations.
         *
         * @return Default CoroutineDispatcher
         */
        @Singleton
        @Provides
        @Dispatcher(DaitsoDispatchers.Default)
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        /**
         * Provides Main Dispatcher for UI operations.
         *
         * @return Main CoroutineDispatcher
         */
        @Singleton
        @Provides
        @Dispatcher(DaitsoDispatchers.Main)
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    }
}
