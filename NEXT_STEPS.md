# Next Steps - Frontend Development Guide

## 🎯 Current Status Summary

✅ **Dashboard Screen:** Fully implemented with UI state, ViewModel, and 7 preview states  
✅ **Build:** Passing with no errors  
✅ **Architecture:** Solid (UI State + ViewModel pattern)  
✅ **DI:** Hilt configured and working  
✅ **Theme:** Live Mode ready  

**Frontend Completion:** 14% (1 of 7 screens)  
**Estimated Time to Full Frontend:** 5-6 days

---

## 📋 What You Have Right Now

### 1. **Working Pattern** (Use as Template)
The DashboardScreen demonstrates the exact pattern to follow for all other screens:

```
ScreenName/
├── ScreenNameScreen.kt        (Main UI Composable)
├── ScreenNameUiState.kt       (UI State data classes)
├── ScreenNameViewModel.kt     (Business logic + state management)
└── ScreenNamePreviewProvider.kt (Mock data for previews)
```

### 2. **Reusable Infrastructure**
- Theme system with Live Mode
- Material 3 integration
- Hilt dependency injection
- StateFlow and SharedFlow for reactive state
- Error/Loading/Success state patterns
- Common UI components (OrbCard, StatCard, etc.)

### 3. **Quality Checks Setup**
- **Local:** Pre-commit hooks (Lint + Detekt + Unit Tests)
- **Remote:** GitHub Actions on PR (for future)
- Build passing: ✅
- Lint passing: ✅
- Detekt passing: ✅

---

## 🚀 How to Proceed

### Step 1: Create Reusable Components (1 Day)

**Files to Create:**

#### A. Timer Component
**File:** `app/src/main/java/com/trading/orb/ui/components/TimerComponent.kt`

```kotlin
@Composable
fun TimerComponent(
    durationMinutes: Int,
    isRunning: Boolean = false,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for countdown
    var remainingSeconds by remember { 
        mutableStateOf(durationMinutes * 60) 
    }
    
    // LaunchedEffect for countdown logic
    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds == 0) {
                onComplete()
            }
        }
    }
    
    // Format and display
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge,
            color = if (remainingSeconds > 300) Success else Warning
        )
    }
}
```

#### B. Dialog Templates
**Files to Create:**
1. `ConfirmationDialog.kt`
2. `LoadingDialog.kt`
3. `ErrorDialog.kt`
4. `InfoDialog.kt`
5. `SuccessAnimation.kt`

Example template:
```kotlin
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isVisible: Boolean = true
) {
    if (!isVisible) return
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(cancelText)
            }
        }
    )
}
```

### Step 2: Complete Strategy Screen (1-2 Days)

**Files to Create:**

