package com.bup.ys.daitso.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bup.ys.daitso.core.designsystem.theme.DaitsoTheme

/**
 * Daitso loading indicator component.
 *
 * A Material 3 circular progress indicator styled with Daitso theme colors.
 * Centered on the screen.
 *
 * @param modifier Modifier for customization
 */
@Composable
fun DaitsoLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Daitso loading indicator preview.
 */
@Composable
fun DaitsoLoadingIndicatorPreview() {
    DaitsoTheme {
        DaitsoLoadingIndicator()
    }
}
