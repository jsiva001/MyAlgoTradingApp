# Frontend Implementation Roadmap

## 🎯 Objective
Complete all UI screens with proper state management before backend API integration.

---

## 📱 Screen Implementation Order

### 1️⃣ **Strategy Screen** (PRIORITY 1)
**Why First:** Core feature, uses timer + date picker components

**Implementation Steps:**
```
StrategyConfigScreen/
├── StrategyUiState.kt          (Create)
├── StrategyViewModel.kt         (Create)
├── StrategyConfigScreen.kt      (Update)
├── StrategyPreviewProvider.kt   (Create)
└── TimerComponent.kt            (Create - reusable)
```

**Data Model:**
```kotlin
data class StrategyConfigUiState(
    val strategyName: String = "",
    val selectedInstrument: Instrument? = null,
    val breakoutPercentage: Double = 0.5,
    val sessionStartTime: LocalTime = LocalTime.now(),
    val sessionEndTime: LocalTime = LocalTime.now().plusHours(6),
    val maxPositions: Int = 2,
    val riskPerTrade: Double = 100.0,
    val isActive: Boolean = false,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)
```

**Features:**
- Strategy configuration form
- Timer for session duration
- Date/Time pickers
- Save/Update strategy
- Enable/Disable strategy toggle
- Validation for input fields

---

### 2️⃣ **Positions Screen** (PRIORITY 2)
**Why Second:** Depends on position models, shows live data

**Implementation Steps:**
```
PositionsScreen/
├── PositionsUiState.kt         (Create)
├── PositionsViewModel.kt        (Create)
├── PositionsScreen.kt           (Update)
└── PositionsPreviewProvider.kt  (Create)
```

**Data Model:**
```kotlin
data class PositionsUiState(
    val positions: List<PositionItem> = emptyList(),
    val totalValue: Double = 0.0,
    val totalPnL: Double = 0.0,
    val selectedPosition: PositionItem? = null,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)

data class PositionItem(
    val positionId: String,
    val instrument: Instrument,
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val pnl: Double,
    val pnlPercentage: Double,
    val tradeMode: TradingMode
)
```

**Features:**
- List of open positions
- Position details (entry price, current price, P&L)
- Close position button
- Color-coded P&L display
- Real-time price updates
- Swipe to close actions

---

### 3️⃣ **Trade History Screen** (PRIORITY 3)
**Why Third:** Displays completed trades, uses date picker

**Implementation Steps:**
```
TradeHistoryScreen/
├── TradeHistoryUiState.kt      (Create)
├── TradeHistoryViewModel.kt     (Create)
├── TradeHistoryScreen.kt        (Update)
└── TradeHistoryPreviewProvider.kt (Create)
```

**Data Model:**
```kotlin
data class TradeHistoryUiState(
    val trades: List<TradeHistoryItem> = emptyList(),
    val filters: TradeFilter = TradeFilter(),
    val stats: TradeStats = TradeStats(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)

data class TradeHistoryItem(
    val tradeId: String,
    val instrument: Instrument,
    val entryTime: LocalDateTime,
    val exitTime: LocalDateTime,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Int,
    val pnl: Double,
    val pnlPercentage: Double
)

data class TradeFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minPnL: Double? = null,
    val maxPnL: Double? = null
)

data class TradeStats(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val totalPnL: Double = 0.0,
    val averagePnL: Double = 0.0
)
```

**Features:**
- Paginated trade list
- Date range filtering
- Profit/Loss filtering
- Trade statistics summary
- Export option
- Trade details view

---

### 4️⃣ **Risk Management Screen** (PRIORITY 4)
**Why Fourth:** Displays risk metrics and portfolio stats

**Implementation Steps:**
```
RiskScreen/
├── RiskUiState.kt              (Create)
├── RiskViewModel.kt             (Create)
├── RiskScreen.kt                (Update)
└── RiskPreviewProvider.kt       (Create)
```

**Data Model:**
```kotlin
data class RiskUiState(
    val portfolioValue: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val dailyDrawdown: Double = 0.0,
    val riskReward: Double = 0.0,
    val exposurePercentage: Double = 0.0,
    val marginUsed: Double = 0.0,
    val marginAvailable: Double = 0.0,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)
```

**Features:**
- Portfolio value display
- Drawdown metrics
- Risk/Reward ratio
- Margin utilization
- Exposure breakdown
- Visual indicators (gauges, charts)
- Alert threshold settings

---

### 5️⃣ **Live Logs Screen** (PRIORITY 5)
**Why Fifth:** Real-time data display, doesn't block other screens

**Implementation Steps:**
```
LiveLogsScreen/
├── LiveLogsUiState.kt          (Create)
├── LiveLogsViewModel.kt         (Create)
├── LiveLogsScreen.kt            (Update)
└── LiveLogsPreviewProvider.kt   (Create)
```

**Data Model:**
```kotlin
data class LiveLogsUiState(
    val logs: List<LogEntry> = emptyList(),
    val filters: LogFilter = LogFilter(),
    val autoScroll: Boolean = true,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)

data class LogEntry(
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val message: String,
    val category: String
)

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR, CRITICAL
}

data class LogFilter(
    val levels: Set<LogLevel> = LogLevel.values().toSet(),
    val category: String? = null,
    val searchText: String = ""
)
```

**Features:**
- Real-time log stream
- Log level filtering (DEBUG, INFO, WARNING, ERROR)
- Category filtering
- Search functionality
- Auto-scroll option
- Clear logs button
- Export logs

---

### 6️⃣ **More Screen** (PRIORITY 6)
**Why Last:** Navigation/Settings, lowest dependency

