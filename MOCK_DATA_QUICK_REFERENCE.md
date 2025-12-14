# 🎮 Mock Data - Quick Reference Guide

## What is Mock Data?

Mock data is **simulated fake data** used during development and testing instead of real data from the broker API. It flows through your app exactly like real data would, allowing you to:

- ✅ Test without internet
- ✅ Test without real money
- ✅ Control exact scenarios
- ✅ Reproduce bugs reliably
- ✅ Develop faster

---

## Quick Flow: How Mock Data Flows

```
User clicks START
        ↓
Initialize MockMarketDataSource
        ↓
Generate fake LTP prices (every 1 second)
        ↓
OrbStrategyEngine processes prices
        ↓
Detects breakouts & places mock orders
        ↓
Events bubble up to ViewModel
        ↓
Repository updates state
        ↓
UI recomposes with new data
        ↓
User sees live trading simulation!
```

---

## Key Mock Classes

### 1. **MockMarketDataSource**
**What:** Generates fake stock prices

**File:** `data/engine/mock/MockMarketDataSource.kt`

**Generates:** LTP price every 1 second
```
185.00 → 185.23 → 185.45 → ... → 188.50
```

**Configuration:**
```kotlin
MockMarketDataSource(
    basePrice = 185.0,        // Starting price
    volatility = 0.5,         // Price movement range
    updateIntervalMs = 1000   // Update frequency
)
```

### 2. **MockOrderExecutor**
**What:** Simulates order execution

**File:** `data/engine/mock/MockOrderExecutor.kt`

**Features:**
- Returns success instantly (with 500ms simulated delay)
- Generates fake order IDs
- Tracks positions in memory
- Optional failure simulation

**Configuration:**
```kotlin
MockOrderExecutor(
    executionDelayMs = 500,  // Simulate network delay
    failureRate = 0          // 0% failure rate (always succeeds)
)
```

### 3. **MockScenarios**
**What:** Pre-configured test scenarios

**File:** `data/engine/mock/MockScenarios.kt`

**Available:**
```kotlin
MockScenarios.successfulHighBreakout()  // Bullish scenario
MockScenarios.stopLossScenario()        // Bearish scenario
```

---

## Where Mock Data is Used

### **In TradingViewModel**
```kotlin
fun initializeAndStartMockStrategy(scenario: String = "normal") {
    // Creates mock data sources
    val mockDataSource = MockScenarios.successfulHighBreakout().first
    val mockExecutor = MockOrderExecutor(executionDelayMs = 500)
    
    // Passes to strategy engine
    strategyEngine = OrbStrategyEngine(
        marketDataSource = mockDataSource,  ← MOCK
        orderExecutor = mockExecutor,       ← MOCK
        config = config,
        riskSettings = riskSettings.value
    )
    
    // Start processing
    strategyEngine?.start()
}
```

---

## Data Transformation Steps

```
Step 1: MockMarketDataSource.subscribeLTP()
        └─ Emits: Flow<Double> (185.0, 185.23, 185.45, ...)

Step 2: OrbStrategyEngine collects the flow
        └─ Emits: StrategyEvent (PriceUpdate, OrbCaptured, etc)

Step 3: ViewModel.observeStrategyEvents()
        └─ Updates: AppState in Repository

Step 4: Repository.appState (StateFlow)
        └─ Emits: State updates

Step 5: DashboardScreen.collectAsStateWithLifecycle()
        └─ Recomposes: UI with new data

Step 6: User sees results!
```

---

## Real-Time Data Updates

### **Every Second**
```
Time 0s:   MockMarketDataSource generates LTP = 185.00
           OrbStrategyEngine processes it
           Emits StrategyEvent.PriceUpdate
           ViewModel updates state
           UI shows: "LTP: ₹185.00"

Time 1s:   MockMarketDataSource generates LTP = 185.23
           Same flow...
           UI updates: "LTP: ₹185.23"

Time 2s:   LTP = 185.45 → UI updates
...continues for 15+ minutes
```

### **When Breakout Occurs**
```
Time 10m 30s: LTP = 188.50 > HIGH (186.45)
              
              OrbStrategyEngine detects BREAKOUT
              Emits: StrategyEvent.TradeEntered
              
              ViewModel observes event
              Repository.addTrade(...)
              Updates: activePositions = 1
              
              UI updates IMMEDIATELY
              User sees: "TRADE OPENED at ₹185.23"
```

---

## Observing Mock Data in Logs

When you run the app with mock strategy, watch for these logs:

```
📊 Price Updates (every second):
[MockMarketDataSource] Emitting LTP: 185.23
[MockMarketDataSource] Emitting LTP: 185.45

🎯 ORB Captured (after 15 min):
[OrbStrategyEngine] ✅ ORB Captured
[OrbStrategyEngine] HIGH: 186.45, LOW: 184.89

⚡ Breakout Detected:
[OrbStrategyEngine] ✅ HIGH BREAKOUT! LTP: 188.50 > 186.45

🎁 Order Executed:
[MockOrderExecutor] Placing MARKET order - BUY 50 x NIFTY24DEC22000CE
[MockOrderExecutor] Mock order executed - Order ID: abc123, Price: 185.23

🏁 Position Opened:
[ViewModel] 🟢 Trade Entered: Position ID: POS_001
[Repository] Added trade to database

🎯 Target Hit:
[OrbStrategyEngine] 🎯 Target Hit! Profit: ₹262.50
[Repository] Trade closed - Total P&L: +₹262.50
[UI] Shows: "✅ Target Hit! Profit: ₹262.50"
```

