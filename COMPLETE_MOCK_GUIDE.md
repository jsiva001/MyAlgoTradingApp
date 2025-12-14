# 📚 Complete Mock Data Guide - All You Need to Know

## Overview

This document ties together all aspects of mock data in your ORB trading app.

---

## 🎯 Three Types of Mocking

### 1. **UI Preview Mocking** (Static Data)
**What:** Android Studio preview of UI components

**Used for:** Seeing how screens look

**Files:**
- `DashboardPreviewProvider.kt`
- `PositionsPreviewProvider.kt`
- `@Preview` Composable functions

**Example:**
```kotlin
@Preview
fun DashboardScreenPreview() {
    DashboardScreenContent(
        appState = DashboardPreviewProvider.sampleAppState()  ← Static data
    )
}
```

**Key:** Data is **static and instant**, just for visualization

---

### 2. **Runtime Mock Data** (Continuous Simulation)
**What:** Full trading simulation while app is running

**Used for:** Testing strategy logic end-to-end

**Files:**
- `MockMarketDataSource.kt` - Generates prices
- `MockOrderExecutor.kt` - Simulates execution
- `MockScenarios.kt` - Pre-configured scenarios

**Example:**
```kotlin
fun initializeAndStartMockStrategy() {
    val mockDataSource = MockMarketDataSource()  ← Generates prices
    strategyEngine = OrbStrategyEngine(mockDataSource, ...)
    strategyEngine?.start()  ← Runs full simulation
}
```

**Key:** Data is **continuous and realistic**, simulates real trading

---

### 3. **Test Data Mocking** (Unit Tests)
**What:** Mock data for unit and integration tests

**Used for:** Testing individual components in isolation

**Files:**
- Test classes with `@get:Rule val instantExecutorRule`
- Mock builders for data models

**Key:** Data is **test-specific**, tests isolated behavior

---

## 🔄 Complete Data Flow

```
┌──────────────────────────────────────────────────────────────┐
│           RUNTIME MOCK DATA COMPLETE FLOW                     │
└──────────────────────────────────────────────────────────────┘

ANDROID STUDIO PREVIEW                RUNNING APP
─────────────────────────────         ────────────────────────

DashboardPreviewProvider              User clicks START button
    ↓                                      ↓
sampleAppState()                      initializeAndStartMockStrategy()
    ↓                                      ↓
Static data loaded                    Create MockMarketDataSource
    ↓                                      ↓
@Preview renders                      MockMarketDataSource.subscribeLTP()
    ↓                                      ↓
Shows one frame                       Flow<Double> emits every 1s
    ↓                                      ↓
User sees snapshot                    185.0→185.23→185.45→...→188.5
                                           ↓
                                      OrbStrategyEngine processes
                                           ↓
                                      Detects breakout → TRADE ENTERED
                                           ↓
                                      MockOrderExecutor.placeOrder()
                                           ↓
                                      ViewModel observes event
                                           ↓
                                      Repository updates state
                                           ↓
                                      UI recomposes with new data
                                           ↓
                                      User sees live simulation!
```

---

## 📊 Where Mock Data is Used

### **During Development**
```
Phase 1: UI Building
├─ Use: Preview Provider (static data)
├─ Goal: Build layouts without data
└─ Example: See button positions, colors

Phase 2: Feature Integration
├─ Use: Runtime Mock Data
├─ Goal: Test features work together
└─ Example: START button triggers strategy

Phase 3: Strategy Testing
├─ Use: MockScenarios + Runtime Mock
├─ Goal: Test trading logic
└─ Example: Verify breakout detection works
```

### **During Testing**
```
Phase 1: Unit Tests
├─ Use: Test Data Mocking
├─ Goal: Test components in isolation
└─ Example: Test P&L calculation logic

Phase 2: Integration Tests
├─ Use: Runtime Mock Data + Mock objects
├─ Goal: Test components together
└─ Example: Test ViewModel updates state correctly

Phase 3: UI Tests
├─ Use: Preview Provider + Mock data
├─ Goal: Test UI rendering
└─ Example: Test P&L displays correctly
```

---

## �� How to Use Mock Data

