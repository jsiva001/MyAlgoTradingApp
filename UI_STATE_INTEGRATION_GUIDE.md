# 📱 UI State Management Integration Guide

## Overview

This guide shows how to integrate UI State management into your Compose screens. Each screen will have:

1. **UI State** - Represents screen state (loading, success, error)
2. **UI Models** - Data models for displaying on UI
3. **ViewModel** - Manages state and business logic
4. **Screen Composable** - Displays the state

---

## Architecture Diagram

```
Data/Domain Layer
       ↓
Repository/UseCase
       ↓
ViewModel (collects state)
       ↓
UI State + UI Models
       ↓
Composable Screen (displays state)
```

---

## Integration Pattern for Each Screen

### Step 1: Update ViewModel

```kotlin
// DashboardViewModel.kt
class DashboardViewModel(
    private val repository: DashboardRepository,
    private val logger: Logger
) : ViewModel() {
    
    // State flow for UI state
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = LoadingState(isLoading = true)) }
            try {
                val stats = repository.getQuickStats()
                val strategy = repository.getStrategyStatus()
                val orbLevels = repository.getOrbLevels()
                val trades = repository.getRecentTrades()
                val metrics = repository.getPerformanceMetrics()

                _uiState.update {
                    it.copy(
                        quickStats = mapToQuickStatsUiModel(
                            totalProfit = stats.profit,
                            totalReturn = stats.return,
                            winRate = stats.winRate,
                            totalTrades = stats.totalTrades
                        ),
                        strategyStatus = mapToStrategyStatusUiModel(
                            isActive = strategy.isActive,
                            strategyName = strategy.name,
                            uptime = strategy.uptime,
                            lastUpdated = strategy.lastUpdated
                        ),
                        orbLevels = mapToOrbLevelsUiModel(
                            symbol = orbLevels.symbol,
                            openPrice = orbLevels.open,
                            orbHigh = orbLevels.high,
                            orbLow = orbLevels.low,
                            lastPrice = orbLevels.lastPrice
                        ),
                        recentTrades = trades.map {
                            mapToRecentTradeUiModel(
                                tradeId = it.id,
                                symbol = it.symbol,
                                type = it.type,
                                quantity = it.quantity,
                                entryPrice = it.entryPrice,
                                exitPrice = it.exitPrice,
                                profitLoss = it.profitLoss,
                                status = it.status,
                                timestamp = it.timestamp
                            )
                        },
                        performanceMetrics = mapToPerformanceMetricsUiModel(
                            dailyProfit = metrics.dailyProfit,
                            monthlyProfit = metrics.monthlyProfit,
                            bestTrade = metrics.bestTrade,
                            worstTrade = metrics.worstTrade,
                            averageTrade = metrics.averageTrade,
                            profitFactor = metrics.profitFactor
                        ),
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                logger.error("Failed to load dashboard", e)
                _uiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Unknown error",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    fun retry() {
        loadDashboard()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadDashboard()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
```

### Step 2: Update Screen Composable

