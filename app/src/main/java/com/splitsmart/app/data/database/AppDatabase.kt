package com.splitsmart.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.splitsmart.app.data.model.*

/**
 * Singleton Room database for SplitSmart.
 * Increment version and add a Migration whenever the schema changes.
 */
@Database(
    entities = [
        User::class,
        Group::class,
        GroupMember::class,
        Expense::class,
        ExpenseParticipant::class,
        Settlement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settlementDao(): SettlementDao
}
