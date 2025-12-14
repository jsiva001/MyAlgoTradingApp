# 🔄 Mock Data Runtime Flow - Complete Explanation

## Overview

When the app **runs on a device/emulator**, the mock data flows through the system in a specific way:

**Mock Data ➜ Strategy Engine ➜ Repository ➜ ViewModel ➜ UI**

This is **completely different** from Android Studio Preview which just displays static mock data.

---

## 🎯 The Big Picture

```
┌──────────────────────────────────────────────────────────────────┐
│                     RUNTIME MOCK DATA FLOW                        │
│                      (App Running on Device)                      │
└──────────────────────────────────────────────────────────────────┘

Step 1: User clicks START button
   ↓
Step 2: Initialize Mock Components
   ↓
Step 3: Mock Engine Generates Data
   ↓
Step 4: Strategy Engine Processes Data
   ↓
Step 5: Repository Updates State
   ↓
Step 6: ViewModel Collects State
   ↓
Step 7: UI Recomposes with Data
```

---

## Step-by-Step Detailed Flow

### **Step 1️⃣: Dashboard Screen START Button Clicked**

When user clicks START button on dashboard:

```kotlin
// DashboardScreen.kt
onToggleStrategy = { tradingViewModel.toggleStrategy() }
                             ↓
// TradingViewModel.kt
fun toggleStrategy() {
    viewModelScope.launch {
        if (appState.value.strategyStatus == StrategyStatus.ACTIVE) {
            // Stop
            strategyEngine?.stop()
        } else {
            // Start Mock Strategy
            initializeAndStartMockStrategy("normal")  ← USER INITIATED
        }
    }
}
```

---

### **Step 2️⃣: Initialize Mock Strategy Components**

```kotlin
// TradingViewModel.kt
fun initializeAndStartMockStrategy(scenario: String = "normal") {
    Timber.i("🧪 Initializing MOCK ORB Strategy Engine")
    
    viewModelScope.launch {
        // 2A: Create Mock Market Data Source
        val mockDataSource = when (scenario) {
            "high_breakout" -> MockScenarios.successfulHighBreakout().first
            "stop_loss" -> MockScenarios.stopLossScenario().first
            else -> MockScenarios.successfulHighBreakout().first
        }
        
        // 2B: Create Mock Order Executor
        val mockExecutor = MockOrderExecutor(
            executionDelayMs = 500,  // Simulate network delay
            failureRate = 0           // No failures in mock
        )
        
        // 2C: Get Strategy Config
        val config = strategyConfig.value  // ← From Repository
        
        // 2D: Initialize ORB Strategy Engine with Mock Components
        strategyEngine = OrbStrategyEngine(
            marketDataSource = mockDataSource,    ← MOCK
            orderExecutor = mockExecutor,         ← MOCK
            config = config,
            riskSettings = riskSettings.value
        )
        
        // 2E: Start Observing Strategy Events
        observeStrategyEvents()  ← Listen for ALL trade events
        
        // 2F: Start the Engine
        strategyEngine?.start()
        
        // 2G: Update Repository State
        repository.startStrategy()  ← Sets strategyStatus = ACTIVE
    }
}
```

---

### **Step 3️⃣: Mock Market Data Generation**

MockMarketDataSource generates fake LTP (Last Traded Price) data:

```kotlin
// MockMarketDataSource.kt
class MockMarketDataSource(
    private val basePrice: Double = 185.0,      // Starting price
    private val volatility: Double = 0.5,       // Price movement range
    private val updateIntervalMs: Long = 1000   // Update every 1 second
)

override fun subscribeLTP(symbol: String): Flow<Double> = flow {
    while (true) {
        // Generate random price change
        val priceChange = random.nextDouble(-volatility, volatility)
        currentPrice += priceChange
        
        // Emit price to subscribers
        emit(currentPrice)  ← UPDATES EVERY 1 SECOND
        
        delay(updateIntervalMs)
    }
}
```

**Example Output:**
```
Time 0s:   LTP = 185.00
Time 1s:   LTP = 185.23
Time 2s:   LTP = 185.45
Time 3s:   LTP = 185.12
Time 4s:   LTP = 185.89
Time 5s:   LTP = 186.50  ← BREAKOUT! (exceeds HIGH)
...continues flowing
```

---

### **Step 4️⃣: Strategy Engine Processes Mock Data**

The OrbStrategyEngine receives the flow of mock prices:

