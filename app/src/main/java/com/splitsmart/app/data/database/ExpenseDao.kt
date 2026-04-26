package com.splitsmart.app.data.database

import androidx.room.*
import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.ExpenseParticipant
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // ── Expense CRUD ────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<ExpenseParticipant>)

    /** Inserts expense + all its participants atomically. */
    @Transaction
    suspend fun insertExpenseWithParticipants(
        expense: Expense,
        participants: List<ExpenseParticipant>
    ) {
        insertExpense(expense)
        insertParticipants(participants)
    }

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getExpensesForGroup(groupId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): Expense?

    @Query("SELECT * FROM expense_participants WHERE expenseId = :expenseId")
    suspend fun getParticipantsForExpense(expenseId: String): List<ExpenseParticipant>

    /** Returns all participants for all expenses in a group — used for balance calculation. */
    @Query(
        """
        SELECT ep.* FROM expense_participants ep
        INNER JOIN expenses e ON ep.expenseId = e.id
        WHERE e.groupId = :groupId
        """
    )
    suspend fun getAllParticipantsForGroup(groupId: String): List<ExpenseParticipant>

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense_participants WHERE expenseId = :expenseId")
    suspend fun deleteParticipantsForExpense(expenseId: String)
}
