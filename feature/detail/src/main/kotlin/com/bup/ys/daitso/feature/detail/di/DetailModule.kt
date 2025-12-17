package com.bup.ys.daitso.feature.detail.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt Dependency Injection module for the Detail feature.
 *
 * Provides bindings for repositories and other dependencies used in the detail feature.
 *
 * Note: CartRepository bindings are provided by core:data module.
 */
@Module
@InstallIn(SingletonComponent::class)
object DetailModule {
    // No bindings needed - CartRepository is provided by core:data
}
