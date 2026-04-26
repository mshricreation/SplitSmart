package com.splitsmart.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitsmart.app.data.model.Expense
import com.splitsmart.app.data.model.User
import com.splitsmart.app.ui.components.ExpenseCard
import com.splitsmart.app.ui.components.MemberAvatar
import androidx.compose.ui.graphics.Color
import com.splitsmart.app.ui.theme.NegativeRed
import com.splitsmart.app.ui.theme.PositiveGreen
import com.splitsmart.app.utils.UserBalance
import kotlin.math.abs
import com.splitsmart.app.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onAddExpense: () -> Unit,
    onViewBalances: () -> Unit,
    onSettleUp: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Expenses", "Balances", "Members")

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            allUsers = uiState.allUsers,
            existingMembers = uiState.members,
            onDismiss = { showAddMemberDialog = false },
            onAddMember = { userId ->
                viewModel.addMember(userId)
                showAddMemberDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.group?.name ?: "Group",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onViewBalances) {
                        Icon(Icons.Default.BarChart, "View Balances")
                    }
                    IconButton(onClick = onSettleUp) {
                        Icon(Icons.Default.CheckCircle, "Settle Up")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    ExtendedFloatingActionButton(
                        onClick = onAddExpense,
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text("Add Expense") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                2 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddMemberDialog = true },
                        icon = { Icon(Icons.Default.PersonAdd, null) },
                        text = { Text("Add Member") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Balance Summary Card ────────────────────────────────────────
            BalanceSummaryHeader(balances = uiState.balances)

            // ── Tab row ─────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ExpensesTab(
                    expenses = uiState.expenses,
                    members = uiState.members,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                1 -> BalancesTab(
                    balances = uiState.balances,
                    members = uiState.members,
                    onViewDetailedBalances = onViewBalances
                )
                2 -> MembersTab(
                    members = uiState.members,
                    onRemoveMember = { viewModel.removeMember(it) }
                )
            }
        }
    }
}

@Composable
private fun BalanceSummaryHeader(balances: List<UserBalance>) {
    val totalOwed = balances.filter { it.netAmount < 0 }.sumOf { abs(it.netAmount) }
    
    if (balances.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Group Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${String.format("%.2f", totalOwed)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Settle Debts",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    allUsers: List<User>,
    existingMembers: List<User>,
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit
) {
    val existingIds = existingMembers.map { it.id }.toSet()
    val availableUsers = allUsers.filter { it.id !in existingIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (availableUsers.isEmpty()) {
                    Text("No other users available to add.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(availableUsers) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAddMember(user.id) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberAvatar(name = user.name, size = 36.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExpensesTab(
    expenses: List<Expense>,
    members: List<User>,
    onDeleteExpense: (Expense) -> Unit
) {
    val memberNameMap = members.associate { it.id to it.name }

    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Receipt, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(12.dp))
                Text("No expenses yet", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tap + to add the first one", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            var showDeleteDialog by remember { mutableStateOf(false) }

            ExpenseCard(
                expense = expense,
                paidByName = memberNameMap[expense.paidById] ?: "Unknown",
                onClick = { showDeleteDialog = true }
            )

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Expense?") },
                    text = { Text("Are you sure you want to delete \"${expense.description}\"?") },
                    confirmButton = {
                        TextButton(onClick = { onDeleteExpense(expense); showDeleteDialog = false }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun BalancesTab(
    balances: List<UserBalance>,
    members: List<User>,
    onViewDetailedBalances: () -> Unit
) {
    val memberNameMap = members.associate { it.id to it.name }

    if (balances.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No balances to show", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(balances, key = { it.userId }) { balance ->
            val amount = balance.netAmount
            val name = memberNameMap[balance.userId] ?: "Unknown"
            
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(name = name, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    
                    Column(horizontalAlignment = Alignment.End) {
                        val color = when {
                            amount > 0.005 -> PositiveGreen
                            amount < -0.005 -> NegativeRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val text = when {
                            amount > 0.005 -> "gets back ₹${String.format("%.2f", amount)}"
                            amount < -0.005 -> "owes ₹${String.format("%.2f", abs(amount))}"
                            else -> "settled"
                        }
                        Text(
                            text = text,
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onViewDetailedBalances,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Analytics, null)
                Spacer(Modifier.width(8.dp))
                Text("View Detailed Insights")
            }
        }
    }
}

@Composable
private fun MembersTab(
    members: List<User>,
    onRemoveMember: (String) -> Unit
) {
    if (members.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No members", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(members, key = { it.id }) { member ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(name = member.name, size = 42.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        member.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemoveMember(member.id) }) {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription = "Remove member",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
