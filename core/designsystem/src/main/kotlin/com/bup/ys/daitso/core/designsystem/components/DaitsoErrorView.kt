package com.bup.ys.daitso.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bup.ys.daitso.core.designsystem.theme.DaitsoTheme

/**
 * Daitso error view component.
 *
 * Displays an error message with Material 3 error styling.
 *
 * @param message The error message to display
 * @param modifier Modifier for customization
 * @param onRetry Optional callback for a retry button
 */
@Composable
fun DaitsoErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onRetry != null) {
                DaitsoButton(
                    text = "Retry",
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/**
 * Daitso error view preview.
 */
@Composable
fun DaitsoErrorViewPreview() {
    DaitsoTheme {
        DaitsoErrorView(
            message = "An error occurred. Please try again.",
            onRetry = { }
        )
    }
}
