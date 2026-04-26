package com.splitsmart.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsmart.app.data.model.*
import com.splitsmart.app.data.repository.ExpenseRepository
import com.splitsmart.app.data.repository.GroupRepository
import com.splitsmart.app.utils.SplitCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddExpenseUiState(
    val members: List<User> = emptyList(),
    val description: String = "",
    val amount: String = "",
    val paidById: String = "",
    val selectedParticipants: Set<String> = emptySet(),
    val splitType: SplitType = SplitType.EQUAL,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    // For UNEQUAL: userId → custom amount string
    val customAmounts: Map<String, String> = emptyMap(),
    // For PERCENTAGE: userId → percentage string
    val customPercentages: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        observeMembers()
    }

    private fun observeMembers() {
        viewModelScope.launch {
            groupRepository.getMembersOfGroup(groupId).collect { members ->
                _uiState.update {
                    it.copy(
                        members = members,
                        // Default: all members participate
                        selectedParticipants = members.map { m -> m.id }.toSet(),
                        paidById = if (it.paidById.isEmpty() && members.isNotEmpty())
                            members.first().id else it.paidById
                    )
                }
            }
        }
    }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onPaidByChange(userId: String) = _uiState.update { it.copy(paidById = userId) }
    fun onCategoryChange(cat: ExpenseCategory) = _uiState.update { it.copy(category = cat) }

    fun onSplitTypeChange(type: SplitType) = _uiState.update { it.copy(splitType = type) }

    fun onParticipantToggle(userId: String) {
        _uiState.update { state ->
            val updated = state.selectedParticipants.toMutableSet().apply {
                if (contains(userId)) remove(userId) else add(userId)
            }
            state.copy(selectedParticipants = updated)
        }
    }

    fun onCustomAmountChange(userId: String, value: String) {
        _uiState.update { state ->
            state.copy(customAmounts = state.customAmounts + (userId to value))
        }
    }

    fun onCustomPercentageChange(userId: String, value: String) {
        _uiState.update { state ->
            state.copy(customPercentages = state.customPercentages + (userId to value))
        }
    }

    fun saveExpense() {
        val state = _uiState.value
        val totalAmount = state.amount.toDoubleOrNull()

        // ── Validation ──────────────────────────────────────────────────────
        when {
            state.description.isBlank() ->
                return _uiState.update { it.copy(error = "Please enter a description") }
            totalAmount == null || totalAmount <= 0 ->
                return _uiState.update { it.copy(error = "Please enter a valid amount") }
            state.paidById.isEmpty() ->
                return _uiState.update { it.copy(error = "Please select who paid") }
            state.selectedParticipants.isEmpty() ->
                return _uiState.update { it.copy(error = "Select at least one participant") }
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val participants = state.selectedParticipants.toList()

                // ── Calculate shares based on split type ────────────────────
                val shares: Map<String, Double> = when (state.splitType) {
                    SplitType.EQUAL -> SplitCalculator.calculateEqualSplit(totalAmount!!, participants)

                    SplitType.UNEQUAL -> {
                        val amounts = participants.associate { id ->
                            id to (state.customAmounts[id]?.toDoubleOrNull() ?: 0.0)
                        }
                        SplitCalculator.calculateUnequalSplit(totalAmount!!, amounts)
                    }

                    SplitType.PERCENTAGE -> {
                        val pcts = participants.associate { id ->
                            id to (state.customPercentages[id]?.toDoubleOrNull() ?: 0.0)
                        }
                        SplitCalculator.calculatePercentageSplit(totalAmount!!, pcts)
                    }
                }

                val expenseId = UUID.randomUUID().toString()
                val expense = Expense(
                    id = expenseId,
                    groupId = groupId,
                    description = state.description.trim(),
                    amount = totalAmount!!,
                    paidById = state.paidById,
                    splitType = state.splitType,
                    category = state.category
                )

                val expenseParticipants = shares.map { (userId, share) ->
                    ExpenseParticipant(expenseId = expenseId, userId = userId, shareAmount = share)
                }

                expenseRepository.insertExpenseWithParticipants(expense, expenseParticipants)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
