package com.splitsmart.app.data.repository

import com.splitsmart.app.data.database.ExpenseDao
import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.ExpenseParticipant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getExpensesForGroup(groupId: String): Flow<List<Expense>> =
        expenseDao.getExpensesForGroup(groupId)

    suspend fun getExpenseById(expenseId: String): Expense? =
        expenseDao.getExpenseById(expenseId)

    suspend fun insertExpenseWithParticipants(
        expense: Expense,
        participants: List<ExpenseParticipant>
    ) = expenseDao.insertExpenseWithParticipants(expense, participants)

    suspend fun getParticipantsForExpense(expenseId: String): List<ExpenseParticipant> =
        expenseDao.getParticipantsForExpense(expenseId)

    /** Fetches all participants for every expense in a group — used for bulk balance calc. */
    suspend fun getAllParticipantsForGroup(groupId: String): List<ExpenseParticipant> =
        expenseDao.getAllParticipantsForGroup(groupId)

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteParticipantsForExpense(expense.id)
        expenseDao.deleteExpense(expense)
    }
}
