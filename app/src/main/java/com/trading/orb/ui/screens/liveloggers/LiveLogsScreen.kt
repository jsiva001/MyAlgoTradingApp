package com.trading.orb.ui.screens.liveloggers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.components.TimeFormatter
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.event.LiveLogsUiEvent
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.state.LogEntryUiModel
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.utils.LaunchEventCollector
import com.trading.orb.ui.utils.*
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLogsScreen(
    viewModel: LiveLogsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.liveLogsUiState.collectAsStateWithLifecycle()
    
    LaunchEventCollector(eventFlow = viewModel.uiEvent) { event ->
        when (event) {
            is LiveLogsUiEvent.ShowError -> {}
            is LiveLogsUiEvent.ShowSuccess -> {}
            is LiveLogsUiEvent.LogsCleared -> {}
            is LiveLogsUiEvent.LogsExported -> {}
            is LiveLogsUiEvent.LogSelected -> {}
        }
    }
    
    LiveLogsScreenContent(
        uiState = uiState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveLogsScreenContent(
    uiState: LiveLogsUiState = LiveLogsUiState(),
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Live Streaming",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { autoScroll = !autoScroll },
                    label = {
                        Text(
                            text = "Auto-scroll",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = if (autoScroll) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (autoScroll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Logs list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PADDING_STANDARD),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(uiState.logs) { log ->
                LogEntryItem(log = log)
            }
        }
    }
}

@Composable
private fun LogEntryItem(log: LogEntryUiModel) {
    val (icon, color) = when (log.level.uppercase()) {
        "INFO" -> Icons.Default.Info to MaterialTheme.colorScheme.primary
        "SUCCESS" -> Icons.Default.CheckCircle to Success
        "WARNING" -> Icons.Default.Warning to Warning
        "ERROR" -> Icons.Default.Error to Error
        else -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiveLogsScreenPreview() {
    OrbTradingTheme {
        LiveLogsScreenContent(uiState = LiveLogsPreviewProvider.sampleLiveLogsUiState())
    }
}

@Preview(showBackground = true, name = "Live Mode")
@Composable
fun LiveLogsScreenLivePreview() {
    OrbTradingTheme {
        LiveLogsScreenContent(uiState = LiveLogsPreviewProvider.sampleLiveLogsUiState())
    }
}
