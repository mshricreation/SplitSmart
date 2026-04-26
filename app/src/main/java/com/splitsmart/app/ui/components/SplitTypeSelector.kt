package com.splitsmart.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitsmart.app.data.model.SplitType

/**
 * Segmented-button control to switch between split types.
 */
@Composable
fun SplitTypeSelector(
    selected: SplitType,
    onSelect: (SplitType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = SplitType.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onSelect(type) },
                selected = selected == type,
                label = {
                    Text(
                        text = when (type) {
                            SplitType.EQUAL      -> "Equal"
                            SplitType.UNEQUAL    -> "Custom"
                            SplitType.PERCENTAGE -> "%"
                        }
                    )
                }
            )
        }
    }
}
