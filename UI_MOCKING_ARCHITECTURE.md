# 🎭 UI Mocking Architecture - Complete Explanation

## Overview
The app uses a **centralized UI mocking strategy** across all screens using **Preview Providers** and **@Preview** Composable functions. This allows developers to visualize and test UI without needing actual data from the backend.

---

## Architecture Layers

### 1. **Preview Provider Pattern** (Data Generation)
Each screen has a companion `*PreviewProvider` object that generates fake/mock data.

**Location:** `app/src/main/java/com/trading/orb/ui/screens/{screen_name}/{Screen}PreviewProvider.kt`

**Pattern:**
```kotlin
object DashboardPreviewProvider {
    fun sampleDailyStats(): DailyStats { }
    fun sampleOrbLevels(): OrbLevels { }
    fun sampleAppState(): AppState { }
    fun sampleDashboardUiState(): DashboardUiState { }
}
```

### 2. **Preview Composables** (UI Display)
Each screen has multiple `@Preview` functions that use the Preview Provider data.

**Pattern:**
```kotlin
@Preview(name = "Dashboard - Active Strategy", showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreenContent(
        uiState = DashboardPreviewProvider.sampleDashboardUiState(),
        appState = DashboardPreviewProvider.sampleAppState(),
        onToggleStrategy = {},
        onToggleMode = {},
        onEmergencyStop = {},
        onRetry = {}
    )
}
```

---

## Mocking Pattern by Screen

### 📊 **Dashboard Screen**
**File:** `DashboardScreen.kt`

**Mocked Data:**
- `DailyStats`: P&L, Win Rate, Active Positions
- `OrbLevels`: High, Low, LTP, Breakout Buffer
- `AppState`: Trading Mode, Strategy Status, Connection Status
- `DashboardUiState`: Loading, Error states

**Mock Variations:**
1. ✅ **Active Strategy** - Strategy running with positions
2. ✅ **Positive P&L** - Green P&L values (+₹5,000)
3. ✅ **Negative P&L** - Red P&L values (-₹1,250)
4. ✅ **Inactive Strategy** - Strategy stopped, no positions
5. ✅ **Multiple Positions** - 5 active positions

**Example:**
```kotlin
@Preview
fun DashboardScreenPositivePreview() {
    DashboardScreenContent(
        appState = DashboardPreviewProvider.sampleAppState(
            totalPnl = 5000.0,
            winRate = 75.0
        ),
        ...
    )
}
```

### 💰 **Positions Screen**
**File:** `PositionsScreen.kt`

**Mocked Data:**
- `PositionUiModel`: Symbol, Entry Price, Current Price, P&L
- `Position List`: Multiple positions with different statuses
- Filter states: LONG, SHORT, ALL

**Mock Variations:**
1. ✅ **Profitable Positions** - Green P&L
2. ✅ **Loss Positions** - Red P&L
3. ✅ **Mixed Positions** - Combination of wins/losses
4. ✅ **Empty State** - No open positions
5. ✅ **Filtered View** - Long-only or Short-only

**Example:**
```kotlin
fun samplePositionsList(): List<PositionUiModel> {
    return listOf(
        samplePositionUiModel(symbol = "NIFTY50", profitLoss = 150.0),
        samplePositionUiModel(symbol = "FINNIFTY", profitLoss = -200.0)
    )
}
```

### 📈 **Trade History Screen**
**File:** `TradeHistoryScreen.kt`

**Mocked Data:**
- `Trade`: Entry Price, Exit Price, Duration, P&L, Win/Loss
- Filter: TODAY, THIS_WEEK, THIS_MONTH, ALL
- Sort: By Date, By P&L, By Duration

**Mock Variations:**
1. ✅ **Winning Trades** - Profitable trades
2. ✅ **Losing Trades** - Loss-making trades
3. ✅ **Filtered by Period** - Different time ranges
4. ✅ **Empty History** - No trades
5. ✅ **Large Dataset** - 50+ trades

### ⚙️ **Strategy Config Screen**
**File:** `StrategyConfigScreen.kt`

**Mocked Data:**
- `StrategyConfig`: Instrument, Timeframe, Entry Rules
- `RiskSettings`: Max Loss, Position Size, Stop Loss %
- `Parameter Values`: Min/Max for each setting

**Mock Variations:**
1. ✅ **Default Config** - Standard settings
2. ✅ **Aggressive Config** - High risk parameters
3. ✅ **Conservative Config** - Low risk parameters
4. ✅ **Custom Instrument** - Different stocks/options

### ⚠️ **Risk Management Screen**
**File:** `RiskScreen.kt`

**Mocked Data:**
- Daily Loss Limit
- Maximum Position Size
- Drawdown Protection
- Risk Warnings

**Mock Variations:**
1. ✅ **Normal Risk** - All checks pass
2. ✅ **High Risk** - Some warnings
3. ✅ **Exceeded Limits** - Risk limit hit

### 📋 **Logs Screen**
**File:** `LiveLogsScreen.kt`

**Mocked Data:**
- Log entries with timestamps
- Different log levels: INFO, WARNING, ERROR
- Log messages from trading events

**Mock Variations:**
1. ✅ **Clean Logs** - Only INFO level
2. ✅ **With Warnings** - Mix of INFO & WARN
3. ✅ **With Errors** - Mix of all levels
4. ✅ **Empty Logs** - No entries