### **1. Building UI (Preview)**
```kotlin
// Open DashboardScreen.kt
// Right-click → Show Compose Preview
// See multiple @Preview variations
// Make code changes → See changes instantly
// No need to run app!
```

### **2. Testing Strategy (Runtime Mock)**
```kotlin
// Run app on emulator
// Navigate to Dashboard
// Click START button
// Strategy uses MockMarketDataSource
// Watch prices update in real-time
// See when trades trigger
// Check P&L calculations
```

### **3. Testing Specific Scenario**
```kotlin
// Edit initializeAndStartMockStrategy()
val mockDataSource = when (scenario) {
    "high_breakout" → MockScenarios.successfulHighBreakout().first
    "stop_loss" → MockScenarios.stopLossScenario().first
    else → MockScenarios.successfulHighBreakout().first
}

// Run app with different scenario
// Verify behavior
```

---

## 🔑 Key Components Deep Dive

### **MockMarketDataSource**
```
Purpose: Generate fake LTP prices
Location: data/engine/mock/MockMarketDataSource.kt

How it works:
├─ subscribeLTP() returns Flow<Double>
├─ Generates price every 1000ms
├─ Price movement: random.nextDouble(-volatility, +volatility)
├─ Emits: 185.0 → 185.23 → 185.45 → ...
└─ Continues forever until strategy stops

Configurability:
├─ basePrice: starting price (default 185.0)
├─ volatility: price movement range (default 0.5)
└─ updateIntervalMs: frequency (default 1000)

Real-world equivalent:
└─ Replaced by AngelMarketDataSource (WebSocket)
```

### **MockOrderExecutor**
```
Purpose: Simulate order execution
Location: data/engine/mock/MockOrderExecutor.kt

How it works:
├─ placeMarketOrder() is called
├─ Simulates 500ms network delay
├─ Returns OrderResponse with success
├─ Generates random order ID
├─ Stores position in memory
└─ Never fails (unless failureRate set)

Configurability:
├─ executionDelayMs: simulated delay (default 500)
└─ failureRate: % chance of failure (default 0)

Real-world equivalent:
└─ Replaced by AngelOrderExecutor (REST API)
```

### **MockScenarios**
```
Purpose: Provide pre-configured test scenarios
Location: data/engine/mock/MockScenarios.kt

Scenarios:
├─ successfulHighBreakout()
│  ├─ basePrice: 185.0
│  ├─ volatility: 0.3 (low, stable)
│  └─ Result: High breakout → BUY CE → PROFIT
│
└─ stopLossScenario()
   ├─ basePrice: 189.0
   ├─ volatility: 1.5 (high, volatile)
   └─ Result: Low breakout → BUY PE → LOSS
```

---

## 📈 State Flow Diagram

```
MockMarketDataSource
    ↓ (Flow<Double>)
OrbStrategyEngine.waitAndCaptureOrb()
    ↓ (StrategyEvent.OrbCaptured)
OrbStrategyEngine.monitorForBreakout()
    ↓ (StrategyEvent.TradeEntered)
ViewModel.observeStrategyEvents()
    ↓ (Update internal state)
Repository.appState (StateFlow)
    ↓ (Emit updated state)
DashboardScreen.collectAsStateWithLifecycle()
    ↓ (Receive new state)
Composable recomposes
    ↓ (Render new UI)
User sees updated information!
```

---

## 🎯 Expected Behavior

