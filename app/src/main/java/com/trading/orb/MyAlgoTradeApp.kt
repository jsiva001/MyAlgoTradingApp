package com.trading.orb

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyAlgoTradeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        initializeLogging()

        // Initialize any other app-wide configurations here
        initializeAppConfigurations()
    }

    private fun initializeLogging() {
        // Plant a debug tree for logging in debug builds
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeAppConfigurations() {
        // Initialize app configurations
        // This can include API initialization, analytics setup, crash reporting, etc.
    }
}

