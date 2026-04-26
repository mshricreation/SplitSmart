package com.splitsmart.app.utils

import com.splitsmart.app.data.model.SplitType
import kotlin.math.roundToLong

/**
 * Calculates per-participant share amounts for the three split modes.
 *
 * All methods return a Map<userId, shareAmount> where values are rounded to
 * 2 decimal places and the last participant absorbs any floating-point remainder
 * so the shares always sum exactly to [totalAmount].
 */
object SplitCalculator {

    /**
     * EQUAL split — every participant pays the same share.
     */
    fun calculateEqualSplit(
        totalAmount: Double,
        participantIds: List<String>
    ): Map<String, Double> {
        if (participantIds.isEmpty()) return emptyMap()

        val rawShare = totalAmount / participantIds.size
        val roundedShare = rawShare.roundTo2Decimals()

        val result = mutableMapOf<String, Double>()
        var runningTotal = 0.0

        participantIds.forEachIndexed { index, userId ->
            if (index == participantIds.lastIndex) {
                // Last participant gets the remainder to correct floating-point drift
                result[userId] = (totalAmount - runningTotal).roundTo2Decimals()
            } else {
                result[userId] = roundedShare
                runningTotal += roundedShare
            }
        }
        return result
    }

    /**
     * UNEQUAL split — caller provides custom amounts that must sum to [totalAmount].
     * Validates that the sum matches (within ₹0.01 tolerance) and throws otherwise.
     */
    fun calculateUnequalSplit(
        totalAmount: Double,
        customAmounts: Map<String, Double>
    ): Map<String, Double> {
        val sum = customAmounts.values.sum()
        require(kotlin.math.abs(sum - totalAmount) < 0.01) {
            "Custom split amounts (${sum}) must sum to total (${totalAmount})"
        }
        return customAmounts.mapValues { it.value.roundTo2Decimals() }
    }

    /**
     * PERCENTAGE split — caller provides percentages that must sum to 100.
     * Converts percentages to actual monetary amounts.
     */
    fun calculatePercentageSplit(
        totalAmount: Double,
        percentages: Map<String, Double>
    ): Map<String, Double> {
        val sum = percentages.values.sum()
        require(kotlin.math.abs(sum - 100.0) < 0.01) {
            "Percentages (${sum}) must sum to 100"
        }

        val ids = percentages.keys.toList()
        val result = mutableMapOf<String, Double>()
        var runningTotal = 0.0

        ids.forEachIndexed { index, userId ->
            val pct = percentages[userId] ?: 0.0
            if (index == ids.lastIndex) {
                result[userId] = (totalAmount - runningTotal).roundTo2Decimals()
            } else {
                val share = (totalAmount * pct / 100.0).roundTo2Decimals()
                result[userId] = share
                runningTotal += share
            }
        }
        return result
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    /** Rounds to 2 decimal places without accumulating floating-point error. */
    private fun Double.roundTo2Decimals(): Double =
        (this * 100).roundToLong() / 100.0
}
