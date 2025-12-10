# Screen-Based Folder Structure Guide

## Overview
This project uses a **Screen-Based Folder Structure** for better organization and scalability. Each screen has its own dedicated folder containing all related files.

## Directory Structure

```
ui/
├── screens/
│   ├── dashboard/
│   │   ├── DashboardScreen.kt           # Composable UI
│   │   ├── DashboardViewModel.kt        # ViewModel for state management
│   │   └── DashboardUiState.kt          # UI State & Data Classes
│   │
│   ├── strategy/
│   │   ├── StrategyConfigScreen.kt      # Composable UI
│   │   ├── StrategyViewModel.kt         # ViewModel (to be created)
│   │   └── StrategyUiState.kt           # UI State (to be created)
│   │
│   ├── positions/
│   │   ├── PositionsScreen.kt           # Composable UI
│   │   ├── PositionsViewModel.kt        # ViewModel (to be created)
│   │   └── PositionsUiState.kt          # UI State (to be created)
│   │
│   ├── risk/
│   │   ├── RiskScreen.kt                # Composable UI
│   │   ├── RiskViewModel.kt             # ViewModel (to be created)
│   │   └── RiskUiState.kt               # UI State (to be created)
│   │
│   ├── tradehistory/
│   │   ├── TradeHistoryScreen.kt        # Composable UI
│   │   ├── TradeHistoryViewModel.kt     # ViewModel (to be created)
│   │   └── TradeHistoryUiState.kt       # UI State (to be created)
│   │
│   ├── liveloggers/
│   │   ├── LiveLogsScreen.kt            # Composable UI
│   │   ├── LiveLogsViewModel.kt         # ViewModel (to be created)
│   │   └── LiveLogsUiState.kt           # UI State (to be created)
│   │
│   └── more/
│       ├── MoreScreen.kt                # Composable UI
│       ├── MoreViewModel.kt             # ViewModel (to be created)
│       └── MoreUiState.kt               # UI State (to be created)
│
├── components/                          # Reusable components (Shared across screens)
│   ├── CommonComponents.kt
│   ├── DialogComponents.kt
│   ├── DatePickerComponents.kt
│   ├── TimerComponents.kt
│   └── TopBar.kt
│
├── navigation/                          # Navigation setup
│   ├── Screen.kt
│   └── BottomNavItem.kt
│
├── theme/                               # Design theme
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
├── state/                               # Shared state classes (if any)
│   ├── UiState.kt                       # Base state classes
│   ├── UiModels.kt                      # Common UI Models
│   └── UiMappers.kt                     # State mappers
│
├── viewmodel/                           # DEPRECATED - Use screen-specific ViewModels instead
│   └── TradingViewModel.kt              # Will be refactored
│
└── MainScreen.kt                        # Main app screen with navigation
```

## File Organization by Screen

### Dashboard Screen
**Path:** `ui/screens/dashboard/`

- **DashboardScreen.kt**: UI Composable that displays dashboard content
- **DashboardViewModel.kt**: Manages dashboard state and user actions
  - Collects app state from repository
  - Handles toggle strategy, toggle mode, emergency stop
  - Provides UI events
- **DashboardUiState.kt**: Data classes for dashboard UI state
  - `DashboardUiState`: Main state holder
  - `AppState`: Global app state
  - Enums: `TradingMode`, `StrategyStatus`, `ConnectionStatus`

**Pattern:**
```kotlin
// In Composable
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    // callbacks
) {
    val uiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    // Use states to build UI
}

// In ViewModel
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {
    val appState: StateFlow<AppState>
    val dashboardUiState: StateFlow<DashboardUiState>
    
    fun toggleStrategy() { /* ... */ }
    fun toggleTradingMode() { /* ... */ }
    fun emergencyStop() { /* ... */ }
}

// In UiState
data class DashboardUiState(
    val dailyStats: DailyStats,
    val orbLevels: OrbLevels?,
    val loading: LoadingState,
    val error: ErrorState,
    val isRefreshing: Boolean
)
```

## Creating a New Screen

Follow these steps when creating a new screen:

### 1. Create Screen Folder
```bash
mkdir -p app/src/main/java/com/trading/orb/ui/screens/{screen_name}
```

### 2. Create ViewModel
**File:** `{ScreenName}ViewModel.kt`
```kotlin
@HiltViewModel
class MyScreenViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MyScreenUiState())
    val uiState: StateFlow<MyScreenUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<MyScreenUiEvent>()
    val uiEvent: SharedFlow<MyScreenUiEvent> = _uiEvent.asSharedFlow()
    
    // Load data
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Update state
        }
    }
    
    // User actions
    fun onAction() {
        viewModelScope.launch {
            // Handle action
        }
    }
}
```

### 3. Create UI State
**File:** `{ScreenName}UiState.kt`
```kotlin
data class MyScreenUiState(
    val data: MyData = MyData(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false
)

sealed class MyScreenUiEvent {
    data class ShowError(val message: String) : MyScreenUiEvent()
    data class ShowSuccess(val message: String) : MyScreenUiEvent()
}
```

### 4. Create Screen Composable
**File:** `{ScreenName}Screen.kt`
```kotlin
@Composable
fun MyScreen(
    viewModel: MyScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    MyScreenContent(
        uiState = uiState,
        onAction = { viewModel.onAction() }
    )
}

@Composable
private fun MyScreenContent(
    uiState: MyScreenUiState,
    onAction: () -> Unit
) {
    // UI Implementation
}
```

## Shared Components

Located in `ui/components/`, these are reusable across all screens:

- **CommonComponents.kt**: Generic UI components (Cards, Buttons, etc.)
- **DialogComponents.kt**: Dialog templates for confirmations, alerts, etc.
- **DatePickerComponents.kt**: Reusable date picker
- **TimerComponents.kt**: Reusable timer components
- **TopBar.kt**: App bar components

## State Management Flow

```
User Interaction
       ↓
Composable calls ViewModel method
       ↓
ViewModel updates StateFlow
       ↓
Composable observes StateFlow change
       ↓
UI re-composes with new state
```

## Best Practices

1. **Keep ViewModel state**: Never hold UI state in Composables, use ViewModels
2. **Separate UI concerns**: Split large screens into smaller composables
3. **Use StateFlow**: For reactive state management
4. **Loading & Error States**: Always include in UI state
5. **Preview Providers**: Create mock data providers for Compose previews
6. **Navigation**: Use sealed classes in Screen.kt for type-safe navigation

## Migration Guide

For existing screens being migrated to this structure:

1. Move `ScreenName.kt` to `screens/{screen_name}/ScreenNameScreen.kt`
2. Create `ScreenNameViewModel.kt` with proper state management
3. Create `ScreenNameUiState.kt` with data classes
4. Update imports in MainScreen.kt and navigation files
5. Test thoroughly in Android Studio preview

## Example: Complete Dashboard Implementation

See `ui/screens/dashboard/` for a complete, production-ready example:
- ✅ UI State management
- ✅ ViewModel with repository integration
- ✅ Loading/Error states
- ✅ Multiple preview providers
- ✅ Comprehensive preview composables

## File Organization Benefits

✅ **Scalability**: Easy to add new screens
✅ **Maintainability**: All screen-related code in one place
✅ **Testability**: Easy to unit test ViewModels in isolation
✅ **Reusability**: Common components in shared folder
✅ **Clarity**: Clear separation of concerns
✅ **Collaboration**: Multiple developers can work on different screens independently

