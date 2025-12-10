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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.LogEntry
import com.trading.orb.data.model.LogLevel
import com.trading.orb.data.model.TradingMode
import com.trading.orb.ui.components.TimeFormatter
import com.trading.orb.ui.theme.*
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLogsScreen(
    modifier: Modifier = Modifier,
    tradingMode: TradingMode = TradingMode.PAPER // Add this line
) {
    // Sample logs - replace with actual log stream from ViewModel
    val sampleLogs = remember {
        listOf(
            LogEntry(
                timestamp = LocalDateTime.now(),
                level = LogLevel.INFO,
                message = "Strategy started in PAPER mode"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(1),
                level = LogLevel.SUCCESS,
                message = "First 15-min candle captured: H0=188.0, L0=183.0"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(2),
                level = LogLevel.WARNING,
                message = "Breakout detected at ₹188.2"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(3),
                level = LogLevel.SUCCESS,
                message = "Order placed: BUY NIFTY 22000 CE @ ₹183.50"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(4),
                level = LogLevel.SUCCESS,
                message = "Order filled: 50 qty @ ₹183.50"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(5),
                level = LogLevel.INFO,
                message = "Stop loss set at ₹175.50"
            ),
            LogEntry(
                timestamp = LocalDateTime.now().minusMinutes(6),
                level = LogLevel.INFO,
                message = "Target set at ₹198.50"
            )
        )
    }

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

        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Logs list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sampleLogs) { log ->
                LogEntryItem(log = log)
            }
        }
    }
}

@Composable
private fun LogEntryItem(log: LogEntry) {
    val (icon, color) = when (log.level) {
        LogLevel.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
        LogLevel.SUCCESS -> Icons.Default.CheckCircle to Success
        LogLevel.WARNING -> Icons.Default.Warning to Warning
        LogLevel.ERROR -> Icons.Default.Error to Error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
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
                text = TimeFormatter.formatTime(log.timestamp),
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
        LiveLogsScreen()
    }
}

@Preview(showBackground = true, name = "Live Mode")
@Composable
fun LiveLogsScreenLivePreview() {
    OrbTradingTheme {
        LiveLogsScreen(tradingMode = TradingMode.LIVE) // Add this line
    }
}
