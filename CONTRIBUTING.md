# Contributing to MyAlgoTradeApp 🤝

Thank you for your interest in contributing to MyAlgoTradeApp! This document provides guidelines and instructions for contributing to the project.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Workflow](#development-workflow)
4. [Coding Standards](#coding-standards)
5. [Testing](#testing)
6. [Pull Request Process](#pull-request-process)
7. [Issue Reporting](#issue-reporting)
8. [Questions?](#questions)

---

## Code of Conduct

We are committed to providing a welcoming and inclusive environment. All contributors must:

- ✅ Be respectful and professional
- ✅ Welcome diverse perspectives
- ✅ Be supportive of other developers
- ✅ Report inappropriate behavior constructively

---

## Getting Started

### Prerequisites

- Android Studio 2023.1+
- Java 17+
- Gradle 8.5+
- Git

### Setup Development Environment

1. **Fork the repository** on GitHub

2. **Clone your fork**

```bash
git clone https://github.com/YOUR_USERNAME/MyAlgoTradingApp.git
cd MyAlgoTradingApp
```

3. **Add upstream remote**

```bash
git remote add upstream https://github.com/jsiva001/MyAlgoTradingApp.git
```

4. **Create local branch**

```bash
git checkout -b feature/your-feature-name
```

5. **Build and test**

```bash
./gradlew build
./gradlew test
```

---

## Development Workflow

### 1. Choose an Issue

- Check [Issues](https://github.com/jsiva001/MyAlgoTradingApp/issues)
- Look for `good-first-issue` or `help-wanted` labels
- Comment on the issue to claim it
- Discuss approach with maintainers

### 2. Create Feature Branch

```bash
# Sync with upstream
git fetch upstream
git checkout -b feature/your-feature-name upstream/main

# Branch naming convention:
# feature/add-new-screen
# bugfix/fix-pnl-calculation
# refactor/improve-error-handling
# docs/update-readme
```

### 3. Make Changes

**Follow the project structure:**

```
com.trading.orb/
├── data/                 (Models, Repository, API)
├── di/                   (Dependency Injection)
├── ui/
│   ├── components/       (Reusable Components)
│   ├── screens/          (Screen Composables)
│   ├── theme/            (Colors, Typography)
│   ├── navigation/       (Routes)
│   └── viewmodel/        (ViewModels)
└── util/                 (Utilities)
```

### 4. Commit Changes

**Use meaningful commit messages:**

```bash
# Format: <type>(<scope>): <subject>

# Good examples:
git commit -m "feat(screens): add position alerts screen"
git commit -m "fix(components): resolve timer display bug"
git commit -m "refactor(repository): improve error handling"

# Commit types:
# feat     - New feature
# fix      - Bug fix
# refactor - Code restructuring
# docs     - Documentation update
# style    - Formatting changes
# test     - Adding/updating tests
# chore    - Dependency updates
# perf     - Performance improvements
```

### 5. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 6. Create Pull Request

- Go to GitHub
- Click "New Pull Request"
- Select your branch
- Fill in the PR template
- Request review

---

## Coding Standards

### Follow Kotlin Style Guide

```kotlin
// ✅ GOOD
class TradingViewModel(
    private val repository: TradingRepository
) : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    fun startStrategy() {
        viewModelScope.launch {
            val result = repository.startStrategy()
            result.onSuccess { /* handle */ }
            result.onFailure { /* handle */ }
        }
    }
}

// ❌ AVOID
class ViewModel(var repo:TradingRepository){
    var state = mutableStateOf(AppState());
    fun start() {
        GlobalScope.launch { repo.startStrategy() }
    }
}
```

### Naming Conventions

```kotlin
// Classes and Interfaces
class TradingViewModel
interface TradingRepository
data class Position

// Functions
fun startStrategy()
fun calculatePnL()

// Constants
companion object {
    const val MAX_POSITIONS = 10
}

// Private members
private val _state = MutableStateFlow<State>()
```

### Code Organization

```kotlin
// Order in class:
// 1. Companion object with constants
// 2. Constructor parameters
// 3. Properties
// 4. Public methods
// 5. Private methods
// 6. Helper functions

class Example {
    companion object {
        const val TAG = "Example"
    }

    private val _state = MutableStateFlow<State>()
    val state: StateFlow<State> = _state.asStateFlow()

    fun publicMethod() { }

    private fun privateMethod() { }
}
```

### Documentation

```kotlin
/**
 * Calculates unrealized P&L for a position
 *
 * @param position Trading position with current price
 * @return Unrealized profit/loss value
 * @throws IllegalArgumentException if position is invalid
 */
fun calculateUnrealizedPnL(position: Position): Double {
    // Validate position
    require(position.quantity > 0) { "Quantity must be positive" }

    return (position.currentPrice - position.entryPrice) * position.quantity
}
```

---

## Testing

### Write Tests for New Code

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
    fun startStrategy_updatesStatusToActive() = runTest {
        // Arrange
        val expectedStatus = StrategyStatus.ACTIVE

        // Act
        viewModel.toggleStrategy()

        // Assert
        assertEquals(
            expectedStatus,
            viewModel.appState.value.strategyStatus
        )
    }

    @Test
    fun calculatePnL_withProfit_returnsPositiveValue() {
        // Arrange
        val position = Position(
            entryPrice = 100.0,
            currentPrice = 110.0,
            quantity = 10
        )

        // Act
        val pnl = calculatePnL(position)

        // Assert
        assertEquals(100.0, pnl)
    }
}
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (Android device)
./gradlew connectedAndroidTest

# Specific test
./gradlew test --tests "com.trading.orb.ui.viewmodel.*"
```

### Test Coverage

Aim for:
- ViewModels: 80%+
- Repositories: 75%+
- Utilities: 85%+

---

## Pull Request Process

### Before Submitting

- [ ] Synced with upstream main branch
- [ ] Code follows project standards
- [ ] No lint warnings introduced
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] Documentation updated
- [ ] No hardcoded values
- [ ] Commit history is clean

### PR Template

```markdown
## Description
Brief description of what this PR does

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactoring
- [ ] Documentation update
- [ ] Performance improvement

## Related Issue
Fixes #(issue number)

## Changes Made
- Added feature X
- Fixed bug Y
- Updated documentation

## Testing
- [ ] Unit tests added
- [ ] Manual testing completed
- [ ] All tests passing

## Screenshots (if applicable)
(Add screenshots for UI changes)

## Checklist
- [ ] Code follows project standards
- [ ] No new lint warnings
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] No breaking changes
- [ ] Reviewed own code
```

### Review Process

1. **Automated checks** run (build, tests, lint)
2. **Maintainers review** code
3. **Address feedback** and update PR
4. **Approval** when ready
5. **Merge** using squash & merge

---

## Issue Reporting

### Before Creating an Issue

- ✅ Search existing issues
- ✅ Check documentation
- ✅ Verify it's not already fixed
- ✅ Gather relevant information

### Issue Template

```markdown
## Description
Clear description of the issue

## Reproduction Steps
1. Step 1
2. Step 2
3. Expected result vs Actual result

## Environment
- Android Studio version: X.X
- Gradle version: X.X
- Android SDK version: XX
- Device/Emulator: X

## Logs/Screenshots
(Add relevant logs or screenshots)

## Additional Context
Any other information that might help
```

---

## Common Contribution Scenarios

### Adding a New Screen

1. **Create Screen Composable**

```kotlin
// ui/screens/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopBar(title = "New Feature", onBack = onNavigateBack)
        // Screen content
    }
}
```

2. **Add Route**

```kotlin
// ui/navigation/Screen.kt
sealed class Screen(val route: String) {
    object NewFeature : Screen("new_feature")
}
```

3. **Update Navigation**

```kotlin
// ui/MainScreen.kt
NavHost(...) {
    composable(Screen.NewFeature.route) {
        NewFeatureScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
```

4. **Add Tests**

```kotlin
// Add unit tests for ViewModel logic
```

### Adding a New Component

1. **Create Component**

```kotlin
// ui/components/NewComponent.kt
@Composable
fun NewComponent(
    data: String,
    modifier: Modifier = Modifier,
    onAction: (Action) -> Unit = {}
) {
    // Component implementation
}
```

2. **Document Usage**

```kotlin
/**
 * Display new feature component
 *
 * @param data Data to display
 * @param onAction Callback for user actions
 */
```

3. **Add to CommonComponents** or create specialized file

### Fixing a Bug

1. **Create Issue** if not exists
2. **Create Branch** with bugfix prefix
3. **Add Test** that reproduces bug
4. **Fix Bug**
5. **Verify Test** now passes
6. **Submit PR** with issue reference

---

## Code Review Checklist

When reviewing others' PRs:

- ✅ Code is readable and maintainable
- ✅ Follows project standards
- ✅ No dead code or unused imports
- ✅ Proper error handling
- ✅ Tests are adequate
- ✅ Documentation is clear
- ✅ No performance regressions
- ✅ Security best practices followed

---

## Questions?

- 📖 Check [PROJECT_STANDARDS.md](PROJECT_STANDARDS.md)
- 📋 Read [README.md](README.md)
- 💬 Open a discussion on GitHub
- 📧 Contact maintainers

---

## Thank You! 🙏

Your contributions make MyAlgoTradeApp better for everyone!

Happy coding! 🚀
