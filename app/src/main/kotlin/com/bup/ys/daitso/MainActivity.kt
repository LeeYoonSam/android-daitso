package com.bup.ys.daitso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.bup.ys.daitso.core.designsystem.theme.DaitsoTheme
import com.bup.ys.daitso.navigation.DaitsoNavHost

/**
 * Main activity of the Daitso application.
 *
 * This is the entry point for the UI layer. It sets up the Compose content
 * with Daitso custom theme and hosts the navigation graph.
 *
 * Uses Hilt for dependency injection of required components.
 * Enables edge-to-edge display for full screen support.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaitsoTheme {
                DaitsoNavHost()
            }
        }
    }
}
