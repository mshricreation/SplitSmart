package com.splitsmart.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app user.
 * avatarColor is a Material color stored as an Int for consistent avatar display.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val avatarColor: Int = 0 // stored as ARGB Int; generated at creation time
)
