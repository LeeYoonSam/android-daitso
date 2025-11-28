package com.bup.ys.daitso.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Hilt Module for network dependency injection.
 *
 * Provides Retrofit, OkHttp, and API service instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides OkHttp client with logging interceptor.
     *
     * @return Configured OkHttpClient
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * Provides Retrofit instance configured with OkHttp and Kotlin Serialization.
     *
     * @param okHttpClient The OkHttpClient to use
     * @return Configured Retrofit instance
     */
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
        }

        return Retrofit.Builder()
            .baseUrl("https://api.daitso.com/") // Replace with actual API URL
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Provides DaitsoApiService instance.
     *
     * @param retrofit The Retrofit instance
     * @return Configured API service
     */
    @Singleton
    @Provides
    fun provideDaitsoApiService(retrofit: Retrofit): DaitsoApiService {
        return retrofit.create(DaitsoApiService::class.java)
    }

    /**
     * Provides NetworkDataSource implementation.
     *
     * @param apiService The API service instance
     * @return NetworkDataSource implementation
     */
    @Singleton
    @Provides
    fun provideNetworkDataSource(apiService: DaitsoApiService): NetworkDataSource {
        return NetworkDataSourceImpl(apiService)
    }
}
