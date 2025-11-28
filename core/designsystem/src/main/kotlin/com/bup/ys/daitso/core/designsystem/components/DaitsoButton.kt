package com.bup.ys.daitso.core.designsystem.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bup.ys.daitso.core.designsystem.theme.DaitsoTheme

/**
 * Daitso primary button component.
 *
 * A Material 3 button styled with Daitso theme colors.
 *
 * @param text The text displayed on the button
 * @param onClick Callback when the button is clicked
 * @param modifier Modifier for customization
 * @param enabled Whether the button is enabled
 */
@Composable
fun DaitsoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text)
    }
}

/**
 * Daitso button preview.
 */
@Composable
fun DaitsoButtonPreview() {
    DaitsoTheme {
        DaitsoButton(
            text = "Click Me",
            onClick = { }
        )
    }
}
