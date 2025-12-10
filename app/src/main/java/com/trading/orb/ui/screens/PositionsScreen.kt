package com.trading.orb.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.theme.*
import java.time.LocalDateTime

@Composable
fun PositionsScreen(
    positions: List<Position>,
    onClosePosition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Summary Header
        PositionsSummaryHeader(positions = positions)

        // Positions List
        if (positions.isEmpty()) {
            EmptyPositionsView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(positions) { position ->
                    PositionCard(
                        position = position,
                        onClose = { onClosePosition(position.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionsSummaryHeader(positions: List<Position>) {
    val totalPnL = positions.sumOf { it.pnl }

    OrbCard(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Total Open P&L",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            PnLDisplay(
                pnl = totalPnL,
                fontSize = 32
            )
        }
    }
}

@Composable
private fun PositionCard(
    position: Position,
    onClose: () -> Unit
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    val borderColor = if (position.isProfit) Success else Error

    OrbCard(
        modifier = Modifier.border(
            width = 2.dp,
            color = borderColor,
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        // Header: Instrument name and side badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = position.instrument.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${position.instrument.exchange} • Qty: ${position.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            OrderSideBadge(side = position.side)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price info grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceInfoColumn(
                label = "Entry",
                value = "₹${String.format("%.2f", position.entryPrice)}",
                modifier = Modifier.weight(1f)
            )
            PriceInfoColumn(
                label = "Current",
                value = "₹${String.format("%.2f", position.currentPrice)}",
                valueColor = if (position.isProfit) Success else Error,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "P&L",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PnLDisplay(
                    pnl = position.pnl,
                    percentage = position.pnlPercentage,
                    fontSize = 16
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SL and Target
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelBox(
                label = "SL",
                value = "₹${String.format("%.2f", position.stopLoss)}",
                color = Error,
                modifier = Modifier.weight(1f)
            )
            LevelBox(
                label = "Target",
                value = "₹${String.format("%.2f", position.target)}",
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entry time
        Text(
            text = "Entry: ${TimeFormatter.formatTime(position.entryTime)}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Close button
        Button(
            onClick = { showCloseDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error
            )
        ) {
            Text(
                text = "Close Position",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Confirmation dialog
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Close Position?") },
            text = {
                Text("Are you sure you want to close this position for ${position.instrument.displayName}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClose()
                        showCloseDialog = false
                    }
                ) {
                    Text("Close", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PriceInfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
private fun LevelBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyPositionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Text(
                text = "No Active Positions",
                style = MaterialTheme.typography.headlineLarge,
                color = TextSecondary
            )
            Text(
                text = "Start the strategy to open positions",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }
    }
}

// Preview
@Preview
@Composable
fun PositionsScreenPreview() {
    val samplePositions = listOf(
        Position(
            id = "1",
            instrument = Instrument(
                symbol = "NIFTY24DEC22000CE",
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            side = OrderSide.BUY,
            quantity = 50,
            entryPrice = 183.50,
            currentPrice = 185.50,
            stopLoss = 175.50,
            target = 198.50,
            entryTime = LocalDateTime.now().minusMinutes(30)
        ),
        Position(
            id = "2",
            instrument = Instrument(
                symbol = "BANKNIFTY27DEC48000PE",
                exchange = "NSE",
                lotSize = 25,
                tickSize = 0.05,
                displayName = "BANKNIFTY 48000 PE"
            ),
            side = OrderSide.BUY,
            quantity = 25,
            entryPrice = 295.00,
            currentPrice = 314.00,
            stopLoss = 287.00,
            target = 310.00,
            entryTime = LocalDateTime.now().minusMinutes(45)
        )
    )

    OrbTradingTheme {
        PositionsScreen(
            positions = samplePositions,
            onClosePosition = {}
        )
    }
}