---

## Testing Different Scenarios

### **Scenario 1: Successful High Breakout (Bullish)**
```kotlin
val mockDataSource = MockScenarios.successfulHighBreakout().first

// What happens:
// 1. Low volatility (0.3) - prices move smoothly
// 2. Prices gradually rise
// 3. Breakout happens: LTP > HIGH
// 4. BUY CE order placed
// 5. Price continues rising → TARGET HIT
// 6. Trade closed with PROFIT ✅
```

### **Scenario 2: Stop Loss Hit (Bearish)**
```kotlin
val mockDataSource = MockScenarios.stopLossScenario().first

// What happens:
// 1. High volatility (1.5) - prices jump around
// 2. Breakout happens: LTP < LOW
// 3. BUY PE order placed
// 4. Price drops suddenly → STOP LOSS HIT
// 5. Trade closed with LOSS ❌
```

---

## UI Updates in Real-Time

### **Dashboard Screen**
```
┌─────────────────────────────────────┐
│ ORB Strategy Simulation             │
├─────────────────────────────────────┤
│ Status: ● ACTIVE                    │
│ ORB High: 186.45                    │
│ ORB Low: 184.89                     │
│ Current LTP: 185.50 (updates/sec)   │ ← LIVE
│ ─────────────────────────────────────│
│ P&L: ₹150.00 (updates on close)     │ ← LIVE
│ Win Rate: 100% (2 out of 2 won)     │ ← LIVE
│ Open Positions: 1                   │ ← LIVE
│                                     │
│         [ START ]  [ STOP ]         │
│       [ EMERGENCY STOP ]            │
└─────────────────────────────────────┘
```

### **Positions Screen**
```
┌──────────────────────────────────────┐
│ NIFTY 22000 CE (OPEN)               │
├──────────────────────────────────────┤
│ Entry Price: ₹185.23                │
│ Current Price: ₹190.50              │ ← LIVE
│ P&L: +₹262.50                       │ ← LIVE
│ Status: TRACKING TARGET ₹200.23     │
│                                      │
│ [Close Position]  [Edit Stop Loss]  │
└──────────────────────────────────────┘
```

### **Trade History Screen**
```
┌──────────────────────────────────────┐
│ Trade #1: CLOSED (PROFIT)           │
├──────────────────────────────────────┤
│ Entry: ₹185.23  Exit: ₹200.23       │
│ Profit: +₹750 (50 lots × ₹15)       │
│ Duration: 5 minutes                  │
│ Type: HIGH BREAKOUT                  │
└──────────────────────────────────────┘
```

---

## Key Points

✅ **Mock data flows through REAL app code**
- Not just in previews
- Uses actual ViewModel, Repository, etc.
- Same state management as production

✅ **Mock data is continuous**
- New price every second
- Events triggered automatically
- UI updates in real-time

✅ **Mock data is deterministic**
- Same scenario produces same result
- Can replay bugs
- Great for testing

✅ **Easy to switch to real data**
- Just replace MockMarketDataSource
- All other code stays the same
- No UI changes needed

---

## Switching to Real Data

When ready to use real Angel One API:

```kotlin
// BEFORE (Mock):
val mockDataSource = MockMarketDataSource(basePrice = 185.0)

// AFTER (Real):
val realDataSource = AngelMarketDataSource(
    apiKey = BuildConfig.ANGEL_API_KEY,
    token = BuildConfig.ANGEL_ACCESS_TOKEN,
    userId = BuildConfig.ANGEL_USER_ID
)

// Rest of code is IDENTICAL!
strategyEngine = OrbStrategyEngine(
    marketDataSource = realDataSource,  ← Just swap this
    orderExecutor = realExecutor,       ← And this
    config = config,
    riskSettings = riskSettings
)
```

---

## Troubleshooting

### **No logs appearing**
```
❌ Problem: Started mock strategy but no logs
✅ Solution: Check TradingViewModel.toggleStrategy() is being called
✅ Check: Timber is configured in Application class
✅ Check: Logcat is showing your app package (com.trading.orb)
```

### **UI not updating**
```
❌ Problem: UI doesn't show P&L changes
✅ Solution: Check if collectAsStateWithLifecycle() is used
✅ Solution: Verify appState from repository is being collected
✅ Solution: Check if recomposition is happening
```

### **Breakout not detected**
```
❌ Problem: Mock prices generated but no breakout
✅ Solution: Increase volatility in MockMarketDataSource
✅ Solution: Check breakoutBuffer in StrategyConfig
✅ Solution: Verify ORB window times (9:15-9:30)
```

---

## Summary

**Mock Data Architecture:**

```
MockMarketDataSource (generates prices)
        ↓
OrbStrategyEngine (processes prices)
        ↓
StrategyEvent (signals important events)
        ↓
ViewModel.observeStrategyEvents() (listens for events)
        ↓
Repository.appState (updates state)
        ↓
UI (collects state and displays)
        ↓
User sees complete trading simulation!
```

**All flows through REAL app code** - Perfect for development and testing! 🚀

---

## Related Documents

- `MOCK_DATA_RUNTIME_FLOW.md` - Detailed step-by-step flow
- `MOCK_VS_REAL_COMPARISON.md` - Mock vs Real differences
- `UI_MOCKING_ARCHITECTURE.md` - Preview provider pattern
- `ORB_STRATEGY_ARCHITECTURE.md` - Strategy engine details
