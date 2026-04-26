package com.splitsmart.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records a manual settlement payment between two users in a group.
 * fromUserId pays toUserId the given amount to reduce their debt.
 */
@Entity(
    tableName = "settlements",
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
data class Settlement(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromUserId: String,  // who paid
    val toUserId: String,    // who received
    val amount: Double,
    val settledAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