```kotlin
// DashboardScreen.kt
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Collect state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handle loading state
        if (uiState.loading.isLoading) {
            LoadingScreen(message = uiState.loading.loadingMessage)
            return@Column
        }

        // Handle error state
        if (uiState.error.hasError) {
            ErrorScreen(
                message = uiState.error.errorMessage,
                isRetryable = uiState.error.isRetryable,
                onRetry = { viewModel.retry() }
            )
            return@Column
        }

        // Handle success state - Display UI models
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stats Section
            item {
                QuickStatsSection(uiState.quickStats)
            }

            // Strategy Status Section
            item {
                StrategyStatusCard(
                    status = uiState.strategyStatus
                )
            }

            // ORB Levels Section
            if (uiState.orbLevels != null) {
                item {
                    OrbLevelsCard(
                        orbLevels = uiState.orbLevels!!
                    )
                }
            }

            // Recent Trades Section
            if (uiState.recentTrades.isNotEmpty()) {
                item {
                    RecentTradesSection(
                        trades = uiState.recentTrades
                    )
                }
            }

            // Performance Metrics Section
            item {
                PerformanceMetricsSection(
                    metrics = uiState.performanceMetrics
                )
            }
        }
    }
}

// Composables to display UI models
@Composable
private fun QuickStatsSection(stats: QuickStatsUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Profit: $${String.format("%.2f", stats.totalProfit)}")
            Text("Win Rate: ${String.format("%.2f", stats.winRate)}%")
            Text("Total Trades: ${stats.totalTrades}")
        }
    }
}

@Composable
private fun StrategyStatusCard(status: StrategyStatusUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(status.strategyName, style = MaterialTheme.typography.titleLarge)
            Text("Status: ${if (status.isActive) "Active" else "Inactive"}")
            Text("Uptime: ${status.uptime}")
        }
    }
}

@Composable
private fun OrbLevelsCard(orbLevels: OrbLevelsUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${orbLevels.symbol} ORB Levels")
            Text("Open: $${String.format("%.2f", orbLevels.openPrice)}")
            Text("High: $${String.format("%.2f", orbLevels.orbHigh)}")
            Text("Low: $${String.format("%.2f", orbLevels.orbLow)}")
            Text("Last: $${String.format("%.2f", orbLevels.lastPrice)}")
            Text("Deviation: ${String.format("%.2f", orbLevels.deviation)}%")
        }
    }
}

@Composable
private fun RecentTradesSection(trades: List<RecentTradeUiModel>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recent Trades", style = MaterialTheme.typography.titleLarge)
            trades.forEach { trade ->
                TradeRow(trade)
            }
        }
    }
}

@Composable
private fun TradeRow(trade: RecentTradeUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("${trade.symbol} ${trade.type}")
            Text("Qty: ${trade.quantity} @ $${String.format("%.2f", trade.entryPrice)}")
        }
        Text(
            "$${String.format("%.2f", trade.profitLoss)}",
            color = if (trade.profitLoss >= 0) Color.Green else Color.Red
        )
    }
}

@Composable
private fun PerformanceMetricsSection(metrics: PerformanceMetricsUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Performance Metrics", style = MaterialTheme.typography.titleLarge)
            Text("Daily Profit: $${String.format("%.2f", metrics.dailyProfit)}")
            Text("Monthly Profit: $${String.format("%.2f", metrics.monthlyProfit)}")
            Text("Best Trade: $${String.format("%.2f", metrics.bestTrade)}")
            Text("Worst Trade: $${String.format("%.2f", metrics.worstTrade)}")
            Text("Profit Factor: ${String.format("%.2f", metrics.profitFactor)}")
        }
    }
}

// Loading screen
@Composable
fun LoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

// Error screen
@Composable
fun ErrorScreen(
    message: String,
    isRetryable: Boolean = true,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error", style = MaterialTheme.typography.titleLarge)
            Text(message, textAlign = TextAlign.Center)
            if (isRetryable) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
```

---

## Apply This Pattern to All Screens

Apply the same pattern to:
- StrategyConfigScreen + StrategyConfigViewModel
- PositionsScreen + PositionsViewModel
- RiskScreen + RiskViewModel
- TradeHistoryScreen + TradeHistoryViewModel
- LiveLogsScreen + LiveLogsViewModel
- MoreScreen + MoreViewModel

---

## Benefits

✅ **Type-safe UI state**  
✅ **Easy to test** - State is predictable  
✅ **Easier to debug** - See exactly what's displayed  
✅ **Reusable UI models** - Share across screens  
✅ **Loading/Error handling** - Built-in  
✅ **Performance** - StateFlow is efficient  
✅ **Lifecycle-aware** - collectAsStateWithLifecycle()

---

## Testing Examples

```kotlin
@Test
fun testDashboardLoading() {
    val viewModel = DashboardViewModel(repository, logger)
    // State should be Loading initially
    assert(viewModel.uiState.value.loading.isLoading)
}

@Test
fun testDashboardSuccess() {
    val viewModel = DashboardViewModel(repository, logger)
    // After loading, should have data
    assert(viewModel.uiState.value.quickStats.totalTrades > 0)
}

@Test
fun testDashboardError() {
    val viewModel = DashboardViewModel(failingRepository, logger)
    // On error, should show error message
    assert(viewModel.uiState.value.error.hasError)
}
```

---

## Next Steps

1. ✅ Create UI State classes (Done - UiState.kt)
2. ✅ Create UI Data Models (Done - UiModels.kt)
3. ✅ Create Mappers (Done - UiMappers.kt)
4. ⏳ Update ViewModels to emit UiState
5. ⏳ Update Screen Composables to use UiState
6. ⏳ Add Loading/Error screens
7. ⏳ Test all screens

---

**Status:** Ready to integrate into screens! 🚀
