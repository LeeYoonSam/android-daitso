package com.bup.ys.daitso.feature.cart.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility object for formatting prices with Korean Won currency symbol.
 *
 * Formats prices as: ₩15,000 (with comma separators)
 */
object PriceFormatter {

    private const val KOREAN_WON_SYMBOL = "₩"

    /**
     * Format a price as Korean Won with comma separators.
     *
     * Rounds the price to nearest integer and formats with thousand separators.
     * Example: 15000.0 -> "₩15,000"
     *
     * @param price The price to format (in Korean Won)
     * @return Formatted price string with symbol and comma separators
     */
    fun formatPrice(price: Double): String {
        val roundedPrice = price.toLong()
        val formatter = NumberFormat.getInstance(Locale("ko", "KR"))
        val formattedNumber = formatter.format(roundedPrice)
        return "$KOREAN_WON_SYMBOL$formattedNumber"
    }

    /**
     * Format a price as Korean Won with comma separators.
     *
     * @param price The price to format (in Korean Won)
     * @return Formatted price string with symbol and comma separators
     */
    fun formatPrice(price: Int): String {
        return formatPrice(price.toDouble())
    }

    /**
     * Format a price as Korean Won with comma separators.
     *
     * @param price The price to format (in Korean Won)
     * @return Formatted price string with symbol and comma separators
     */
    fun formatPrice(price: Long): String {
        val formatter = NumberFormat.getInstance(Locale("ko", "KR"))
        val formattedNumber = formatter.format(price)
        return "$KOREAN_WON_SYMBOL$formattedNumber"
    }
}
