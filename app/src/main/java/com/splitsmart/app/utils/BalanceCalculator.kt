package com.splitsmart.app.utils

import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.ExpenseParticipant
import com.splitsmart.app.data.model.Settlement
import kotlin.math.roundToLong

/** Per-user balance result. Positive = user is owed money; negative = user owes money. */
data class UserBalance(
    val userId: String,
    val netAmount: Double   // +ve → to receive; -ve → owes
)

/**
 * Computes net balances for all members in a group.
 *
 * Logic:
 *  For each expense:
 *    - payer is CREDITED the full amount
 *    - each participant is DEBITED their shareAmount
 *
 *  For each settlement:
 *    - fromUser is CREDITED (they paid back)
 *    - toUser is DEBITED (they were repaid)
 *
 *  Net balance = total credits − total debits
 */
object BalanceCalculator {

    fun calculate(
        memberIds: List<String>,
        expenses: List<Expense>,
        participants: List<ExpenseParticipant>,
        settlements: List<Settlement>
    ): List<UserBalance> {
        // Running tally: userId → net amount
        val balanceMap = mutableMapOf<String, Double>()
        memberIds.forEach { balanceMap[it] = 0.0 }

        // Build quick lookup: expenseId → paidById
        val payerMap = expenses.associate { it.id to it.paidById }

        // Process expense participants
        participants.forEach { ep ->
            val payerId = payerMap[ep.expenseId] ?: return@forEach

            // Payer gets credited the share amount
            balanceMap[payerId] = (balanceMap[payerId] ?: 0.0) + ep.shareAmount

            // Participant gets debited their share
            balanceMap[ep.userId] = (balanceMap[ep.userId] ?: 0.0) - ep.shareAmount
        }

        // Process settlements — payer reduces net debt, receiver reduces net credit
        settlements.forEach { s ->
            balanceMap[s.fromUserId] = (balanceMap[s.fromUserId] ?: 0.0) + s.amount
            balanceMap[s.toUserId] = (balanceMap[s.toUserId] ?: 0.0) - s.amount
        }

        return balanceMap.map { (userId, amount) ->
            UserBalance(userId = userId, netAmount = amount.roundTo2Decimals())
        }
    }

    private fun Double.roundTo2Decimals(): Double =
        (this * 100).roundToLong() / 100.0
}
