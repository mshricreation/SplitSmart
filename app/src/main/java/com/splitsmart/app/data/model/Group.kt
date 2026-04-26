package com.splitsmart.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a group (e.g., "Goa Trip", "Flat Expenses").
 * currency is stored as an ISO 4217 code (default INR) — ready for multi-currency Phase 3.
 */
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val currency: String = "INR"
)
