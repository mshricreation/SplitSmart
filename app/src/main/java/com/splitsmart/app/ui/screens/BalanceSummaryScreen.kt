package com.splitsmart.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitsmart.app.ui.components.BalanceRow
import com.splitsmart.app.ui.components.MemberAvatar
import com.splitsmart.app.ui.theme.NegativeRed
import com.splitsmart.app.ui.theme.PositiveGreen
import com.splitsmart.app.utils.Transaction
import com.splitsmart.app.viewmodel.BalanceSummaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSummaryScreen(
    onNavigateBack: () -> Unit,
    onSettleUp: (fromUserId: String, toUserId: String, amount: String) -> Unit,
    viewModel: BalanceSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Balances", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Net Balances section ─────────────────────────────────────────
            item {
                Text(
                    "Net Balances",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.balances.isEmpty()) {
                item {
                    Text(
                        "No expenses yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            uiState.balances.forEachIndexed { idx, balance ->
                                val name = uiState.userNameMap[balance.userId] ?: "Unknown"
                                BalanceRow(balance = balance, userName = name)
                                if (idx < uiState.balances.lastIndex)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // ── Smart Settlement section ─────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Smart Settlement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Minimum transactions to settle all debts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.suggestedTransactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = PositiveGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Everyone is settled up! 🎉",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(uiState.suggestedTransactions, key = { "${it.from}-${it.to}" }) { txn ->
                    SmartTransactionCard(
                        transaction = txn,
                        nameMap = uiState.userNameMap,
                        onSettleClick = {
                            onSettleUp(txn.from, txn.to, "%.2f".format(txn.amount))
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SmartTransactionCard(
    transaction: Transaction,
    nameMap: Map<String, String>,
    onSettleClick: () -> Unit
) {
    val fromName = nameMap[transaction.from] ?: transaction.from
    val toName   = nameMap[transaction.to]   ?: transaction.to

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From side
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                MemberAvatar(name = fromName, size = 40.dp)
                Spacer(Modifier.height(4.dp))
                Text(fromName, style = MaterialTheme.typography.labelSmall,
                    maxLines = 1)
            }

            // Arrow + amount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "₹${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NegativeRed
                )
                Icon(
                    Icons.Default.ArrowForward, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // To side
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                MemberAvatar(name = toName, size = 40.dp)
                Spacer(Modifier.height(4.dp))
                Text(toName, style = MaterialTheme.typography.labelSmall,
                    maxLines = 1)
            }

            Spacer(Modifier.width(12.dp))

            // Settle button
            FilledTonalButton(
                onClick = onSettleClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Settle", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
