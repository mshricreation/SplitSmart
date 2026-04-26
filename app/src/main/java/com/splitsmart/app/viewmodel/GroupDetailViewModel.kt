package com.splitsmart.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.ExpenseRepository
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val allUsers: List<User> = emptyList(),      // for "add member" picker
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // groupId is passed via navigation as a SavedStateHandle arg
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
        observeMembers()
        observeExpenses()
        observeAllUsers()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            val group = groupRepository.getGroupById(groupId)
            _uiState.update { it.copy(group = group) }
        }
    }

    private fun observeMembers() {
        viewModelScope.launch {
            groupRepository.getMembersOfGroup(groupId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { members ->
                    _uiState.update { it.copy(members = members, isLoading = false) }
                }
        }
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            expenseRepository.getExpensesForGroup(groupId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { expenses ->
                    _uiState.update { it.copy(expenses = expenses) }
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
