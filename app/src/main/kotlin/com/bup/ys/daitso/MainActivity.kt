package com.bup.ys.daitso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import com.bup.ys.daitso.navigation.DaitsoNavHost

/**
 * Main activity of the Daitso application.
 *
 * This is the entry point for the UI layer. It sets up the Compose content
 * with Material Design 3 theme and hosts the navigation graph.
 *
 * Uses Hilt for dependency injection of required components.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DaitsoNavHost()
            }
        }
    }
}
