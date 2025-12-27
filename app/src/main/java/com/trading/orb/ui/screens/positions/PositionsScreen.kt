package com.trading.orb.ui.screens.positions

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.state.PositionUiModel
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.LaunchEventCollector
import com.trading.orb.ui.utils.ProfitCalculationUtils
import com.trading.orb.ui.viewmodel.TradingViewModel
import com.trading.orb.ui.viewmodel.UiEvent
import timber.log.Timber
import java.time.format.DateTimeFormatter

@Composable
fun PositionsScreen(
    tradingViewModel: TradingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val appState by tradingViewModel.appState.collectAsStateWithLifecycle()
    
    LaunchEventCollector(eventFlow = tradingViewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowError -> {
                Timber.e("❌ Error: ${event.message}")
            }
            is UiEvent.ShowSuccess -> {
                Timber.d("✅ Success: ${event.message}")
            }
            else -> {}
        }
    }
    
    // Transform positions to UI models
    val positionUiModels = appState.activePositions.map { position ->
        val pnlAmount = ProfitCalculationUtils.calculatePositionPnL(position)
        val pnlPercent = ProfitCalculationUtils.calculatePnLPercentage(
            pnlAmount,
            position.entryPrice,
            position.quantity
        )
        
        PositionUiModel(
            positionId = position.id,
            symbol = position.instrument.symbol,
            type = position.side.name,
            quantity = position.quantity,
            entryPrice = position.entryPrice,
            currentPrice = position.currentPrice,
            profitLoss = pnlAmount,
            profitLossPercent = pnlPercent,
            stopLoss = position.stopLoss,
            takeProfit = position.target,
            openTime = position.entryTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            riskLevel = if (pnlAmount >= 0) "LOW" else "MEDIUM"
        )
    }
    
    // Calculate summary stats
    val totalProfit = positionUiModels.filter { it.profitLoss > 0 }.sumOf { it.profitLoss }
    val totalLoss = positionUiModels.filter { it.profitLoss < 0 }.sumOf { it.profitLoss }
    
    PositionsScreenContent(
        positions = positionUiModels,
        totalProfit = totalProfit,
        totalLoss = totalLoss,
        onClosePosition = { positionId -> 
            Timber.d("📍 onClosePosition called with positionId: $positionId")
            tradingViewModel.closePosition(positionId)
        },
        modifier = modifier
    )
}

@Composable
private fun PositionsScreenContent(
    positions: List<PositionUiModel>,
    totalProfit: Double,
    totalLoss: Double,
    onClosePosition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Summary Header
        PositionsSummaryHeader(totalProfit = totalProfit, totalLoss = totalLoss)

        // Positions List
        if (positions.isEmpty()) {
            EmptyPositionsView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(PADDING_STANDARD),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(positions) { positionUiModel ->
                    PositionCard(
                        positionUiModel = positionUiModel,
                        onClose = { onClosePosition(positionUiModel.positionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionsSummaryHeader(totalProfit: Double, totalLoss: Double) {
    val totalPnL = totalProfit + totalLoss

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
    positionUiModel: PositionUiModel,
    onClose: () -> Unit
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    val borderColor = if (positionUiModel.profitLoss >= 0) Success else Error

    OrbCard(
        modifier = Modifier.border(
            width = 2.dp,
            color = borderColor,
            shape = RoundedCornerShape(CORNER_RADIUS_LARGE)
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
                    text = positionUiModel.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Qty: ${positionUiModel.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            OrderSideBadge(side = if (positionUiModel.type == "LONG") OrderSide.BUY else OrderSide.SELL)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price info grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceInfoColumn(
                label = "Entry",
                value = "₹${String.format("%.2f", positionUiModel.entryPrice)}",
                modifier = Modifier.weight(1f)
            )
            PriceInfoColumn(
                label = "Current",
                value = "₹${String.format("%.2f", positionUiModel.currentPrice)}",
                valueColor = if (positionUiModel.profitLoss >= 0) Success else Error,
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
                    pnl = positionUiModel.profitLoss,
                    percentage = positionUiModel.profitLossPercent,
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
                value = "₹${String.format("%.2f", positionUiModel.stopLoss ?: 0.0)}",
                color = Error,
                modifier = Modifier.weight(1f)
            )
            LevelBox(
                label = "Target",
                value = "₹${String.format("%.2f", positionUiModel.takeProfit ?: 0.0)}",
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entry time
        Text(
            text = "Entry: ${positionUiModel.openTime}",
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
            title = { Text("⚠️ Close Position") },
            text = {
                Text("Close ${positionUiModel.symbol} position at current market price?\n\nP&L: ${if (positionUiModel.profitLoss >= 0) "+" else ""}₹${String.format("%.0f", positionUiModel.profitLoss)}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Timber.d("🔍 Close button clicked for position: ${positionUiModel.positionId}")
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
            .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
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
    OrbTradingTheme {
        PositionsScreenContent(
            positions = listOf(),
            totalProfit = 0.0,
            totalLoss = 0.0,
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenEmptyPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            positions = listOf(),
            totalProfit = 0.0,
            totalLoss = 0.0,
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenLoadingPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            positions = listOf(),
            totalProfit = 0.0,
            totalLoss = 0.0,
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenErrorPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            positions = listOf(),
            totalProfit = 0.0,
            totalLoss = 0.0,
            onClosePosition = {}
        )
    }
}