### **Timeline of Mock Trade**
```
T+0min    START button clicked
          └─ MockMarketDataSource starts emitting prices
          └─ OrbStrategyEngine starts

T+0-15min ORB Window (collecting prices)
          └─ LTP: 185.0 → 185.23 → ... → 186.45
          └─ Logs: "Collecting ORB data..."

T+15min   ORB Captured
          └─ HIGH: 186.45, LOW: 184.89
          └─ Logs: "ORB Captured!"
          └─ UI updates: Shows ORB levels

T+15-∞min Monitoring for breakout
          └─ LTP: 185.5 → 186.0 → 187.0 → 188.0
          └─ Checking: LTP > 188.45 OR LTP < 182.89?

T+20min   BREAKOUT! LTP = 188.50 > 188.45
          └─ Logs: "✅ HIGH BREAKOUT!"
          └─ Calls: orderExecutor.placeMarketOrder()
          └─ Returns: OrderResponse (success)
          └─ Emits: StrategyEvent.TradeEntered

T+20min+500ms Trade Entered
          └─ ViewModel receives event
          └─ Updates: activePositions = 1
          └─ Logs: "🟢 Trade Entered!"
          └─ UI shows: "Open Position: NIFTY 22000 CE @ ₹185.23"

T+21min   Price continues: 189.0 → 190.0 → 195.0
          └─ ViewModel observes PriceUpdate events
          └─ Updates: ltp = 195.0
          └─ UI updates: "Current LTP: ₹195.0"

T+21min30s TARGET HIT! P&L = +₹750 (profit)
          └─ Logs: "🎯 Target Hit!"
          └─ ViewModel closes position
          └─ Updates: totalPnl = +₹750
          └─ UI shows: "✅ Target Hit! Profit: ₹750"
          └─ Button changes: START (ready for next trade)
```

---

## 📋 Checklist: Understanding Mock Data

### **UI Preview (Static)**
- [ ] Know what @Preview does
- [ ] Know how to use Preview Provider
- [ ] Can see multiple preview variations
- [ ] Understand it's just for visualization

### **Runtime Mock (Continuous)**
- [ ] Know how MockMarketDataSource works
- [ ] Can trace price flow through OrbStrategyEngine
- [ ] Understand event emission
- [ ] Can see logs while trading
- [ ] Know how to change scenarios

### **Integration**
- [ ] Can start mock strategy from dashboard
- [ ] See prices updating every second
- [ ] Watch for breakout signals
- [ ] See trades executing
- [ ] Understand P&L calculations

### **Switching to Real**
- [ ] Know where to change data source
- [ ] Know what AngelMarketDataSource is
- [ ] Know it's just one class swap
- [ ] Rest of code stays the same

---

## 🧪 Testing with Mock Data

### **Reproducible Test Scenario**
```kotlin
@Test
fun testHighBreakoutDetection() {
    val mockDataSource = MockScenarios.successfulHighBreakout().first
    val config = mockDataSource.config
    
    val engine = OrbStrategyEngine(
        marketDataSource = mockDataSource,
        orderExecutor = MockOrderExecutor(),
        config = config,
        riskSettings = RiskSettings(...)
    )
    
    // Every run produces same result!
    // Because mock data is deterministic
}
```

### **Advantage**
```
Real Data Testing       vs    Mock Data Testing
─────────────────────        ─────────────────
Depends on market            Always same result
Takes hours                  Takes minutes
Can't reproduce              Easily reproducible
Need real broker             No dependencies
Can lose money               Safe to test
Hard to debug                Easy to debug
```

---

## ⚡ Performance Implications

### **Mock Data Performance**
```
CPU: Low
├─ Simple random number generation
├─ No network I/O
└─ No database queries

Memory: ~5MB
├─ Prices stored in memory
├─ No persistence
└─ Cleared on app exit

Network: None
├─ No internet required
├─ No API calls
└─ Works offline

Battery: Minimal
├─ No network drain
├─ Minimal CPU usage
└─ Comparable to UI interaction
```

### **Real Data Performance**
```
CPU: Medium
├─ WebSocket parsing
├─ Event processing
├─ Database I/O

Memory: ~20MB
├─ Prices persisted to DB
├─ Trades stored
├─ Data survives restart

Network: Required
├─ WebSocket connection
├─ REST API calls
└─ Continuous data flow

Battery: Significant
├─ Network drain
├─ CPU usage
└─ Depends on activity
```

---

## 🚀 Switching from Mock to Real

### **One-Step Switch**
```kotlin
// File: TradingViewModel.kt

// BEFORE:
val mockDataSource = MockMarketDataSource()

// AFTER:
val realDataSource = AngelMarketDataSource(
    apiKey = BuildConfig.ANGEL_API_KEY,
    token = BuildConfig.ANGEL_ACCESS_TOKEN
)

// That's it! Everything else works!
```