**Implementation Steps:**
```
MoreScreen/
├── MoreUiState.kt              (Create)
├── MoreViewModel.kt             (Create)
├── MoreScreen.kt                (Update)
└── MorePreviewProvider.kt       (Create)
```

**Features:**
- Settings menu
- Account information
- Trading preferences
- Notification settings
- Help & Support
- About & Feedback
- Logout option

---

## 🧩 Reusable Components to Create

### 1. **Dialog Templates**
```
components/dialogs/
├── ConfirmationDialog.kt
├── InfoDialog.kt
├── LoadingDialog.kt
├── ErrorDialog.kt
└── SuccessAnimation.kt
```

**ConfirmationDialog Example:**
```kotlin
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    tradingMode: TradingMode = TradingMode.LIVE,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isVisible: Boolean = true
)
```

### 2. **Timer Component**
```kotlin
@Composable
fun TimerComponent(
    durationMinutes: Int,
    onComplete: () -> Unit = {},
    isRunning: Boolean = false,
    tradingMode: TradingMode = TradingMode.LIVE
)
```

### 3. **Date Picker Component**
```kotlin
@Composable
fun DateRangePickerComponent(
    onDateRangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    tradingMode: TradingMode = TradingMode.LIVE,
    allowFutureDates: Boolean = false
)
```

---

## 📋 Screen Checklist Template

For each screen, ensure:
```
Screen: ____________

UI State:
  [ ] Created UiState data class
  [ ] Added all necessary properties
  [ ] Includes loading state
  [ ] Includes error state

ViewModel:
  [ ] Created ViewModel class
  [ ] Proper Hilt injection
  [ ] StateFlow for UI state
  [ ] SharedFlow for events
  [ ] Business logic methods

Composable:
  [ ] Main composable created
  [ ] Content composable created
  [ ] Loading state handled
  [ ] Error state handled
  [ ] Success state implemented
  [ ] All interactive elements functional

Preview Provider:
  [ ] Created PreviewProvider object
  [ ] Sample data functions
  [ ] Multiple preview states
  [ ] Live Mode theme only

Preview Composables:
  [ ] Success state preview
  [ ] Loading state preview
  [ ] Error state preview
  [ ] Edge case previews (if applicable)

Testing:
  [ ] Build successful
  [ ] No lint warnings
  [ ] Detekt passing
  [ ] Previews rendering correctly
```

---

## 🎨 Component Styling Standards

All screens must follow:
- **Theme:** Live Mode (dark) as primary, Paper Mode available
- **Cards:** Use `OrbCard` component
- **Buttons:** Material 3 buttons with appropriate colors
- **Text:** Typography hierarchy from Material 3
- **Spacing:** 4dp, 8dp, 12dp, 16dp, 24dp
- **Icons:** Material Icons only
- **Colors:** 
  - Primary: Material Blue
  - Success: Green (#4CAF50)
  - Error: Red (#F44336)
  - Warning: Amber (#FFC107)

---

## ✅ Definition of Done

A screen is **COMPLETE** when:
1. ✅ UI State class created with all necessary data
2. ✅ ViewModel created with proper logic
3. ✅ Composable implemented with loading/error/success states
4. ✅ PreviewProvider with mock data created
5. ✅ 4+ preview states showing different scenarios
6. ✅ Live Mode theme applied
7. ✅ Build successful with no warnings
8. ✅ Lint checks passing
9. ✅ Detekt checks passing
10. ✅ Previews render correctly in Android Studio
11. ✅ Follows PROJECT_STANDARDS.md conventions

---

## 📊 Progress Tracking

### Phase 1: ✅ DONE (Dashboard)
- [x] DashboardScreen
- [x] DashboardUiState
- [x] DashboardViewModel
- [x] DashboardPreviewProvider

### Phase 2: 🔄 IN PROGRESS (Screens 1-2)
- [ ] Strategy Screen
- [ ] Positions Screen
- [ ] TimerComponent
- [ ] DatePickerComponent
- [ ] Dialog Templates

### Phase 3: ⏳ PENDING (Screens 3-6)
- [ ] Trade History Screen
- [ ] Risk Screen
- [ ] Live Logs Screen
- [ ] More Screen

### Phase 4: ⏳ PENDING (Validation)
- [ ] All screens with previews
- [ ] All components created
- [ ] Build passing
- [ ] Code quality checks passing

### Phase 5: ⏳ READY (Backend Integration)
- [ ] Backend API models
- [ ] Repository integration
- [ ] Network calls
- [ ] Real data integration

---

## 📝 Git Workflow for Implementation

For each screen:

```bash
# Create feature branch
git checkout -b feature/screen-name-implementation

# Make changes
# - Create UiState
# - Create ViewModel  
# - Update Screen
# - Create PreviewProvider

# Test locally
./gradlew clean build
./gradlew detekt
./gradlew lint

# Commit
git add .
git commit -m "feat: complete ScreenName with UI state and preview"

# Push and create PR
git push origin feature/screen-name-implementation
```

---

## 🚀 Estimated Timeline

| Screen | Effort | Timeline |
|--------|--------|----------|
| Strategy | High | 1-2 days |
| Positions | Medium | 1 day |
| Trade History | Medium | 1 day |
| Risk | Medium | 1 day |
| Live Logs | Low | 0.5 day |
| More | Low | 0.5 day |
| Dialog Components | Medium | 1 day |
| **Total** | **Varies** | **5-6 days** |

---

**Next Action:** Start with Strategy Screen implementation → Create StrategyUiState.kt
