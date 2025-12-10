# 📋 MyAlgoTradeApp - Project Standards & Guidelines

**Last Updated:** December 10, 2025  
**Version:** 1.0

---

## 📖 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Code Standards](#code-standards)
3. [Package Structure](#package-structure)
4. [Naming Conventions](#naming-conventions)
5. [Development Workflow](#development-workflow)
6. [Quality Assurance](#quality-assurance)
7. [Git Workflow](#git-workflow)
8. [Performance Guidelines](#performance-guidelines)

---

## Architecture Overview

### Pattern: MVVM + Repository + Hilt DI

```
Data Layer (Repository Pattern)
    ↓
Domain Layer (Use Cases - Future)
    ↓
Presentation Layer (MVVM + Compose)
```

### Key Technologies

- **UI Framework**: Jetpack Compose
- **State Management**: StateFlow & Flow
- **Dependency Injection**: Hilt
- **Architecture**: MVVM
- **Database**: Room (Future)
- **Networking**: Retrofit (Future)
- **Async**: Kotlin Coroutines

---

## Code Standards

### Kotlin Style Guide

**1. Naming Conventions**

```kotlin
// Classes, Interfaces (PascalCase)
class TradingViewModel
interface TradingRepository
data class Position

// Functions & Variables (camelCase)
fun startStrategy()
fun calculatePnL()
val currentPrice: Double

// Constants (CONSTANT_CASE)
companion object {
    const val MARKET_OPEN = "09:15"
    const val MAX_POSITIONS = 10
}

// Private members (_prefixed)
private val _uiEvent = MutableStateFlow<UiEvent>()
```

**2. Line Length**

- Maximum 120 characters
- Break long lines logically
- Indent continuation by 4 spaces

**3. Spacing & Formatting**

```kotlin
// One empty line between class members
class MyClass {
    val property1: String = ""
    
    val property2: Int = 0
    
    fun method1() { }
    
    fun method2() { }
}

// No blank lines between property and method
companion object {
    const val CONSTANT = ""
    
    fun staticMethod() { }
}
```

**4. Comments**

- Use `//` for line comments
- Use `/** */` for documentation
- Comment WHY, not WHAT
- Keep comments up-to-date

```kotlin
/**
 * Calculates unrealized P&L for a position
 * @param position Trading position
 * @return Unrealized profit/loss in currency units
 */
fun calculateUnrealizedPnL(position: Position): Double {
    // Account for fees in calculation
    return (position.currentPrice - position.entryPrice) * position.quantity
}
```

**5. Null Safety**

```kotlin
// Prefer non-nullable types
val name: String = "value"

// Use nullable only when necessary
val nickname: String? = null

// Use safe calls & elvis operator
val length = name?.length ?: 0

// Use scope functions for null checks
position?.let {
    closePosition(it)
}
```

---

## Package Structure

```
com.trading.orb/
├── MainActivity.kt
├── MyAlgoTradeApp.kt
│
├── data/
│   ├── model/
│   │   └── Models.kt (All data classes)
│   ├── remote/
│   │   ├── TradingApi.kt (Retrofit service - Future)
│   │   └── RemoteDataSource.kt (Future)
│   ├── local/
│   │   ├── TradingDao.kt (Room DAO - Future)
│   │   └── LocalDataSource.kt (Future)
│   └── repository/
│       ├── TradingRepository.kt (Interface)
│       ├── TradingRepositoryImpl.kt (Implementation)
│       └── RepositoryModule.kt (DI - Future)
│
├── di/
│   ├── AppModule.kt (Hilt modules)
│   └── NetworkModule.kt (Network DI - Future)
│
├── ui/
│   ├── MainActivity.kt
│   ├── MainScreen.kt (Navigation)
│   ├── SampleData.kt (Mock data)
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   │
│   ├── navigation/
│   │   ├── Screen.kt (Route definitions)
│   │   └── BottomNavItem.kt (Nav items)
│   │
│   ├── components/
│   │   ├── CommonComponents.kt (Reusable UI)
│   │   ├── DialogComponents.kt (5 dialog types)
│   │   ├── TimerComponents.kt (6 timer types)
│   │   ├── DatePickerComponents.kt (4 date types)
│   │   └── TopBar.kt (App bar)
│   │
│   ├── screens/
│   │   ├── DashboardScreen.kt
│   │   ├── PositionsScreen.kt
│   │   ├── TradeHistoryScreen.kt
│   │   ├── StrategyConfigScreen.kt
│   │   ├── RiskScreen.kt
│   │   ├── LiveLogsScreen.kt
│   │   └── MoreScreen.kt
│   │
│   └── viewmodel/
│       └── TradingViewModel.kt
│
└── util/
    ├── TimeFormatter.kt (Future)
    ├── PnLCalculator.kt (Future)
    └── Validators.kt (Future)
```

---

## Naming Conventions

### File Names

```
// Composables
DashboardScreen.kt
PositionsScreen.kt
CommonComponents.kt
DialogComponents.kt

// ViewModels
TradingViewModel.kt

// Models/Data Classes
Models.kt
ApiResponse.kt

// Repositories
TradingRepository.kt
TradingRepositoryImpl.kt

// Utils
TimeFormatter.kt
PnLCalculator.kt
```

### Composable Functions

```kotlin
// Screen Composables (PascalCase + "Screen")
@Composable
fun DashboardScreen(viewModel: TradingViewModel)

// Component Composables (PascalCase + "Composable" or descriptive)
@Composable
fun ConfirmationDialog(...)

@Composable
fun StatCard(...)

@Composable
fun TimerDisplay(...)

// Private Helper Composables (_prefixed)
@Composable
private fun _PositionItem(...)
```

### Variables & Properties

```kotlin
// State variables (meaningful names)
val strategyStatus: StateFlow<StrategyStatus>
val currentPnL: Double
val tradingMode: TradingMode

// Event variables (_Event suffix for single events)
val uiEvent: SharedFlow<UiEvent>

// Mutable local state (remember)
var isLoading by remember { mutableStateOf(false) }
var selectedDate by remember { mutableStateOf(LocalDate.now()) }
```

---

## Development Workflow

### 1. Create Feature Branch

```bash
# Feature branches
git checkout -b feature/add-position-alerts

# Bugfix branches
git checkout -b bugfix/fix-pnl-calculation

# Refactor branches
git checkout -b refactor/improve-state-management
```

### 2. Commit Messages

```
# Format: <type>(<scope>): <subject>

# Types: feat, fix, refactor, docs, style, test, chore, perf
# Scope: screens, components, viewmodel, repository, etc.
# Subject: imperative, lowercase, no period

feat(screens): add trade filter by date range
fix(components): resolve timer display bug in 24h format
refactor(repository): improve error handling in API calls
docs(README): update setup instructions
chore(deps): update Compose BOM to 2025.12.00
```

### 3. Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactoring
- [ ] Documentation update

## Testing
- [ ] Unit tests added
- [ ] Manual testing completed
- [ ] No breaking changes

## Checklist
- [ ] Code follows project standards
- [ ] No lint warnings
- [ ] Comments added for complex logic
- [ ] Git history is clean
```

### 4. Code Review Checklist

- ✅ Follows naming conventions
- ✅ Code is readable and maintainable
- ✅ No dead code or unused imports
- ✅ Proper error handling
- ✅ No hardcoded values
- ✅ Comments explain WHY, not WHAT
- ✅ Performance considerations addressed
- ✅ Security best practices followed

---

## Quality Assurance

### 1. Build Verification

```bash
# Full build (includes tests)
./gradlew build

# Build without tests (faster)
./gradlew build -x test

# Build and lint
./gradlew build lint
```

### 2. Lint Standards

**Current Status:**
- ✅ 0 errors
- ⚠️ 104 non-critical warnings (acceptable)

**Before committing:**
```bash
./gradlew lint
```

**Addressing warnings:**
- DefaultLocale → Add `Locale.US` to format strings
- Update dependencies → Consider in next sprint
- KaptUsage → Migrate to KSP for performance (future)

### 3. Code Coverage Goals

| Category | Target |
|----------|--------|
| ViewModels | 80%+ |
| Repositories | 75%+ |
| Utilities | 85%+ |
| Composables | 60%+ (visual components) |

### 4. Performance Standards

**Composition Performance**
- Recompositions < 100ms for user interactions
- Avoid recomposing entire screens
- Use proper scoping for state

**Memory**
- No memory leaks in Coroutines
- Proper cleanup in LaunchedEffect
- Resource disposal in onDispose

**Bundle Size**
- Target: < 25 MB APK
- Monitor dependency sizes
- Use ProGuard/R8 for release builds

---

## Git Workflow

### 1. Local Development

```bash
# Create feature branch
git checkout -b feature/your-feature

# Make changes and commit regularly
git add .
git commit -m "feat: your commit message"

# Before pushing, sync with main
git fetch origin
git rebase origin/main

# Push to remote
git push -u origin feature/your-feature
```

### 2. Branch Protection Rules (Recommended)

Configure on GitHub:
- Require pull request reviews (minimum 1)
- Require status checks to pass
- Require branches to be up-to-date
- Dismiss stale reviews when new commits pushed

### 3. Merge Strategy

- Use **Squash and merge** for feature branches
- Keep commit history clean
- Write meaningful merge commit messages

---

## Performance Guidelines

### Compose Best Practices

**1. Recomposition Optimization**

```kotlin
// ✅ GOOD: Pass only what's needed
@Composable
fun PositionItem(
    position: Position,
    onClose: (String) -> Unit
) { }

// ❌ BAD: Passing entire ViewModel
@Composable
fun PositionItem(viewModel: TradingViewModel) { }
```

**2. State Management**

```kotlin
// ✅ GOOD: Hoist state to minimal scope
@Composable
fun DashboardScreen(viewModel: TradingViewModel) {
    val appState by viewModel.appState.collectAsState()
    
    Column {
        StatusSection(appState)
        ControlsSection(appState)
    }
}

// ❌ BAD: Create state at wrong level
@Composable
fun Dashboard() {
    val appState by remember { mutableStateOf(...) }
}
```

**3. Resource Management**

```kotlin
// ✅ GOOD: Proper cleanup
LaunchedEffect(Unit) {
    val job = launch {
        // Long running task
    }
    onDispose {
        job.cancel()
    }
}

// ❌ BAD: No cleanup
val job = GlobalScope.launch { }
```

### Network Performance

- Use proper pagination for lists
- Implement request caching
- Compress API responses
- Handle timeout gracefully

---

## Security Guidelines

### 1. Secrets Management

```kotlin
// ✅ GOOD: Use BuildConfig (injected at build time)
val apiKey = BuildConfig.API_KEY

// ❌ BAD: Hardcoded strings
val apiKey = "secret123"

// ❌ BAD: In source control
// api_key=secret123
```

### 2. Data Protection

```kotlin
// ✅ Encrypt sensitive data at rest
// ✅ Use HTTPS for all API calls
// ✅ Validate all user inputs
// ✅ Use SSL pinning for critical endpoints
```

### 3. Proguard/R8 Rules

```
# Already configured in build.gradle.kts
# Ensure release builds use proper obfuscation
```

---

## Testing Standards

### Unit Tests

```kotlin
class TradingViewModelTest {
    private lateinit var viewModel: TradingViewModel
    private lateinit var repository: FakeTradingRepository

    @Before
    fun setup() {
        repository = FakeTradingRepository()
        viewModel = TradingViewModel(repository)
    }

    @Test
    fun startStrategy_succeeds() = runTest {
        // Arrange
        val expectedStatus = StrategyStatus.ACTIVE

        // Act
        viewModel.toggleStrategy()

        // Assert
        assertEquals(expectedStatus, viewModel.appState.value.strategyStatus)
    }
}
```

### Test Naming Convention

```
<Function>_<Condition>_<Expected Result>

Example:
calculatePnL_WithProfit_ReturnsPositiveValue
closePosition_WhenInvalid_ThrowsException
filterTrades_WithDateRange_ReturnsFiltered
```

---

## Documentation Standards

### README

- ✅ Project description
- ✅ Setup instructions
- ✅ Architecture overview
- ✅ Build commands
- ✅ Contributing guidelines

### Code Documentation

- ✅ Public API documentation (KDoc)
- ✅ Complex logic explanation
- ✅ TODO comments for future work

```kotlin
/**
 * Calculates the P&L for a closed position
 *
 * Formula: (Exit Price - Entry Price) × Quantity - Fees
 *
 * @param trade The completed trade
 * @return Realized P&L in currency units
 * @throws IllegalArgumentException if trade is incomplete
 */
fun calculateRealizedPnL(trade: Trade): Double { }
```

---

## Dependencies Management

### Current Stack

```toml
# Core
androidx-core = "1.12.0"
androidx-lifecycle = "2.6.2"

# Compose
androidx-compose-bom = "2025.12.00"
compose-ui = "1.x"
compose-material3 = "1.x"

# DI
hilt = "2.48"

# Async
kotlinx-coroutines = "1.7.x"

# Logging
timber = "5.x"
```

### Adding Dependencies

```bash
# Add to libs.versions.toml
[versions]
myDependency = "1.0.0"

[libraries]
my-dependency = { group = "com.example", name = "library", version.ref = "myDependency" }

# Use in build.gradle.kts
implementation(libs.my.dependency)
```

---

## Deployment Checklist

Before release:

- [ ] All tests passing
- [ ] Build successful with no errors
- [ ] Lint warnings < 10
- [ ] ProGuard/R8 configured for release
- [ ] Version updated in build.gradle.kts
- [ ] Changelog updated
- [ ] Code reviewed
- [ ] Security audit completed
- [ ] Performance profiled
- [ ] Release notes prepared

---

## Resources & References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Material Design 3](https://m3.material.io/)
- [Hilt Documentation](https://dagger.dev/hilt/)

---

**Last Updated:** December 10, 2025  
**Maintained by:** Development Team  
**Questions?** Open an issue or start a discussion!
