# 🎯 UI Mocking Flow Diagram

## 1️⃣ Data Generation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    PREVIEW DATA GENERATION                   │
└─────────────────────────────────────────────────────────────┘

DashboardPreviewProvider (Object - Singleton)
│
├─── sampleDailyStats() ───────────────────────┐
│    Returns: DailyStats                       │
│    ├─ totalPnl: 2450.0                       │
│    ├─ activePositions: 2                     │
│    └─ winRate: 68.0                          │
│                                              │
├─── sampleOrbLevels() ────────────────────────┤
│    Returns: OrbLevels                        │
│    ├─ instrument: NIFTY24DEC22000CE          │
│    ├─ high: 188.0                            │
│    ├─ low: 183.0                             │
│    └─ ltp: 185.50                            │
│                                              ├──> sampleAppState()
├─── sampleAppState() ─────────────────────────┤
│    Returns: AppState                         │
│    ├─ tradingMode: PAPER                     │
│    ├─ strategyStatus: ACTIVE                 │
│    ├─ connectionStatus: CONNECTED            │
│    ├─ dailyStats: [from above]               │
│    └─ orbLevels: [from above]                │
│                                              │
└─── sampleDashboardUiState() ─────────────────┘
     Returns: DashboardUiState
     ├─ loading: LoadingState(false)
     ├─ error: ErrorState()
     └─ isRefreshing: false
```

---

## 2️⃣ Preview Composable Rendering

```
┌─────────────────────────────────────────────────────────────┐
│                  @PREVIEW COMPOSABLE FUNCTIONS               │
└─────────────────────────────────────────────────────────────┘

@Preview(name = "Dashboard - Active Strategy")
├─ Call: DashboardScreenPreview()
│   └─> DashboardScreenContent(
│       uiState = DashboardPreviewProvider.sampleDashboardUiState()
│       appState = DashboardPreviewProvider.sampleAppState()
│       ...callbacks = {}
│   )
│       ↓
│       Renders UI with mock data
│       ↓
│       ╔═══════════════════════════════╗
│       ║    Dashboard (Preview)        ║
│       ║  Strategy Status: ● Active    ║
│       ║  P&L: ₹2,450                  ║
│       ║  Win Rate: 68%                ║
│       ╚═══════════════════════════════╝

@Preview(name = "Dashboard - Positive P&L")
├─ Call: DashboardScreenPositivePreview()
│   └─> DashboardScreenContent(
│       appState = DashboardPreviewProvider.sampleAppState(
│           totalPnl = 5000.0,
│           winRate = 75.0
│       )
│   )
│       ↓
│       ╔═══════════════════════════════╗
│       ║    Dashboard (Preview)        ║
│       ║  Strategy Status: ● Active    ║
│       ║  P&L: ₹5,000  ✅              ║
│       ║  Win Rate: 75%                ║
│       ╚═══════════════════════════════╝

@Preview(name = "Dashboard - Negative P&L")
├─ Call: DashboardScreenNegativePreview()
│   └─> DashboardScreenContent(
│       appState = DashboardPreviewProvider.sampleAppState(
│           totalPnl = -1250.0,
│           winRate = 35.0
│       )
│   )
│       ↓
│       ╔═══════════════════════════════╗
│       ║    Dashboard (Preview)        ║
│       ║  Strategy Status: ● Active    ║
│       ║  P&L: ₹-1,250  ❌             ║
│       ║  Win Rate: 35%                ║
│       ╚═══════════════════════════════╝
```

---

## 3️⃣ Multiple Preview Variations

```
┌────────────────────────────────────────────────────────────────┐
│                    ANDROID STUDIO PREVIEW PANE                 │
└────────────────────────────────────────────────────────────────┘

📱 Dashboard - Active Strategy
├─────────────────────────────
│  Strategy Status: ● Active  │
│  P&L: ₹2,450                │
│  [START] [MODE] [STOP]      │
├─────────────────────────────

📱 Dashboard - Positive P&L
├─────────────────────────────
│  Strategy Status: ● Active  │
│  P&L: ₹5,000  ✅            │
│  [START] [MODE] [STOP]      │
├─────────────────────────────