### **What Changes**
```
MockMarketDataSource              AngelMarketDataSource
├─ Generates prices               ├─ Receives from API
├─ Every 1 second                 ├─ Every tick (~100ms)
├─ Random-ish                      ├─ Real market prices
└─ Controlled scenarios            └─ Unpredictable

MockOrderExecutor                 AngelOrderExecutor
├─ Always succeeds                ├─ Can fail
├─ Instant (500ms sim)            ├─ Depends on broker
└─ In memory                       └─ Persisted at broker

Same OrbStrategyEngine!
Same ViewModel!
Same Repository!
Same UI!
```

---

## 📚 Documentation Map

| Document | Purpose |
|----------|---------|
| **MOCK_DATA_QUICK_REFERENCE.md** | Quick start guide |
| **MOCK_DATA_RUNTIME_FLOW.md** | Detailed step-by-step flow |
| **MOCK_VS_REAL_COMPARISON.md** | Mock vs Real differences |
| **UI_MOCKING_ARCHITECTURE.md** | Preview provider pattern |
| **ORB_STRATEGY_ARCHITECTURE.md** | Strategy engine details |
| **COMPLETE_MOCK_GUIDE.md** | This file - overview of everything |

---

## 🎓 Learning Path

### **Day 1: Preview Mocking**
```
Read: UI_MOCKING_ARCHITECTURE.md
Do: Open DashboardScreen.kt
Do: Click "Show Compose Preview"
Do: See multiple UI variations
Do: Modify mock data in PreviewProvider
Do: See UI changes instantly
Result: Understand preview workflow
```

### **Day 2: Runtime Mock Data**
```
Read: MOCK_DATA_QUICK_REFERENCE.md
Do: Run app on emulator
Do: Click START button on Dashboard
Do: Watch logcat for price updates
Do: See ORB levels appear
Do: See breakout and trade
Result: Understand runtime simulation
```

### **Day 3: Complete Flow**
```
Read: MOCK_DATA_RUNTIME_FLOW.md
Do: Trace code from button click to UI update
Do: Study 9 steps in detail
Do: Identify key components
Do: Understand data transformation
Result: Master complete architecture
```

### **Day 4: Mock vs Real**
```
Read: MOCK_VS_REAL_COMPARISON.md
Do: Understand differences
Do: Know advantages of each
Do: Plan migration strategy
Do: Know when to use which
Result: Ready to implement real data
```

---

## 🎯 Quick Answers

**Q: Where do prices come from when I click START?**
A: `MockMarketDataSource.subscribeLTP()` - generates them randomly

**Q: How often do prices update?**
A: Every 1 second (configurable via `updateIntervalMs`)

**Q: Do mock trades affect real money?**
A: No, they're simulated in memory only

**Q: Can I see the prices in logs?**
A: Yes, search logcat for "Mock:" or "Emitting LTP"

**Q: How do I test different scenarios?**
A: Use `MockScenarios.successfulHighBreakout()` or `stopLossScenario()`

**Q: What happens when I switch to real data?**
A: Same code, same logic, just real prices and real money

**Q: Will the UI look the same with real data?**
A: Yes, 100% identical

---

## Summary

```
┌─────────────────────────────────────────────────────┐
│         MOCK DATA IN YOUR ORB TRADING APP            │
├─────────────────────────────────────────────────────┤
│                                                     │
│  THREE TYPES OF MOCKING:                           │
│  1. UI Preview (static) - for visualization        │
│  2. Runtime Mock (continuous) - for simulation     │
│  3. Test Data (isolated) - for unit tests          │
│                                                     │
│  PURPOSE:                                           │
│  ✅ Develop without backend                        │
│  ✅ Test without real money                        │
│  ✅ Control exact scenarios                        │
│  ✅ Reproduce bugs reliably                        │
│                                                     │
│  KEY INSIGHT:                                       │
│  Same code works with BOTH mock and real data!     │
│  Just swap the data source implementation          │
│                                                     │
│  ARCHITECTURE:                                      │
│  Data → Engine → Events → ViewModel → Repo → UI    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

This is your complete guide to understanding mock data in the app! 🚀
