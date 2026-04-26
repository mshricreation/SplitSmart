package com.splitsmart.app.utils

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * Represents a single optimised payment transaction.
 * [from] pays [to] the given [amount].
 */
data class Transaction(
    val from: String,   // userId of payer
    val to: String,     // userId of receiver
    val amount: Double
)

/**
 * Smart Settlement Algorithm — minimises the number of transactions needed
 * to fully settle all debts within a group.
 *
 * ──────────────────────────────────────────────
 * ALGORITHM  (Greedy Two-Pointer on sorted nets)
 * ──────────────────────────────────────────────
 *  1. Compute net balance for each person (+ve = creditor, -ve = debtor).
 *  2. Separate into two lists: creditors (net > 0) and debtors (net < 0),
 *     sorted by magnitude descending.
 *  3. At each step:
 *       • Take the largest creditor (maxCredit) and largest debtor (maxDebt).
 *       • Settled amount = min(|maxDebt|, maxCredit)
 *       • Record: debtor → creditor for settled amount.
 *       • Reduce both balances; remove if zero.
 *  4. Repeat until no creditors or debtors remain.
 *
 *  Complexity: O(n log n) — at most n-1 transactions for n people.
 *
 *  Example:
 *    A owes B ₹100, B owes C ₹100
 *    → Net: A = -100, B = 0, C = +100
 *    → Result: A pays C ₹100  (1 transaction instead of 2)
 */
object SmartSettlementAlgorithm {

    fun minimiseTransactions(balances: List<UserBalance>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        // Filter out zero balances and convert to mutable working copies
        // creditors: amount > 0 (they are owed)
        // debtors:   amount < 0 (they owe)
        val creditors = ArrayDeque<Pair<String, Double>>()    // userId to +amount
        val debtors = ArrayDeque<Pair<String, Double>>()      // userId to |amount|

        balances.forEach { balance ->
            val rounded = balance.netAmount.roundTo2Decimals()
            when {
                rounded > 0.005  -> creditors.add(balance.userId to rounded)
                rounded < -0.005 -> debtors.add(balance.userId to abs(rounded))
            }
        }

        // Sort descending by amount so we always match the largest parties first
        creditors.sortByDescending { it.second }
        debtors.sortByDescending { it.second }

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditorId, creditAmt) = creditors.removeFirst()
            val (debtorId, debtAmt)    = debtors.removeFirst()

            val settled = min(creditAmt, debtAmt).roundTo2Decimals()

            // Record the transaction: debtor pays creditor
            transactions.add(
                Transaction(from = debtorId, to = creditorId, amount = settled)
            )

            val remainingCredit = (creditAmt - settled).roundTo2Decimals()
            val remainingDebt   = (debtAmt   - settled).roundTo2Decimals()

            // Re-insert non-zero remainders in sorted position
            if (remainingCredit > 0.005) {
                val insertIdx = creditors.indexOfFirst { it.second <= remainingCredit }
                if (insertIdx == -1) creditors.addLast(creditorId to remainingCredit)
                else creditors.add(insertIdx, creditorId to remainingCredit)
            }

            if (remainingDebt > 0.005) {
                val insertIdx = debtors.indexOfFirst { it.second <= remainingDebt }
                if (insertIdx == -1) debtors.addLast(debtorId to remainingDebt)
                else debtors.add(insertIdx, debtorId to remainingDebt)
            }
        }

        return transactions
    }

    private fun Double.roundTo2Decimals(): Double =
        (this * 100).roundToLong() / 100.0
}
