package com.trading.orb.ui.screens.tradehistory

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
import com.trading.orb.data.model.OrderSide
import com.trading.orb.data.model.Trade
import com.trading.orb.ui.components.*
import com.trading.orb.ui.state.TradeHistoryUiModel
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.ProfitCalculationUtils
import com.trading.orb.ui.viewmodel.TradingViewModel
import timber.log.Timber
import java.time.format.DateTimeFormatter

enum class HistoryFilter {
    ALL, TODAY, WEEK, MONTH
}

@Composable
fun TradeHistoryScreen(
    tradingViewModel: TradingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val appState by tradingViewModel.appState.collectAsStateWithLifecycle()
    
    // Transform trades to UI models
    val tradeUiModels = appState.closedTrades.map { trade ->
        val pnlAmount = ProfitCalculationUtils.calculateTradePnL(trade)
        val pnlPercent = ProfitCalculationUtils.calculatePnLPercentage(
            pnlAmount,
            trade.entryPrice,
            trade.quantity
        )
        val status = ProfitCalculationUtils.getPnLStatus(pnlAmount)
        
        TradeHistoryUiModel(
            tradeId = trade.id,
            symbol = trade.instrument.symbol,
            tradeType = trade.side.name,
            quantity = trade.quantity,
            entryPrice = trade.entryPrice,
            exitPrice = trade.exitPrice,
            profitLoss = pnlAmount,
            profitLossPercent = pnlPercent,
            duration = "${trade.duration}m",
            status = status,
            entryTime = trade.entryTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            exitTime = trade.exitTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            reason = trade.exitReason.name
        )
    }
    
    TradeHistoryScreenContent(
        trades = tradeUiModels,
        modifier = modifier
    )
}

@Composable
private fun TradeHistoryScreenContent(
    trades: List<TradeHistoryUiModel> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.TODAY) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Filter tabs
        FilterTabs(
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it }
        )

        // Statistics summary
        TradeStatistics(trades = trades)

        // Trade list
        if (trades.isEmpty()) {
            EmptyTradesView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trades) { trade ->
                    TradeCard(trade = trade)
                }
            }
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFilter.entries.forEach { filter ->
            FilterTabButton(
                selected = selectedFilter == filter,
                label = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { onFilterChange(filter) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterTabButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}

@Composable
private fun TradeStatistics(trades: List<TradeHistoryUiModel>) {
    val winningTrades = trades.filter { it.profitLoss >= 0 }
    val losingTrades = trades.filter { it.profitLoss < 0 }

    val winRate = if (trades.isNotEmpty()) {
        (winningTrades.size.toDouble() / trades.size) * 100
    } else 0.0

    val avgWin = if (winningTrades.isNotEmpty()) {
        winningTrades.map { it.profitLoss }.average()
    } else 0.0

    val avgLoss = if (losingTrades.isNotEmpty()) {
        losingTrades.map { it.profitLoss }.average()
    } else 0.0

    OrbCard(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                label = "Win Rate",
                value = "${String.format("%.0f", winRate)}%",
                color = Success
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp),
                color = SurfaceVariant
            )

            StatColumn(
                label = "Avg Win",
                value = "₹${String.format("%.0f", avgWin)}",
                color = Success
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp),
                color = SurfaceVariant
            )

            StatColumn(
                label = "Avg Loss",
                value = "₹${String.format("%.0f", avgLoss)}",
                color = Error
            )
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = color
        )
    }
}

@Composable
private fun TradeCard(trade: TradeHistoryUiModel) {
    val borderColor = if (trade.profitLoss >= 0) Success else Error

    OrbCard(
        modifier = Modifier.border(
            width = 2.dp,
            color = borderColor,
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = trade.symbol,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "${trade.entryTime} - ${trade.exitTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            OrderSideBadge(side = if (trade.tradeType == "BUY") OrderSide.BUY else OrderSide.SELL)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceColumn(
                label = "Entry",
                value = "₹${String.format("%.2f", trade.entryPrice)}",
                modifier = Modifier.weight(1f)
            )
            PriceColumn(
                label = "Exit",
                value = "₹${String.format("%.2f", trade.exitPrice)}",
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
                    pnl = trade.profitLoss,
                    percentage = trade.profitLossPercent,
                    fontSize = 16
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exit reason
        Text(
            text = "Reason: ${trade.reason}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun PriceColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
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
            color = TextPrimary
        )
    }
}

@Composable
private fun EmptyTradesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Text(
                text = "No Trade History",
                style = MaterialTheme.typography.headlineLarge,
                color = TextSecondary
            )
            Text(
                text = "Your completed trades will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }
    }
}

// Preview
@Preview
@Composable
fun TradeHistoryScreenPreview() {
    OrbTradingTheme {
        TradeHistoryScreenContent(trades = emptyList())
    }
}
