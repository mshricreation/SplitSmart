package com.splitsmart.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.data.repository.UserRepository
import com.splitsmart.app.data.repository.ExpenseRepository
import com.splitsmart.app.data.repository.SettlementRepository
import com.splitsmart.app.utils.BalanceCalculator
import com.splitsmart.app.utils.UserBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GroupListUiState(
    val groups: List<Group> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            groupRepository.getAllGroups()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { groups ->
                    calculateTotalBalance(groups)
                }
        }
    }

    private suspend fun calculateTotalBalance(groups: List<Group>) {
        // For simplicity, let's assume the first user in the DB is the "current user"
        // In a real app, you'd have a logged-in userId.
        val allUsers = userRepository.getAllUsers().first()
        if (allUsers.isEmpty()) {
            _uiState.update { it.copy(groups = groups, isLoading = false) }
            return
        }
        
        val currentUserId = allUsers.first().id
        var totalNet = 0.0

        groups.forEach { group ->
            val members = groupRepository.getMembersOfGroup(group.id).first()
            val expenses = expenseRepository.getExpensesForGroup(group.id).first()
            val settlements = settlementRepository.getSettlementsForGroup(group.id).first()
            val participants = expenseRepository.getAllParticipantsForGroup(group.id)
            
            val balances = BalanceCalculator.calculate(
                memberIds = members.map { it.id },
                expenses = expenses,
                participants = participants,
                settlements = settlements
            )
            
            val userBalance = balances.find { it.userId == currentUserId }?.netAmount ?: 0.0
            totalNet += userBalance
        }

        _uiState.update { it.copy(groups = groups, totalBalance = totalNet, isLoading = false) }
    }

    /**
     * Creates a new group and adds the initial list of member users.
     * Each new user is also persisted if they don't exist yet.
     */
    fun createGroup(name: String, members: List<User>) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Group name cannot be empty") }
            return
        }
        viewModelScope.launch {
            try {
                val groupId = UUID.randomUUID().toString()
                val group = Group(id = groupId, name = name.trim())
                groupRepository.insertGroup(group)

                members.forEach { user ->
                    userRepository.insertUser(user)
                    groupRepository.addMember(groupId, user.id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
