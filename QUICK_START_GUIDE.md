# Quick Start Guide - Screen-Based Architecture

## For Developers: How to Add a New Screen

### Step 1: Create the Screen Folder
```bash
mkdir -p app/src/main/java/com/trading/orb/ui/screens/{screen_name}
```

### Step 2: Create the ViewModel File
**File**: `{ScreenName}ViewModel.kt`
```kotlin
package com.trading.orb.ui.screens.{screen_name}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class {ScreenName}ViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow({ScreenName}UiState())
    val uiState: StateFlow<{ScreenName}UiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<{ScreenName}UiEvent>()
    val uiEvent: SharedFlow<{ScreenName}UiEvent> = _uiEvent.asSharedFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = LoadingState(isLoading = true)) }
            try {
                // Load data from repository
                _uiState.update { it.copy(loading = LoadingState(isLoading = false)) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Unknown error",
                            isRetryable = true
                        )
                    )
                }
            }
        }
    }
}
```

### Step 3: Create the UiState File
**File**: `{ScreenName}UiState.kt`
```kotlin
package com.trading.orb.ui.screens.{screen_name}

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState

data class {ScreenName}UiState(
    val data: MyData = MyData(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false
)

sealed class {ScreenName}UiEvent {
    data class ShowError(val message: String) : {ScreenName}UiEvent()
    data class ShowSuccess(val message: String) : {ScreenName}UiEvent()
}
```

### Step 4: Update the Screen Composable
**File**: `{ScreenName}Screen.kt` (move existing file here)

Update package declaration:
```kotlin
package com.trading.orb.ui.screens.{screen_name}
```

Update the Composable function signature:
```kotlin
@Composable
fun {ScreenName}Screen(
    uiState: {ScreenName}UiState = {ScreenName}UiState(),
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    {ScreenName}ScreenContent(
        uiState = uiState,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun {ScreenName}ScreenContent(
    uiState: {ScreenName}UiState,
    onAction: () -> Unit,
    modifier: Modifier
) {
    // UI Implementation
}
```

### Step 5: Update Navigation
In `ui/navigation/Screen.kt`, add:
```kotlin
sealed class Screen(val route: String) {
    // ... existing routes ...
    data object {ScreenName} : Screen("{screen_name}")
}
```

In `ui/navigation/BottomNavItem.kt`, add if it's a bottom nav item:
```kotlin
BottomNavItem(
    label = "{Screen Name}",
    icon = Icons.Default.{IconName},
    route = Screen.{ScreenName}.route
)
```

### Step 6: Update MainScreen.kt
In `ui/MainScreen.kt`, add import:
```kotlin
import com.trading.orb.ui.screens.{screen_name}.{ScreenName}Screen
```

Add navigation route in NavHost:
```kotlin
composable(Screen.{ScreenName}.route) {
    {ScreenName}Screen(
        uiState = {ScreenName}UiState(),
        onAction = { /* handle action */ }
    )
}
```

## Common Patterns

### Loading State
```kotlin
when {
    uiState.loading.isLoading -> {
        LoadingScreen(message = uiState.loading.loadingMessage)
    }
    uiState.error.hasError -> {
        ErrorScreen(
            message = uiState.error.errorMessage,
            isRetryable = uiState.error.isRetryable,
            onRetry = { viewModel.retry() }
        )
    }
    else -> {
        // Normal content
    }
}
```

### Handling ViewModel Events
```kotlin
@Composable
fun {ScreenName}Screen(
    viewModel: {ScreenName}ViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is {ScreenName}UiEvent.ShowError -> {
                    // Show error toast
                }
                is {ScreenName}UiEvent.ShowSuccess -> {
                    // Show success toast
                }
                is {ScreenName}UiEvent.Navigate -> {
                    onNavigate(event.screen)
                }
            }
        }
    }
}
```

### State Updates
```kotlin
fun updateData(newData: MyData) {
    viewModelScope.launch {
        _uiState.update { it.copy(data = newData) }
    }
}
```

### Error Handling
```kotlin
repository.fetchData().onSuccess { data ->
    _uiState.update { it.copy(data = data) }
}.onFailure { error ->
    _uiEvent.emit({ScreenName}UiEvent.ShowError(error.message ?: "Unknown error"))
    _uiState.update { 
        it.copy(
            error = ErrorState(
                hasError = true,
                errorMessage = error.message ?: "Unknown error",
                isRetryable = true,
                throwable = error
            )
        )
    }
}
```

## Testing

### Create Preview Provider
```kotlin
object {ScreenName}PreviewProvider {
    fun sampleData(): MyData = MyData(/* ... */)
    
    fun sampleUiState(): {ScreenName}UiState = {ScreenName}UiState(
        data = sampleData(),
        loading = LoadingState(),
        error = ErrorState()
    )
}
```

### Create Previews
```kotlin
@Preview
@Composable
fun {ScreenName}ScreenPreview() {
    {ScreenName}ScreenContent(
        uiState = {ScreenName}PreviewProvider.sampleUiState(),
        onAction = {}
    )
}

@Preview
@Composable
fun {ScreenName}ScreenLoadingPreview() {
    {ScreenName}ScreenContent(
        uiState = {ScreenName}PreviewProvider.sampleUiState().copy(
            loading = LoadingState(isLoading = true)
        ),
        onAction = {}
    )
}

@Preview
@Composable
fun {ScreenName}ScreenErrorPreview() {
    {ScreenName}ScreenContent(
        uiState = {ScreenName}PreviewProvider.sampleUiState().copy(
            error = ErrorState(
                hasError = true,
                errorMessage = "Failed to load data",
                isRetryable = true
            )
        ),
        onAction = {}
    )
}
```

## Checklist for New Screen

- [ ] Created `screens/{screen_name}/` folder
- [ ] Created `{ScreenName}ViewModel.kt`
- [ ] Created `{ScreenName}UiState.kt`
- [ ] Updated `{ScreenName}Screen.kt` package and function signature
- [ ] Added route to `Screen.kt`
- [ ] Added bottom nav item (if applicable) to `BottomNavItem.kt`
- [ ] Updated `MainScreen.kt` with import and navigation route
- [ ] Created preview provider and preview composables
- [ ] Build and test in Android Studio

## Troubleshooting

### Package Not Found
- Verify the import path matches the folder structure exactly
- Check for typos in package names
- Use IDE auto-import (Ctrl+Alt+O on Windows/Linux, Cmd+Alt+O on Mac)

### Unresolved Reference
- Ensure all dependencies are imported
- Check that classes are defined in the correct files
- Verify class names match usage

### Type Mismatch
- Check that parameter types match function signatures
- Use type aliases for conflicting names:
  ```kotlin
  import com.trading.orb.ui.screens.dashboard.AppState as DashboardAppState
  ```

### Build Fails
- Run `./gradlew clean build` to ensure all files are processed
- Check for syntax errors in new files
- Verify Hilt annotations are correct

## Performance Tips

- Use `collectAsStateWithLifecycle()` instead of `collectAsState()`
- Avoid recomposition by memoizing expensive operations
- Use `remember { }` for variables that don't change
- Load data only in `init` or with explicit user action

## Documentation

- See **SCREEN_STRUCTURE.md** for detailed architecture guide
- See **ARCHITECTURE_REFACTORING.md** for refactoring details
- See **Dashboard** screen (`screens/dashboard/`) as an example implementation

---

Need help? Check the Dashboard screen implementation for a complete, production-ready example!