```kotlin
// OrbStrategyEngine.kt
private suspend fun waitAndCaptureOrb() {
    // 4A: Wait until ORB window starts (9:15 AM)
    waitUntilTime(config.orbStartTime)
    
    // 4B: Subscribe to LTP flow from MockMarketDataSource
    marketDataSource.subscribeLTP(config.instrument.symbol)
        .takeWhile { isInOrbWindow() && isRunning }
        .collect { ltp ->
            // 4C: Build candles from LTP data
            val candle = buildCandle(ltp, startTimestamp)
            
            // 4D: Store candles
            candles.add(candle)
            
            // 4E: Emit event for UI to show live price
            _events.emit(StrategyEvent.PriceUpdate(ltp))  ← UI SEES THIS
        }
    
    // 4F: After ORB window, capture high/low
    val orbLevels = OrbLevels(
        high = candles.maxOf { it.high },      // e.g., 188.0
        low = candles.minOf { it.low },        // e.g., 183.0
        ltp = candles.last().close             // e.g., 185.50
    )
}

private suspend fun monitorForBreakout() {
    // 4G: Now monitor for breakout
    marketDataSource.subscribeLTP(config.instrument.symbol)
        .collect { ltp ->
            // 4H: Check if breakout occurred
            if (ltp > orbLevels.high + breakoutBuffer) {  // CE BUY
                Timber.i("✅ HIGH BREAKOUT! LTP: $ltp > High: ${orbLevels.high}")
                
                // Execute BUY order
                val result = orderExecutor.placeMarketOrder(
                    symbol = config.instrument.symbol,
                    side = OrderSide.BUY,  ← CE OPTION
                    quantity = config.instrument.lotSize,
                    tag = "ORB_HIGH_BREAKOUT"
                )
                
                if (result.isSuccess) {
                    activePosition = Position(...)  ← POSITION OPENED
                    _events.emit(StrategyEvent.TradeEntered(...))  ← UI SEES THIS
                }
            } else if (ltp < orbLevels.low - breakoutBuffer) {  // PE BUY
                Timber.i("✅ LOW BREAKOUT! LTP: $ltp < Low: ${orbLevels.low}")
                // Execute BUY PE order...
            }
        }
}
```

---

### **Step 5️⃣: Mock Order Executor Handles Orders**

When the engine signals a trade:

```kotlin
// MockOrderExecutor.kt
override suspend fun placeMarketOrder(
    symbol: String,
    side: OrderSide,
    quantity: Int,
    tag: String?
): Result<OrderResponse> {
    // 5A: Simulate network delay
    delay(executionDelayMs)  // 500ms
    
    // 5B: Generate mock order response
    val orderId = UUID.randomUUID().toString()
    val mockPrice = 185.0 + Random.nextDouble(-1.0, 1.0)
    
    // 5C: Return successful order
    return Result.success(
        OrderResponse(
            orderId = orderId,           // "123e4567-e89b-..."
            status = "COMPLETE",         // Instantly filled in mock
            message = "Mock order executed",
            price = mockPrice
        )
    )
}
```

**Log Output:**
```
[Strategy Engine] ✅ HIGH BREAKOUT! LTP: 188.5 > High: 188.0
[Order Executor] Placing MARKET order - BUY 50 x NIFTY24DEC22000CE
[Order Executor] Mock order executed - Price: 185.23
[Strategy Engine] Position opened: POS_001
```

---

### **Step 6️⃣: ViewModel Observes Strategy Events**

The ViewModel listens to ALL events from the strategy engine:

