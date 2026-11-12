package com.livesignalmap.mobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livesignalmap.mobile.ui.model.NetworkMode

@Composable
fun NetworkModeSelector(
    selectedMode: NetworkMode,
    onModeSelected: (NetworkMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        NetworkMode.WIFI to "Wi-Fi",
        NetworkMode.MOBILE to "Мобильная сеть"
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        modes.forEachIndexed { index, (mode, title) ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = modes.size
                ),
                label = {
                    Text(title)
                }
            )
        }
    }
}