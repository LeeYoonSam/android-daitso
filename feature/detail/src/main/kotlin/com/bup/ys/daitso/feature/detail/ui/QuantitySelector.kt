package com.bup.ys.daitso.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reusable quantity selector component for selecting product quantities.
 *
 * Displays current quantity with increment/decrement buttons.
 * Quantity is clamped between 1 and 999.
 *
 * @param quantity Current quantity value
 * @param onQuantityChange Callback when quantity changes
 * @param modifier Modifier for styling
 * @param minQuantity Minimum allowed quantity (default: 1)
 * @param maxQuantity Maximum allowed quantity (default: 999)
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int = 999
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Decrease button
        QuantitySelectorTextButton(
            text = "-",
            contentDescription = "Decrease quantity",
            onClick = {
                val newQuantity = (quantity - 1).coerceAtLeast(minQuantity)
                onQuantityChange(newQuantity)
            },
            testTag = "quantity_decrement_button",
            enabled = quantity > minQuantity
        )

        // Quantity display
        Text(
            text = quantity.toString(),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(width = 40.dp, height = 24.dp)
                .testTag("quantity_display"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        // Increase button
        QuantitySelectorTextButton(
            text = "+",
            contentDescription = "Increase quantity",
            onClick = {
                val newQuantity = (quantity + 1).coerceAtMost(maxQuantity)
                onQuantityChange(newQuantity)
            },
            testTag = "quantity_increment_button",
            enabled = quantity < maxQuantity
        )
    }
}

/**
 * Text button component used within QuantitySelector.
 */
@Composable
private fun QuantitySelectorTextButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String,
    enabled: Boolean = true
) {
    Text(
        text = text,
        modifier = Modifier
            .size(width = 36.dp, height = 36.dp)
            .testTag(testTag)
            .clickable(enabled = enabled) { onClick() }
            .padding(8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center
    )
}
