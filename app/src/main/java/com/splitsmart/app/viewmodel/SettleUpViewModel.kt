package com.splitsmart.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.Settlement
import com.splitsmart.app.data.model.User
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.data.repository.SettlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettleUpUiState(
    val members: List<User> = emptyList(),
    val fromUserId: String = "",
    val toUserId: String = "",
    val amount: String = "",
    val note: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val settlementRepository: SettlementRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    // Pre-fill from and to when navigated with suggested transaction values
    private val preFilledFrom: String? = savedStateHandle["fromUserId"]
    private val preFilledTo: String?   = savedStateHandle["toUserId"]
    private val preFilledAmt: String?  = savedStateHandle["amount"]

    private val _uiState = MutableStateFlow(SettleUpUiState())
    val uiState: StateFlow<SettleUpUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            groupRepository.getMembersOfGroup(groupId).collect { members ->
                _uiState.update {
                    it.copy(
                        members = members,
                        fromUserId = preFilledFrom ?: (if (members.isNotEmpty()) members.first().id else ""),
                        toUserId   = preFilledTo   ?: (if (members.size > 1) members[1].id else ""),
                        amount     = preFilledAmt  ?: ""
                    )
                }
            }
        }
    }

    fun onFromUserChange(userId: String) = _uiState.update { it.copy(fromUserId = userId) }
    fun onToUserChange(userId: String)   = _uiState.update { it.copy(toUserId = userId) }
    fun onAmountChange(value: String)    = _uiState.update { it.copy(amount = value) }
    fun onNoteChange(value: String)      = _uiState.update { it.copy(note = value) }

    fun recordSettlement() {
        val state = _uiState.value
        val amt = state.amount.toDoubleOrNull()

        when {
            state.fromUserId.isEmpty() || state.toUserId.isEmpty() ->
                return _uiState.update { it.copy(error = "Please select both users") }
            state.fromUserId == state.toUserId ->
                return _uiState.update { it.copy(error = "From and To must be different users") }
            amt == null || amt <= 0 ->
                return _uiState.update { it.copy(error = "Please enter a valid amount") }
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val settlement = Settlement(
                    id = UUID.randomUUID().toString(),
                    groupId = groupId,
                    fromUserId = state.fromUserId,
                    toUserId = state.toUserId,
                    amount = amt!!,
                    note = state.note.trim()
                )
                settlementRepository.insertSettlement(settlement)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