```kotlin
// TradingViewModel.kt
private fun observeStrategyEvents() {
    viewModelScope.launch {
        strategyEngine?.events?.collect { event ->
            when (event) {
                // 6A: ORB Captured
                is StrategyEvent.OrbCaptured -> {
                    Timber.i("ORB Levels: High=${event.orbLevels.high}, Low=${event.orbLevels.low}")
                    
                    _appState.value = _appState.value.copy(
                        orbLevels = event.orbLevels  ← UPDATE STATE
                    )
                }
                
                // 6B: Price Update
                is StrategyEvent.PriceUpdate -> {
                    Timber.d("Price: ${event.ltp}")
                    // Update for UI to show live price
                    _appState.value = _appState.value.copy(
                        orbLevels = _appState.value.orbLevels?.copy(
                            ltp = event.ltp  ← UPDATE LIVE PRICE
                        )
                    )
                }
                
                // 6C: Trade Entered
                is StrategyEvent.TradeEntered -> {
                    Timber.i("🟢 Trade Entered: ${event.position}")
                    
                    // Create Trade object
                    val trade = Trade(
                        tradeId = event.position.id,
                        symbol = event.position.symbol,
                        entryPrice = event.position.entryPrice,
                        entryTime = LocalDateTime.now(),
                        side = event.position.side,
                        quantity = event.position.quantity,
                        status = "OPEN"
                    )
                    
                    // Update repository with new trade
                    repository.addTrade(trade)
                    
                    // Update UI state
                    _dashboardUiState.update {
                        it.copy(
                            dailyStats = it.dailyStats.copy(
                                activePositions = it.dailyStats.activePositions + 1
                            )
                        )
                    }
                    
                    _uiEvent.emit(UiEvent.ShowSuccess("Trade entered at ₹${event.position.entryPrice}"))
                }
                
                // 6D: Target Hit
                is StrategyEvent.TargetHit -> {
                    Timber.i("🎯 Target Hit: ${event.profitLoss}")
                    
                    // Update trade status to CLOSED
                    repository.updateTradeStatus(event.tradeId, "CLOSED")
                    
                    // Update P&L
                    _appState.value = _appState.value.copy(
                        dailyStats = _appState.value.dailyStats.copy(
                            totalPnl = _appState.value.dailyStats.totalPnl + event.profitLoss
                        )
                    )
                    
                    _uiEvent.emit(UiEvent.ShowSuccess("Target Hit! Profit: ₹${event.profitLoss}"))
                }
                
                // 6E: Stop Loss Hit
                is StrategyEvent.StopLossHit -> {
                    Timber.i("🛑 Stop Loss Hit: ${event.loss}")
                    
                    // Close position
                    repository.updateTradeStatus(event.tradeId, "CLOSED")
                    
                    // Update P&L (negative)
                    _appState.value = _appState.value.copy(
                        dailyStats = _appState.value.dailyStats.copy(
                            totalPnl = _appState.value.dailyStats.totalPnl + event.loss,
                            winRate = calculateWinRate()
                        )
                    )
                    
                    _uiEvent.emit(UiEvent.ShowError("Stop Loss Hit! Loss: ₹${event.loss}"))
                }
                
                else -> {}
            }
        }
    }
}
```

---

### **Step 7️⃣: Repository Updates & Persists State**

The repository maintains the single source of truth:

```kotlin
// TradingRepositoryImpl.kt
private val _appState = MutableStateFlow(AppState())
val appState: StateFlow<AppState> = _appState.asStateFlow()

// When events occur, repository updates
override suspend fun startStrategy(): Result<Unit> {
    _appState.value = _appState.value.copy(
        strategyStatus = StrategyStatus.ACTIVE,  ← KEY UPDATE
        connectionStatus = ConnectionStatus.CONNECTED
    )
    return Result.success(Unit)
}

override suspend fun addTrade(trade: Trade): Result<Unit> {
    // 7A: Add to database
    tradeDao.insertTrade(trade)
    
    // 7B: Update in-memory state
    val updatedTrades = (_trades.value ?: emptyList()).toMutableList()
    updatedTrades.add(trade)
    _trades.value = updatedTrades
    
    return Result.success(Unit)
}
```

---

### **Step 8️⃣: ViewModel Exposes State to UI**

```kotlin
// TradingViewModel.kt
val appState: StateFlow<AppState> = repository.appState
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppState()
    )

val dashboardUiState: StateFlow<DashboardUiState> = 
    repository.appState
        .map { appState ->
            DashboardUiState(
                dailyStats = appState.dailyStats,
                orbLevels = appState.orbLevels,
                isRefreshing = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
```

---

### **Step 9️⃣: UI Collects & Displays Data**

```kotlin
// DashboardScreen.kt
@Composable
fun DashboardScreen(
    tradingViewModel: TradingViewModel = hiltViewModel()
) {
    // 9A: Collect real state from ViewModel
    val appState by tradingViewModel.appState.collectAsStateWithLifecycle()
    val uiState by tradingViewModel.dashboardUiState.collectAsStateWithLifecycle()
    
    // 9B: Recompose when state changes
    DashboardScreenContent(
        appState = appState,  ← REAL DATA FROM MOCK ENGINE
        uiState = uiState,    ← REAL DATA FROM REPOSITORY
        onToggleStrategy = { tradingViewModel.toggleStrategy() },
        ...
    )
}

@Composable
private fun DashboardScreenContent(
    appState: AppState
) {
    // 9C: Display data
    Column {
        QuickStatsSection(appState.dailyStats)
        
        StrategyStatusCard(
            status = appState.strategyStatus,  ← Shows ACTIVE/INACTIVE
            onToggleStrategy = onToggleStrategy
        )
        
        OrbLevelsCard(
            orbLevels = appState.orbLevels
        )
    }
}

@Composable
private fun OrbLevelsCard(orbLevels: OrbLevels?) {
    if (orbLevels != null) {
        Column {
            Text("ORB High: ${orbLevels.high}")
            Text("ORB Low: ${orbLevels.low}")
            Text("Current LTP: ${orbLevels.ltp}")  ← UPDATES EVERY SECOND
        }
    }
}
```

