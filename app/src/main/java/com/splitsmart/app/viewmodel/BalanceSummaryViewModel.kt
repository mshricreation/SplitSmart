package com.splitsmart.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.ExpenseRepository
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.data.repository.SettlementRepository
import com.splitsmart.app.data.repository.UserRepository
import com.splitsmart.app.utils.BalanceCalculator
import com.splitsmart.app.utils.SmartSettlementAlgorithm
import com.splitsmart.app.utils.Transaction
import com.splitsmart.app.utils.UserBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BalanceSummaryUiState(
    val balances: List<UserBalance> = emptyList(),
    val suggestedTransactions: List<Transaction> = emptyList(),
    val directTransactions: List<Transaction> = emptyList(),
    val isSimplified: Boolean = true,
    val userNameMap: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class BalanceSummaryViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(BalanceSummaryUiState())
    val uiState: StateFlow<BalanceSummaryUiState> = _uiState.asStateFlow()

    init {
        // Re-compute balances whenever expenses or settlements change
        viewModelScope.launch {
            combine(
                expenseRepository.getExpensesForGroup(groupId),
                settlementRepository.getSettlementsForGroup(groupId),
                groupRepository.getMembersOfGroup(groupId)
            ) { expenses, settlements, members ->
                Triple(expenses, settlements, members)
            }.collect { (expenses, settlements, members) ->
                try {
                    // Build userId → name map for display
                    val nameMap = members.associate { it.id to it.name }

                    // Gather all participant records for this group
                    val participants = expenseRepository.getAllParticipantsForGroup(groupId)

                    val memberIds = members.map { it.id }
                    val balances = BalanceCalculator.calculate(
                        memberIds = memberIds,
                        expenses = expenses,
                        participants = participants,
                        settlements = settlements
                    )

                    // Run smart settlement to get the minimised transaction list
                    val transactions = SmartSettlementAlgorithm.minimiseTransactions(balances)

                    // Calculate direct transactions (unsimplified)
                    val directTxns = calculateDirectTransactions(expenses, participants, settlements)

                    _uiState.update {
                        it.copy(
                            balances = balances,
                            suggestedTransactions = transactions,
                            directTransactions = directTxns,
                            userNameMap = nameMap,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
        }
    }

    fun toggleSimplify() {
        _uiState.update { it.copy(isSimplified = !it.isSimplified) }
    }

    private fun calculateDirectTransactions(
        expenses: List<com.splitsmart.app.data.model.Expense>,
        participants: List<com.splitsmart.app.data.model.ExpenseParticipant>,
        settlements: List<com.splitsmart.app.data.model.Settlement>
    ): List<Transaction> {
        val debtMap = mutableMapOf<Pair<String, String>, Double>() // (From, To) -> Amount

        val expenseMap = expenses.associateBy { it.id }
        
        // 1. Add debts from expenses
        participants.forEach { p ->
            val expense = expenseMap[p.expenseId] ?: return@forEach
            if (p.userId != expense.paidById) {
                val pair = p.userId to expense.paidById
                debtMap[pair] = (debtMap[pair] ?: 0.0) + p.shareAmount
            }
        }

        // 2. Subtract settlements
        settlements.forEach { s ->
            val pair = s.fromUserId to s.toUserId
            debtMap[pair] = (debtMap[pair] ?: 0.0) - s.amount
        }

        // 3. Convert to list and clean up
        return debtMap.map { (pair, amount) ->
            Transaction(from = pair.first, to = pair.second, amount = amount)
        }.filter { it.amount > 0.01 }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