#### A. StrategyUiState.kt
```kotlin
package com.trading.orb.ui.screens.strategy

import com.trading.orb.data.model.Instrument
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import java.time.LocalTime

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

#### B. StrategyViewModel.kt
```kotlin
package com.trading.orb.ui.screens.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StrategyViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    private val _strategyUiState = MutableStateFlow(StrategyConfigUiState())
    val strategyUiState: StateFlow<StrategyConfigUiState> = _strategyUiState.asStateFlow()

    fun saveStrategy() {
        viewModelScope.launch {
            _strategyUiState.update { it.copy(loading = LoadingState(isLoading = true)) }
            try {
                // Call repository to save
                _strategyUiState.update { 
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    ) 
                }
            } catch (e: Exception) {
                _strategyUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to save strategy",
                            isRetryable = true
                        )
                    )
                }
            }
        }
    }

    fun updateStrategyName(name: String) {
        _strategyUiState.update { it.copy(strategyName = name) }
    }

    // Add more update functions for other fields...
}
```

#### C. Update StrategyConfigScreen.kt
- Add proper composable implementation
- Integrate with StrategyViewModel
- Add form fields with validation
- Include Timer and DatePicker components

#### D. StrategyPreviewProvider.kt
```kotlin
object StrategyPreviewProvider {
    fun sampleStrategyUiState(): StrategyConfigUiState {
        return StrategyConfigUiState(
            strategyName = "ORB Strategy",
            breakoutPercentage = 0.75,
            maxPositions = 3,
            riskPerTrade = 100.0,
            isActive = true
        )
    }
}
```

### Step 3: Repeat for Other Screens (2.5-3 Days)

Follow the same pattern for:
1. **PositionsScreen** (similar complexity)
2. **TradeHistoryScreen** (with filtering)
3. **RiskScreen** (with metrics)
4. **LiveLogsScreen** (with real-time)
5. **MoreScreen** (with settings)

Each screen needs:
- ✅ UiState class
- ✅ ViewModel class
- ✅ Screen composable
- ✅ PreviewProvider with 3-5 preview states
- ✅ Preview functions in Screen file (Live Mode only)

---

## 📝 Step-by-Step Implementation Template

For each new screen, follow this exact process:

### 1. Create UiState Class
```bash
# File: app/src/main/java/com/trading/orb/ui/screens/[screenname]/[ScreenName]UiState.kt
data class [ScreenName]UiState(
    // Your data properties here
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState()
)
```

### 2. Create ViewModel
```bash
# File: app/src/main/java/com/trading/orb/ui/screens/[screenname]/[ScreenName]ViewModel.kt
@HiltViewModel
class [ScreenName]ViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {
    // State management logic
}
```

### 3. Update Screen Composable
```bash
# File: app/src/main/java/com/trading/orb/ui/screens/[screenname]/[ScreenName]Screen.kt
@Composable
fun [ScreenName]Screen(
    uiState: [ScreenName]UiState = [ScreenName]UiState(),
    // other params
) {
    // Implementation
}
```

### 4. Create PreviewProvider
```bash
# File: app/src/main/java/com/trading/orb/ui/screens/[screenname]/[ScreenName]PreviewProvider.kt
object [ScreenName]PreviewProvider {
    fun sample[ScreenName]UiState(): [ScreenName]UiState {
        // Return mock data
    }
}
```

### 5. Add Preview Functions
```kotlin
@Preview(name = "[ScreenName] - State 1", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun [ScreenName]PreviewState1() {
    OrbTradingTheme(tradingMode = TradingMode.LIVE) {
        [ScreenName]Screen(
            uiState = [ScreenName]PreviewProvider.sample[ScreenName]UiState()
        )
    }
}
```

---

## ✅ Before Pushing Each Screen

Always run:
```bash
# Build the app
./gradlew clean build

# Run lint checks
./gradlew lint

# Run detekt checks
./gradlew detekt

# Run unit tests
./gradlew test
```

All should pass ✅

---

## 🎨 Preview Preview States to Include

For **every screen**, create previews for:

1. **Success State**
   - Normal data loaded
   - All fields populated
   - Ready for interaction

2. **Loading State**
   - Shows progress indicator
   - Appropriate loading message
   - Disabled interactions

3. **Error State - Retryable**
   - Error message displayed
   - Retry button visible
   - Can attempt recovery

4. **Error State - Non-Retryable**
   - Error message displayed
   - No retry option
   - Navigation options shown

5. **Edge Case State**
   - Empty state (no data)
   - Maximum values displayed
   - Minimum values displayed
   - Or specific to screen

---

## 📂 File Organization Checklist

After completing each screen, verify:
```
✅ ScreenName/
   ✅ ScreenNameScreen.kt (main composable + previews)
   ✅ ScreenNameUiState.kt (data classes)
   ✅ ScreenNameViewModel.kt (business logic)
   ✅ ScreenNamePreviewProvider.kt (mock data)
   ✅ All files in same directory
   ✅ Package name correct
   ✅ Imports organized
   ✅ No unused imports
```

---

## 🔄 Git Workflow for Each Screen

```bash
# Create feature branch
git checkout -b feature/[screen-name]-implementation

# Make all changes for this screen
# - Create UiState
# - Create ViewModel
# - Update Screen
# - Create PreviewProvider

# Test locally
./gradlew clean build
./gradlew detekt

# Commit
git add .
git commit -m "feat: implement [ScreenName] with UI state and previews"

# Push
git push origin feature/[screen-name]-implementation

# Create PR on GitHub
# (Will run GitHub Actions for additional checks)
```

---

## 🎯 Success Criteria

A screen is **DONE** when:
- ✅ UiState class created with all necessary fields
- ✅ ViewModel created with Hilt injection
- ✅ Screen composable handles loading/error/success states
- ✅ PreviewProvider with mock data exists
- ✅ 4+ preview states working in Android Studio
- ✅ Build passes without warnings
- ✅ Lint checks pass
- ✅ Detekt checks pass
- ✅ Follows ProjectStandards.md conventions
- ✅ No hardcoded strings in UI
- ✅ Proper Material 3 theming applied

---

## 📊 Tracking Progress

Track your progress using this checklist:

```
Dashboard       ✅✅✅✅✅ (100%)
Strategy        🔄(Timer) 🔄(UiState) 🔄(ViewModel) ⏳(Screen) ⏳(Provider)
Positions       ⏳⏳⏳⏳⏳ (0%)
TradeHistory    ⏳⏳⏳⏳⏳ (0%)
Risk            ⏳⏳⏳⏳⏳ (0%)
LiveLogs        ⏳⏳⏳⏳⏳ (0%)
More            ⏳⏳⏳⏳⏳ (0%)
Dialogs         ⏳⏳⏳⏳⏳ (0%)

Legend: ✅ = Done, 🔄 = In Progress, ⏳ = Not Started
```

---

## 🚀 After Frontend is Complete

Once all screens are done:

1. ✅ Create API data models
2. ✅ Implement Network layer (Retrofit)
3. ✅ Update Repository to call APIs
4. ✅ Replace mock data with real API calls
5. ✅ Test with backend APIs
6. ✅ Handle real-world errors and edge cases

---

## 📞 Quick Reference

**Key Files to Reference:**
- Dashboard implementation → `DashboardScreen.kt`
- UI State pattern → `DashboardUiState.kt`
- ViewModel pattern → `DashboardViewModel.kt`
- Preview pattern → `DashboardPreviewProvider.kt`

**Theme Reference:**
- Colors → `app/src/main/java/com/trading/orb/ui/theme/Color.kt`
- Typography → `app/src/main/java/com/trading/orb/ui/theme/Typography.kt`

**Standards Reference:**
- `PROJECT_STANDARDS.md` - Code conventions
- `UI_STATE_INTEGRATION_GUIDE.md` - State management pattern

---

## 💡 Pro Tips

1. **Copy & Paste Strategy**
   - Duplicate Dashboard directory as template
   - Rename all files and classes
   - Update the content
   - Much faster than starting from scratch

2. **Test Previews Frequently**
   - After each change, rebuild and preview
   - Catch issues early
   - Visual feedback keeps you on track

3. **Keep PreviewProviders Simple**
   - Use realistic mock data
   - Different scenarios = different preview states
   - Easy to extend when real API is ready

4. **Commit Frequently**
   - After each screen completion
   - Makes it easier to rollback if needed
   - Better git history

---

**You're ready to go! Start with the Timer component, then move to Strategy Screen. 🚀**
