package com.bup.ys.daitso.core.designsystem.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bup.ys.daitso.core.designsystem.theme.DaitsoTheme

/**
 * Daitso text input field component.
 *
 * A Material 3 outlined text field styled with Daitso theme colors.
 *
 * @param value The current text value
 * @param onValueChange Callback when the text value changes
 * @param label The label text displayed above the field
 * @param modifier Modifier for customization
 * @param placeholder Optional placeholder text
 */
@Composable
fun DaitsoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else {
            null
        },
        modifier = modifier,
        singleLine = true
    )
}

/**
 * Daitso text field preview.
 */
@Composable
fun DaitsoTextFieldPreview() {
    val (value, setValue) = remember { mutableStateOf("") }
    DaitsoTheme {
        DaitsoTextField(
            value = value,
            onValueChange = setValue,
            label = "Enter text",
            placeholder = "Type something..."
        )
    }
}
