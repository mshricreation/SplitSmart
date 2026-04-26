package com.splitsmart.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.splitsmart.app.ui.theme.NegativeRed
import com.splitsmart.app.ui.theme.NeutralGray
import com.splitsmart.app.ui.theme.PositiveGreen
import com.splitsmart.app.utils.UserBalance
import kotlin.math.abs

/**
 * Displays a user's net balance in colour-coded form.
 *   +ve → green  "gets back ₹X"
 *   -ve → red    "owes ₹X"
 *   0   → gray   "settled up"
 */
@Composable
fun BalanceRow(
    balance: UserBalance,
    userName: String,
    currency: String = "₹",
    modifier: Modifier = Modifier
) {
    val amount = balance.netAmount
    val (label, color) = when {
        amount > 0.005  -> "gets back $currency${String.format("%.2f", amount)}" to PositiveGreen
        amount < -0.005 -> "owes $currency${String.format("%.2f", abs(amount))}" to NegativeRed
        else            -> "settled up" to NeutralGray
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(name = userName, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
