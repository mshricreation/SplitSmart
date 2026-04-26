package com.splitsmart.app.data.database

import androidx.room.TypeConverter
import com.splitsmart.app.data.model.ExpenseCategory
import com.splitsmart.app.data.model.SplitType

/**
 * Room type converters for enums.
 * Enums are stored as their name strings for readability in the DB.
 */
class Converters {

    @TypeConverter
    fun fromSplitType(value: SplitType): String = value.name

    @TypeConverter
    fun toSplitType(value: String): SplitType = SplitType.valueOf(value)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