---

## 🔀 Complete Data Flow Diagram

```
USER CLICKS START
        ↓
toggleStrategy()
        ↓
initializeAndStartMockStrategy()
        ↓
┌─────────────────────────────────────────────────┐
│  OrbStrategyEngine Created                      │
│  ├─ marketDataSource = MockMarketDataSource     │
│  ├─ orderExecutor = MockOrderExecutor           │
│  └─ config = StrategyConfig                     │
└─────────────────────────────────────────────────┘
        ↓
strategyEngine.start()
        ↓
┌─────────────────────────────────────────────────┐
│  Mock Data Generation                           │
│  MockMarketDataSource.subscribeLTP()            │
│  ├─ Emits LTP every 1 second                    │
│  ├─ 185.0 → 185.23 → 185.45 → ... → 188.5      │
│  └─ Flow<Double>                                │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  Strategy Processing                            │
│  waitAndCaptureOrb()                            │
│  ├─ Collects LTP for 15 minutes                 │
│  ├─ Builds candles                              │
│  ├─ Calculates High/Low/Current                 │
│  └─ Emits: OrbCaptured event                    │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  Breakout Detection                             │
│  monitorForBreakout()                           │
│  ├─ LTP > High → HIGH BREAKOUT                  │
│  ├─ LTP < Low → LOW BREAKOUT                    │
│  └─ Emits: TradeEntered event                   │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  Order Execution                                │
│  orderExecutor.placeMarketOrder()               │
│  ├─ Delay 500ms (simulate network)              │
│  ├─ Generate orderId & price                    │
│  ├─ Return OrderResponse                        │
│  └─ Store Position                              │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  ViewModel Observes Events                      │
│  observeStrategyEvents()                        │
│  ├─ TradeEntered → Update activePositions       │
│  ├─ PriceUpdate → Update LTP                    │
│  ├─ TargetHit → Update totalPnl +               │
│  └─ StopLossHit → Update totalPnl -             │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  Repository Updates State                       │
│  appState.value = appState.copy(...)            │
│  ├─ strategyStatus = ACTIVE/INACTIVE            │
│  ├─ dailyStats.totalPnl = +/-                   │
│  ├─ orbLevels = OrbLevels(...)                  │
│  └─ Emits to StateFlow                          │
└─────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────┐
│  UI Recomposes                                  │
│  collectAsStateWithLifecycle()                  │
│  ├─ Observes appState changes                   │
│  ├─ DashboardScreen recomposes                  │
│  ├─ Shows updated values                        │
│  └─ User sees live data!                        │
└─────────────────────────────────────────────────┘
        ↓
USER SEES:
✅ Strategy Status: ACTIVE (button changes to STOP)
✅ ORB High: 188.0, Low: 183.0, LTP: 188.5 (updates every second)
✅ Open Positions: 1
✅ Total P&L: ₹150.0 (when target hit)
✅ Trade entry time, price, etc.
```

---

## 📊 Data Transformation Summary

| Layer | Data Type | Source |
|-------|-----------|--------|
| **1. Market Data** | `Flow<Double>` | `MockMarketDataSource.subscribeLTP()` |
| **2. Candles** | `List<Candle>` | Built from LTP flow in strategy engine |
| **3. ORB Levels** | `OrbLevels` | Calculated from candles (High/Low) |
| **4. Events** | `StrategyEvent` | Emitted by OrbStrategyEngine |
| **5. Trades** | `Trade` | Created from events by ViewModel |
| **6. App State** | `AppState` | Repository updates from events |
| **7. UI State** | `DashboardUiState` | Mapped from AppState |
| **8. Composables** | UI Display | Receives DashboardUiState |

---

## 🎯 Key Differences: Preview vs Runtime

