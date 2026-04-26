package com.splitsmart.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** How the expense amount is divided among participants. */
enum class SplitType {
    EQUAL,       // amount / participants.size for everyone
    UNEQUAL,     // custom amounts per participant (must sum to total)
    PERCENTAGE   // percentage per participant (must sum to 100)
}

/** Categorisation for analytics and filtering. */
enum class ExpenseCategory {
    FOOD, TRANSPORT, ACCOMMODATION, ENTERTAINMENT, UTILITIES, SHOPPING, HEALTH, OTHER
}

/**
 * Represents an expense within a group.
 *
 * @param paidById  the userId of the person who physically paid
 * @param splitType determines how [ExpenseParticipant.shareAmount] was calculated
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class Expense(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidById: String,
    val splitType: SplitType = SplitType.EQUAL,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val timestamp: Long = System.currentTimeMillis()
)
