package com.splitsmart.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.ExpenseRepository
import com.splitsmart.app.data.repository.GroupRepository
import android.util.Log
import com.splitsmart.app.data.repository.SettlementRepository
import com.splitsmart.app.data.repository.UserRepository
import com.splitsmart.app.utils.BalanceCalculator
import com.splitsmart.app.utils.UserBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val balances: List<UserBalance> = emptyList(),
    val allUsers: List<User> = emptyList(),      // for "add member" picker
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // groupId is passed via navigation as a SavedStateHandle arg
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
        observeData()
        observeAllUsers()
    }

    private fun loadGroup() {
        Log.d("GroupDetailVM", "Loading group: $groupId")
        viewModelScope.launch {
            val group = groupRepository.getGroupById(groupId)
            _uiState.update { it.copy(group = group) }
        }
    }

    private fun observeData() {
        Log.d("GroupDetailVM", "Starting data observation for group: $groupId")
        viewModelScope.launch {
            combine(
                groupRepository.getMembersOfGroup(groupId),
                expenseRepository.getExpensesForGroup(groupId),
                settlementRepository.getSettlementsForGroup(groupId)
            ) { members, expenses, settlements ->
                Log.d("GroupDetailVM", "Data changed: ${members.size} members, ${expenses.size} expenses, ${settlements.size} settlements")
                val participants = expenseRepository.getAllParticipantsForGroup(groupId)
                val memberIds = members.map { it.id }
                val balances = BalanceCalculator.calculate(memberIds, expenses, participants, settlements)
                Triple(members, expenses, balances)
            }.catch { e -> 
                Log.e("GroupDetailVM", "Error observing data", e)
                _uiState.update { it.copy(error = e.message) } 
            }
                .collect { (members, expenses, balances) ->
                    Log.d("GroupDetailVM", "Updating UI state with ${balances.size} balances")
                    _uiState.update { it.copy(
                        members = members,
                        expenses = expenses,
                        balances = balances,
                        isLoading = false
                    ) }
                }
        }
    }

    private fun observeAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers()
                .collect { users -> _uiState.update { it.copy(allUsers = users) } }
        }
    }

    fun addMember(userId: String) {
        viewModelScope.launch {
            try {
                if (!groupRepository.isMember(groupId, userId)) {
                    groupRepository.addMember(groupId, userId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
            groupRepository.removeMember(groupId, userId)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