| Aspect | Preview (Android Studio) | Runtime (Device/Emulator) |
|--------|--------------------------|---------------------------|
| **Data Source** | `PreviewProvider` static methods | `MockMarketDataSource` Flow |
| **Updates** | None - static data | Continuous - every 1 second |
| **Execution** | Instant display | Full strategy engine runs |
| **Events** | None | Real events from engine |
| **State Changes** | Manual in Preview Provider | Automatic from strategy |
| **User Interaction** | No-op callbacks | Real callbacks executed |
| **Duration** | Single frame | Long-running process |
| **Network Simulation** | None | 500ms delay for orders |

---

## Example Scenario: High Breakout Trade

```
Timeline:
─────────────────────────────────────────────

09:15:00 - Strategy starts (ORB window begins)
          LTP: 185.00
          
09:16:00 - Collecting ORB data
          LTP: 185.23 → 185.45 → 184.89 → 185.12
          
09:20:00 - Collecting ORB data
          LTP: 185.67 → 185.34 → 186.00 → 186.45
          
09:30:00 - ORB window closes
          HIGH: 186.45
          LOW: 184.89
          CURRENT: 185.50
          OrbCaptured event emitted → UI updates to show levels
          
09:30:05 - Monitoring for breakout
          LTP: 185.60 (no breakout yet)
          
09:30:10 - BREAKOUT!
          LTP: 188.50 > 186.45 + 2 = HIGH BREAKOUT ✅
          
          → OrderExecutor.placeMarketOrder() called
          → 500ms delay (simulating network)
          → Order filled at 185.23
          → Position opened
          
          → ViewModel observes TradeEntered event
          → Updates: activePositions = 1
          → UI shows: "Open Position: NIFTY 22000 CE @ ₹185.23"
          
09:30:20 - Price movement
          LTP: 186.00 → 189.50 → 190.00 → 190.50
          
          → ViewModel observes PriceUpdate event
          → Updates: ltp = 190.50
          → UI shows live: "Current LTP: ₹190.50, P&L: +₹262.50"
          
09:30:25 - Target hit!
          LTP: 200.23 (185.23 + 15 point target = 200.23)
          
          → TargetHit event emitted with profitLoss = 262.50
          → Repository updates: totalPnl += 262.50
          → ViewModel updates: dailyStats.totalPnl = 262.50
          → UI shows: "✅ Target Hit! Profit: ₹262.50"
          → Button state changes back to "START"
```

---

## 🔑 Key Components Explained

### **MockMarketDataSource**
- Generates continuous LTP flow (one price per second)
- Simulates realistic price movements with volatility
- Can be swapped with real AngelMarketDataSource

### **MockOrderExecutor**
- Simulates order execution with 500ms delay
- Returns success by default (can set failure rate)
- Tracks positions in memory

### **OrbStrategyEngine**
- Core logic that processes market data
- Detects ORB breakouts (High/Low)
- Places orders when signals are detected
- Emits events for every significant action

### **ViewModel**
- Listens to all strategy events
- Updates repository state
- Exposes StateFlows for UI consumption

### **Repository**
- Maintains single source of truth
- Persists data to database
- Emits StateFlows that UI observes

### **UI Layer**
- Observes repository StateFlows
- Recomposes automatically on data changes
- Shows live data to user

---

## 🚀 Switching to Real Data

To switch from mock to real Angel One API:

```kotlin
// Just change this line:
// FROM:
val mockDataSource = MockMarketDataSource()

// TO:
val realDataSource = AngelMarketDataSource(apiKey, token)

// Then pass to strategy engine:
strategyEngine = OrbStrategyEngine(
    marketDataSource = realDataSource,  ← Now real!
    orderExecutor = mockExecutor,       // or AngelOrderExecutor
    config = config,
    riskSettings = riskSettings.value
)
```

**Everything else stays the same!** The strategy logic, UI, and state management work identically with real or mock data.

---

## Summary

**Runtime mock data flow is a complete simulation of live trading:**

1. **MockMarketDataSource** generates realistic price movements continuously
2. **OrbStrategyEngine** processes prices in real-time (every second)
3. **Strategy logic** detects ORB breakouts and executes trades
4. **MockOrderExecutor** simulates order execution with network delays
5. **ViewModel** collects events and updates state automatically
6. **Repository** maintains single source of truth
7. **UI** observes changes via StateFlow and recomposes instantly

This enables **complete end-to-end testing** of the ORB strategy without needing:
- Real broker API
- Real market data
- Real money
- Network connectivity

The same code that runs mock trades will run **real trades** when you swap the data sources! 🚀
