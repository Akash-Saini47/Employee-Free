package com.salarywise.app.domain.model

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val indianFormat = DecimalFormat("##,##,##,##,###")

    fun formatInr(amount: Double, showDecimals: Boolean = false): String {
        val sign = if (amount < 0) "-" else ""
        val absAmount = Math.abs(amount)
        return if (showDecimals) {
            val df = DecimalFormat("##,##,##,##,##0.00")
            "$sign₹${df.format(absAmount)}"
        } else {
            val formatted = indianFormat.format(absAmount.toLong())
            "$sign₹$formatted"
        }
    }

    fun formatPercent(value: Double): String {
        val df = DecimalFormat("0.#")
        return "${df.format(value)}%"
    }
}
