package com.splitsmart.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Records the resolved share amount for each participant in an expense.
 * shareAmount is always the actual currency amount (pre-calculated from split type).
 *
 * Composite PK prevents a user being recorded twice for the same expense.
 */
@Entity(
    tableName = "expense_participants",
    primaryKeys = ["expenseId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId")]
)
data class ExpenseParticipant(
    val expenseId: String,
    val userId: String,
    val shareAmount: Double  // actual amount this user owes for this expense
)
