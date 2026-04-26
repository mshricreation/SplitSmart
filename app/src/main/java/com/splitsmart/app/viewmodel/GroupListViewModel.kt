package com.splitsmart.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GroupListUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    init {
        observeGroups()
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { groups ->
                    _uiState.update { it.copy(groups = groups, isLoading = false) }
                }
        }
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