### ⋯️ **More Menu Screen**
**File:** `MoreScreen.kt`

**Mocked Data:**
- Menu items
- User info
- Settings options

---

## Data Generation Hierarchy

```
Preview Provider (Object)
    ├─ sampleAtom()           ← Basic data unit
    │   ├─ DailyStats
    │   ├─ OrbLevels
    │   ├─ Position
    │   └─ Trade
    │
    ├─ sampleList()           ← Collection of data
    │   ├─ List<Position>
    │   ├─ List<Trade>
    │   └─ List<LogEntry>
    │
    └─ sampleUiState()        ← Complete UI state
        ├─ AppState
        ├─ DashboardUiState
        └─ PositionUiState
```

---

## How Preview Works in Android Studio

### 1. **In Code Editor**
```
When you open a file with @Preview:
├─ Preview Pane opens on right side
├─ Shows multiple preview variations
├─ Real-time rendering of Composables
└─ Can zoom, rotate, simulate different devices
```

### 2. **Interactive Preview**
```
Right-click on @Preview → Show Compose Preview
├─ Shows the composable with mock data
├─ Can interact with components
├─ Can change device/configuration
└─ Can see different screen sizes (phone, tablet, etc)
```

### 3. **Build Configuration**
Preview data is ONLY used during development:
- ❌ NOT included in release builds
- ❌ NOT included in debug APK data
- ✅ Used only by Android Studio's preview system
- ✅ Used by UI tests in debug builds

---

## Mocking Strategy for Each Component

### **State Management**
```kotlin
// Real State (used in Composable)
val appState by tradingViewModel.appState.collectAsStateWithLifecycle()

// Mock State (used in Preview)
val appState = DashboardPreviewProvider.sampleAppState()
```

### **Event Callbacks**
```kotlin
// Real Callbacks
onToggleStrategy = { tradingViewModel.toggleStrategy() }

// Mock Callbacks (empty lambdas)
onToggleStrategy = {}
```

### **UI State**
```kotlin
// Real Loading State
LoadingState(isLoading = true, loadingMessage = "Loading...")

// Mock Loading State
LoadingState(isLoading = false) // Instant display
```

---

## Benefits of This Architecture

✅ **Fast Development**
- See UI instantly without waiting for API calls
- No need to mock backend server
- Immediate visual feedback

✅ **Better Testing**
- Test UI with various data scenarios
- Verify error states, loading states, empty states
- No flaky network tests

✅ **Documentation**
- Preview providers document expected data shapes
- Show different UI variations visually
- Serve as reference for API contracts

✅ **Consistency**
- All screens follow same mocking pattern
- Easy to add new preview variations
- Reusable mock data across screens

✅ **Developer Experience**
- No need to run app to see changes
- Real-time preview updates as you code
- Multiple preview variations at once

---

## File Structure Summary

```
app/src/main/java/com/trading/orb/ui/screens/
├── dashboard/
│   ├── DashboardScreen.kt          ← Main composable + @Preview functions
│   ├── DashboardPreviewProvider.kt ← Mock data generation
│   ├── DashboardUiState.kt         ← Data models
│   └── DashboardViewModel.kt       ← Real state management
├── positions/
│   ├── PositionsScreen.kt
│   ├── PositionsPreviewProvider.kt
│   └── ...
├── tradehistory/
│   ├── TradeHistoryScreen.kt
│   ├── TradeHistoryPreviewProvider.kt
│   └── ...
└── ... (other screens follow same pattern)
```

---

## Quick Reference: Preview Provider Methods

| Screen | Preview Provider | Key Mock Methods |
|--------|------------------|------------------|
| Dashboard | `DashboardPreviewProvider` | `sampleAppState()`, `sampleOrbLevels()`, `sampleDailyStats()` |
| Positions | `PositionsPreviewProvider` | `samplePositionsList()`, `samplePositionUiModel()` |
| History | `TradeHistoryPreviewProvider` | `sampleTradesList()`, `sampleTradeUiModel()` |
| Strategy | `StrategyConfigPreviewProvider` | `sampleStrategyConfig()`, `sampleRiskSettings()` |
| Risk | `RiskPreviewProvider` | `sampleRiskState()` |
| Logs | `LiveLogsPreviewProvider` | `sampleLogEntries()` |
| More | `MorePreviewProvider` | `sampleMenuItems()` |

---

## Example: Creating a New Preview

1. **Add method to Preview Provider:**
```kotlin
object DashboardPreviewProvider {
    fun sampleAppStateWithError(): AppState {
        return AppState(
            strategyStatus = StrategyStatus.ERROR,
            ...
        )
    }
}
```

2. **Create Preview Composable:**
```kotlin
@Preview(name = "Dashboard - Error State")
@Composable
fun DashboardScreenErrorPreview() {
    DashboardScreenContent(
        appState = DashboardPreviewProvider.sampleAppStateWithError(),
        ...
    )
}
```

3. **View in Android Studio Preview Pane**
   - Right-click on file → Show Compose Preview
   - See the error state rendered
   - No need to run app!

---

## Summary

The UI mocking system provides:
- **Mock Data Generation** via Preview Providers (static objects)
- **Preview Display** via @Preview Composables (Android Studio)
- **Separation of Concerns** between UI and state management
- **Multiple Variations** of each screen for different scenarios
- **Zero Runtime Overhead** - only used during development

This enables fast, efficient UI development without backend dependencies! 🚀

