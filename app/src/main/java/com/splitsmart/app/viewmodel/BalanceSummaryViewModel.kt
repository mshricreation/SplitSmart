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

                    _uiState.update {
                        it.copy(
                            balances = balances,
                            suggestedTransactions = transactions,
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

    fun clearError() = _uiState.update { it.copy(error = null) }
}
