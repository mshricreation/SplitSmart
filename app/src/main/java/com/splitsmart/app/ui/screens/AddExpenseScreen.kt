package com.splitsmart.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitsmart.app.data.model.ExpenseCategory
import com.splitsmart.app.data.model.SplitType
import com.splitsmart.app.data.model.User
import com.splitsmart.app.ui.components.MemberAvatar
import com.splitsmart.app.ui.components.SplitTypeSelector
import com.splitsmart.app.ui.components.categoryIcon
import com.splitsmart.app.viewmodel.AddExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveExpense() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else
                            Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Description ─────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Dinner at restaurant") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // ── Amount ──────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // ── Category ────────────────────────────────────────────────────
            item {
                Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                CategoryGrid(
                    selected = uiState.category,
                    onSelect = viewModel::onCategoryChange
                )
            }

            // ── Paid By ─────────────────────────────────────────────────────
            item {
                Text("Paid By", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                PaidBySelector(
                    members = uiState.members,
                    selectedId = uiState.paidById,
                    onSelect = viewModel::onPaidByChange
                )
            }

            // ── Split Type ──────────────────────────────────────────────────
            item {
                Text("Split Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                SplitTypeSelector(
                    selected = uiState.splitType,
                    onSelect = viewModel::onSplitTypeChange
                )
            }

            // ── Participant selection + custom inputs ────────────────────────
            item {
                Text("Participants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            }

            items(uiState.members, key = { it.id }) { member ->
                ParticipantRow(
                    member = member,
                    isSelected = member.id in uiState.selectedParticipants,
                    splitType = uiState.splitType,
                    customAmount = uiState.customAmounts[member.id] ?: "",
                    customPercent = uiState.customPercentages[member.id] ?: "",
                    onToggle = { viewModel.onParticipantToggle(member.id) },
                    onAmountChange = { viewModel.onCustomAmountChange(member.id, it) },
                    onPercentChange = { viewModel.onCustomPercentageChange(member.id, it) }
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.saveExpense() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Expense", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    selected: ExpenseCategory,
    onSelect: (ExpenseCategory) -> Unit
) {
    val categories = ExpenseCategory.entries
    // 4 columns
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { cat ->
                    val isSelected = cat == selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelect(cat) },
                        label = {
                            Text(
                                cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(categoryIcon(cat), null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad remaining cells if row is shorter than 4
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PaidBySelector(
    members: List<User>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        members.forEach { member ->
            val isSelected = member.id == selectedId
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(member.id) },
                label = { Text(member.name) },
                leadingIcon = {
                    MemberAvatar(name = member.name, size = 20.dp)
                }
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    member: User,
    isSelected: Boolean,
    splitType: SplitType,
    customAmount: String,
    customPercent: String,
    onToggle: () -> Unit,
    onAmountChange: (String) -> Unit,
    onPercentChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        MemberAvatar(name = member.name, size = 32.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            member.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Show custom input only when this participant is selected and split is non-equal
        if (isSelected) {
            when (splitType) {
                SplitType.UNEQUAL -> {
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = onAmountChange,
                        label = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                SplitType.PERCENTAGE -> {
                    OutlinedTextField(
                        value = customPercent,
                        onValueChange = onPercentChange,
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                SplitType.EQUAL -> { /* no extra input needed */ }
            }
        }
    }
}
