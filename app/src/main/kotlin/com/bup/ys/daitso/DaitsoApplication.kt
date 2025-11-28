package com.bup.ys.daitso

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Daitso Application class.
 *
 * This is the entry point of the Daitso application, initialized with Hilt dependency injection.
 * All Hilt-injected components are initialized here.
 */
@HiltAndroidApp
class DaitsoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