📱 Dashboard - Negative P&L
├─────────────────────────────
│  Strategy Status: ● Active  │
│  P&L: ₹-1,250  ❌           │
│  [START] [MODE] [STOP]      │
├─────────────────────────────

📱 Dashboard - Inactive
├─────────────────────────────
│  Strategy Status: ○ Inactive│
│  P&L: ₹0                    │
│  [START] [MODE] [STOP]      │
└─────────────────────────────

All visible & interactive in real-time!
No need to run app or wait for data
```

---

## 4️⃣ Data Model Hierarchy

```
┌───────────────────────────────────────────────┐
│              DATA MODEL LAYER                  │
└───────────────────────────────────────────────┘

AppState (UI State Model)
│
├─ tradingMode: TradingMode
│  ├─ PAPER
│  └─ LIVE
│
├─ strategyStatus: StrategyStatus
│  ├─ ACTIVE
│  ├─ INACTIVE
│  ├─ PAUSED
│  └─ ERROR
│
├─ connectionStatus: ConnectionStatus
│  ├─ CONNECTED
│  └─ DISCONNECTED
│
├─ dailyStats: DailyStats
│  ├─ totalPnl: Double
│  ├─ activePositions: Int
│  └─ winRate: Double
│
└─ orbLevels: OrbLevels?
   ├─ instrument: Instrument
   ├─ high: Double
   ├─ low: Double
   ├─ ltp: Double
   └─ breakoutBuffer: Int
```

---

## 5️⃣ Real vs Mock Data Flow

```
┌─────────────────────────┐                 ┌──────────────────────┐
│   REAL APP FLOW         │                 │  PREVIEW FLOW        │
│  (Running on device)    │                 │ (Android Studio)     │
└─────────────────────────┘                 └──────────────────────┘

User launches app                           Developer opens file
        ↓                                              ↓
MainActivity.onCreate()                      Gradle builds @Preview
        ↓                                              ↓
TradingViewModel created                     PreviewProvider called
        ↓                                              ↓
Collects repository.appState                 sampleAppState() returns
        ↓                                     mock AppState
Backend returns live data                             ↓
        ↓                                    UI renders with mock data
appState updates                                      ↓
        ↓                                    Preview pane displays
UI recompose with real data                  multiple variations
        ↓                                              ↓
User sees live trading info              Developer sees all UI states
        ↓                                              ↓
User interacts (click START)             Can test without backend
        ↓                                              ↓
toggleStrategy() called                  Fast iteration & testing
        ↓
repository.startStrategy()
        ↓
appState.copy(strategyStatus = ACTIVE)
        ↓
UI updates (button changes to STOP)
```

---

## 6️⃣ Screen Mocking Variations

```
┌──────────────────────────────────────────────────────────────────┐
│                   ALL SCREENS & THEIR VARIATIONS                 │
└──────────────────────────────────────────────────────────────────┘

DASHBOARD SCREEN
├─ ✅ Active Strategy
├─ ✅ Positive P&L
├─ ✅ Negative P&L
├─ ✅ Inactive
└─ ✅ Multiple Positions

POSITIONS SCREEN
├─ ✅ Profitable Positions
├─ ✅ Loss Positions
├─ ✅ Mixed (Profit + Loss)
├─ ✅ Empty State
└─ ✅ Filtered View (Long/Short)

TRADE HISTORY SCREEN
├─ ✅ Winning Trades
├─ ✅ Losing Trades
├─ ✅ Today's Trades
├─ ✅ Weekly Trades
└─ ✅ Empty History

STRATEGY CONFIG SCREEN
├─ ✅ Default Config
├─ ✅ Aggressive Config
├─ ✅ Conservative Config
└─ ✅ Custom Instrument

RISK SCREEN
├─ ✅ Normal Risk
├─ ✅ High Risk
└─ ✅ Risk Limit Exceeded

LOGS SCREEN
├─ ✅ Clean Logs (INFO only)
├─ ✅ With Warnings
├─ ✅ With Errors
└─ ✅ Empty Logs
```

---

## 7️⃣ File Organization

```
app/src/main/java/com/trading/orb/ui/screens/
│
├── dashboard/
│   ├── DashboardScreen.kt ──────────────── Contains @Preview functions
│   ├── DashboardPreviewProvider.kt ─────── Contains mock data generators
│   ├── DashboardUiState.kt ────────────── Data models
│   └── DashboardViewModel.kt ──────────── Real state management
│
├── positions/
│   ├── PositionsScreen.kt
│   ├── PositionsPreviewProvider.kt
│   ├── PositionUiState.kt
│   └── PositionsViewModel.kt
│
├── tradehistory/
│   ├── TradeHistoryScreen.kt
│   ├── TradeHistoryPreviewProvider.kt
│   └── TradeHistoryViewModel.kt
│
├── strategy/
│   ├── StrategyConfigScreen.kt
│   ├── StrategyConfigPreviewProvider.kt
│   └── StrategyConfigViewModel.kt
│
├── risk/
│   ├── RiskScreen.kt
│   ├── RiskPreviewProvider.kt
│   └── RiskViewModel.kt
│
├── liveloggers/
│   ├── LiveLogsScreen.kt
│   ├── LiveLogsPreviewProvider.kt
│   └── LiveLogsViewModel.kt
│
└── more/
    ├── MoreScreen.kt
    ├── MorePreviewProvider.kt
    └── MoreViewModel.kt
```

---

## 8️⃣ Preview Provider Methods by Screen

```
┌────────────────┬──────────────────────────────────────┐
│   SCREEN       │      KEY MOCK METHODS                │
├────────────────┼──────────────────────────────────────┤
│ Dashboard      │ sampleAppState()                     │
│                │ sampleDailyStats()                   │
│                │ sampleOrbLevels()                    │
│                │ sampleDashboardUiState()             │
├────────────────┼──────────────────────────────────────┤
│ Positions      │ samplePositionsList()                │
│                │ samplePositionUiModel()              │
│                │ samplePositionsUiState()             │
├────────────────┼──────────────────────────────────────┤
│ Trade History  │ sampleTradesList()                   │
│                │ sampleTradeUiModel()                 │
│                │ sampleHistoryFilter()                │
├────────────────┼──────────────────────────────────────┤
│ Strategy       │ sampleStrategyConfig()               │
│                │ sampleRiskSettings()                 │
│                │ sampleStrategyUiState()              │
├────────────────┼──────────────────────────────────────┤
│ Risk           │ sampleRiskState()                    │
│                │ sampleDailyLossLimit()               │
│                │ sampleDrawdownProtection()           │
├────────────────┼──────────────────────────────────────┤
│ Logs           │ sampleLogEntries()                   │
│                │ sampleLogEntry()                     │
│                │ sampleLogsUiState()                  │
├────────────────┼──────────────────────────────────────┤
│ More           │ sampleMenuItems()                    │
│                │ sampleUserInfo()                     │
│                │ sampleSettings()                     │
└────────────────┴──────────────────────────────────────┘
```

---

## Summary: The Magic of UI Mocking

```
┌─────────────────────────────────────┐
│   DEVELOPER WORKFLOW                │
├─────────────────────────────────────┤
│ 1. Open DashboardScreen.kt          │
│ 2. See Preview Pane on right        │
│ 3. See ALL UI variations at once    │
│ 4. Make code changes                │
│ 5. Changes reflect in real-time     │
│ 6. No need to run app               │
│ 7. No need for backend data         │
│ 8. Fast iteration & testing         │
└─────────────────────────────────────┘

RESULT: ⚡ 10X FASTER UI DEVELOPMENT ⚡
```

---

## Key Benefits Visualized

```
WITHOUT UI MOCKING                 WITH UI MOCKING
──────────────────────            ─────────────────
1. Change UI code                 1. Change UI code
   ↓                                 ↓
2. Recompile app                  2. See preview instantly
   ↓                                 ↓
3. Run on emulator                3. Multiple states visible
   ↓                                 ↓
4. Navigate to screen             4. Continue editing
   ↓                                 ↓
5. Click buttons to test          5. Done!
   ↓
6. Wait for changes...
   (can take 30+ seconds)
```

