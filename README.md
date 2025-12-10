# MyAlgoTradeApp 📈

**Algorithmic Trading Application for ORB (Opening Range Breakout) Strategy**

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-purple)]()
[![Compose](https://img.shields.io/badge/Compose-2025.12-blue)]()
[![Android](https://img.shields.io/badge/Android-API26+-green)]()
[![License](https://img.shields.io/badge/License-MIT-yellow)]()

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Development](#development)
- [Architecture](#architecture)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

MyAlgoTradeApp is a professional-grade Android application for algorithmic trading using the Opening Range Breakout (ORB) strategy. It provides real-time position monitoring, risk management, and strategy configuration with support for both paper trading (simulation) and live trading modes.

### Key Highlights

- ✅ **Production-Ready Frontend** - 7 complete screens with Material3 design
- ✅ **15 Reusable Components** - Professional dialogs, timers, and date pickers
- ✅ **MVVM Architecture** - Clean, maintainable, and testable code
- ✅ **Real-Time Updates** - StateFlow-based reactive UI
- ✅ **Mode-Aware Styling** - Paper (Grey) and Live (Blue) trading modes
- ✅ **Zero Errors** - Build successful with quality lint reports
- ✅ **Hilt Dependency Injection** - Clean dependency management

---

## Features

### 🎯 Trading Features

- **Strategy Control**
  - Start/Stop strategy with confirmation dialogs
  - Emergency stop for immediate market exit
  - Real-time strategy status monitoring

- **Position Management**
  - View active trading positions
  - Monitor entry prices, current prices, and P&L
  - Close individual positions with confirmation
  - Track stop loss and target levels

- **Trade History**
  - View completed trades with full details
  - Filter by date range (quick presets: Today, 7 Days, 30 Days, etc.)
  - Display exit reasons (Target Hit, SL Hit, Time Exit, Manual, Circuit Breaker)
  - Calculate trade duration and realized P&L

- **Strategy Configuration**
  - Select trading instruments/symbols
  - Configure ORB levels (High/Low)
  - Set breakout buffer percentages
  - Define entry/exit time windows
  - Manage target and stop loss levels

- **Risk Management**
  - Set maximum daily loss limits
  - Configure position sizing rules
  - Risk/Reward ratio display
  - Capital allocation management

### 💻 UI/UX Features

- **Real-Time Dashboards**
  - Live P&L tracking (Daily & Overall)
  - Win rate percentage
  - Trade count and statistics
  - Market connection status

- **Advanced Components**
  - Countdown timers for market hours
  - Interactive time pickers for configuration
  - Calendar-based date range selectors
  - Professional dialog templates

- **User Feedback**
  - Confirmation dialogs for critical actions
  - Info dialogs for notifications
  - Error dialogs with retry functionality
  - Success animations with auto-close
  - Loading indicators for long operations

---

## Tech Stack

### Frontend

| Component | Version | Purpose |
|-----------|---------|---------|
| **Kotlin** | 1.9.x | Primary programming language |
| **Jetpack Compose** | 2025.12 | Modern UI framework |
| **Material3** | Latest | Design system |
| **ViewModel** | 2.6.2 | State management |
| **StateFlow** | Latest | Reactive data flow |
| **Hilt** | 2.48 | Dependency injection |
| **Coroutines** | 1.7.x | Async operations |
| **Timber** | 5.x | Logging |

### Build & Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **Gradle** | 8.5+ | Build system |
| **AGP** | 8.5+ | Android Gradle Plugin |
| **Min SDK** | 26 | Android 8.0+ support |
| **Target SDK** | 34 | Latest Android APIs |
| **Compile SDK** | 34 | Latest compilation target |

---

## Project Structure

```
MyAlgoTradeApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/trading/orb/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          (Data models)
│   │   │   │   │   ├── remote/         (API clients - Future)
│   │   │   │   │   ├── local/          (Room DB - Future)
│   │   │   │   │   └── repository/     (Repository pattern)
│   │   │   │   ├── di/                 (Dependency injection)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     (15 reusable components)
│   │   │   │   │   ├── screens/        (7 app screens)
│   │   │   │   │   ├── theme/          (Colors, typography)
│   │   │   │   │   ├── navigation/     (Routes, nav items)
│   │   │   │   │   └── viewmodel/      (MVVM ViewModels)
│   │   │   │   └── util/               (Utilities - Future)
│   │   │   └── res/                    (Resources)
│   │   ├── test/                       (Unit tests)
│   │   └── androidTest/                (Instrumented tests)
│   └── build.gradle.kts                (App build config)
├── gradle/                             (Gradle wrapper)
├── build.gradle.kts                    (Root build config)
├── settings.gradle.kts                 (Project settings)
├── libs.versions.toml                  (Dependency versions)
├── .gitignore                          (Git ignore rules)
├── PROJECT_STANDARDS.md                (Development guidelines)
├── README.md                           (This file)
└── LINT_REPORT.md                      (Lint analysis)
```

---

## Getting Started

### Prerequisites

- **Android Studio** 2023.1 or higher
- **Java** 17 or higher
- **Gradle** 8.5 or higher
- **Android SDK** API 26+ (for testing)
- **Git** for version control

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/jsiva001/MyAlgoTradingApp.git
cd MyAlgoTradingApp
```

2. **Open in Android Studio**

```bash
# Or open from command line
open -a "Android Studio" .
```

3. **Sync Gradle**

- File → Sync Now
- Wait for gradle sync to complete

4. **Configure local.properties** (if needed)

```properties
sdk.dir=/path/to/android/sdk
ndk.dir=/path/to/android/ndk
```

5. **Build the project**

```bash
./gradlew build
```

6. **Run the app**

```bash
./gradlew installDebug
# Or use Android Studio's Run button
```

---

## Development

### Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore)
./gradlew assembleRelease

# Full build with tests
./gradlew build

# Build without tests (faster)
./gradlew build -x test
```

### Linting & Quality

```bash
# Run lint analysis
./gradlew lint

# View HTML report
open app/build/reports/lint-results-debug.html

# Format code
./gradlew spotlessApply  # (if configured)
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "com.trading.orb.ui.viewmodel.*"
```

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "feat: your commit message"

# Push to remote
git push -u origin feature/your-feature-name

# Create pull request on GitHub
```

---

## Architecture

### MVVM + Repository Pattern

```
┌─────────────────────────────────────┐
│      UI Layer (Compose)             │
│  (Screens, Components, Theme)       │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│    ViewModel Layer                  │
│  (State Management, Events)         │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│    Repository Layer                 │
│  (Business Logic, Data Aggregation) │
└──────────────────┬──────────────────┘
                   │
        ┌──────────┴───────────┐
        ▼                      ▼
   ┌─────────────┐      ┌──────────────┐
   │  Remote     │      │  Local       │
   │  (API)      │      │  (Database)  │
   └─────────────┘      └──────────────┘
```

### Data Flow

1. **UI observes ViewModel state** via StateFlow
2. **User interaction** triggers ViewModel methods
3. **ViewModel delegates** to Repository
4. **Repository processes** and returns results
5. **ViewModel updates** state
6. **UI recomposes** automatically

### State Management Example

```kotlin
// ViewModel exposes read-only StateFlow
val appState: StateFlow<AppState> = _appState.asStateFlow()

// Composable collects state
val state by viewModel.appState.collectAsState()

// UI renders based on state
StatusBadge(
    text = state.strategyStatus.name,
    color = if (state.strategyStatus == ACTIVE) Success else Error
)
```

---

## Components

### Dialog Templates (5 types)

| Dialog | Use Case | Features |
|--------|----------|----------|
| **ConfirmationDialog** | Critical actions | Two-button, dangerous mode |
| **InfoDialog** | Notifications | Single button, icon support |
| **ErrorDialog** | Error handling | Retry button, error code |
| **LoadingDialog** | Long operations | Non-dismissible spinner |
| **SuccessDialog** | Success feedback | Animated, auto-close |

### Timer Components (6 types)

| Component | Use Case |
|-----------|----------|
| **CountdownTimer** | Real-time countdown to target time |
| **TimerDisplay** | Visual timer with color coding |
| **SimpleTimer** | Compact timer with label |
| **TradingHoursTimer** | Market open/close status |
| **TimeInputField** | Interactive time picker |
| **TimePickerDialog** | Hour/minute spinner selection |

### Date Picker Components (4 types)

| Component | Use Case |
|-----------|----------|
| **DateInputField** | Single date selection |
| **DateRangePicker** | Start/end date selection |
| **CalendarPickerDialog** | Full calendar interface |
| **QuickDateRangeSelector** | Preset date ranges |

---

## API Integration (Future)

When adding backend APIs:

1. **Create API Service** in `data/remote/`

```kotlin
interface TradingApi {
    @POST("/api/strategy/start")
    suspend fun startStrategy(): ApiResponse<Unit>
    
    @GET("/api/positions")
    suspend fun getPositions(): ApiResponse<List<Position>>
}
```

2. **Create Remote Data Source**

```kotlin
class TradingRemoteDataSource(private val api: TradingApi) {
    suspend fun startStrategy() = api.startStrategy()
    suspend fun getPositions() = api.getPositions()
}
```

3. **Update Repository**

```kotlin
class TradingRepositoryImpl(
    private val remoteSource: TradingRemoteDataSource,
    private val localSource: TradingLocalDataSource
) : TradingRepository {
    override suspend fun startStrategy() = remoteSource.startStrategy()
}
```

---

## Performance

### Build Time

| Build Type | Time | Notes |
|-----------|------|-------|
| Debug (incremental) | ~5s | Cached |
| Debug (clean) | ~45s | Full rebuild |
| Release | ~60s | ProGuard/R8 |

### App Size

| Build Type | Size |
|-----------|------|
| Debug APK | ~35 MB |
| Release APK | ~15-20 MB |
| AAB (Bundle) | ~12-15 MB |

### Runtime Performance

- **Memory**: ~150-200 MB (normal usage)
- **Startup**: <2 seconds
- **Recomposition**: <100ms (typical)

---

## Quality Metrics

### Current Status

✅ **Build**: Successful  
✅ **Errors**: 0  
⚠️ **Warnings**: 104 (non-critical)  
✅ **Code Quality**: Good

### Lint Categories

- DefaultLocale (18) → Fix in next phase
- Dependency Updates (Multiple) → Plan updates
- OldTargetApi (1) → Update when ready
- Performance (1) → Migrate to KSP

---

## Contributing

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- See [PROJECT_STANDARDS.md](PROJECT_STANDARDS.md) for detailed guidelines
- Max line length: 120 characters
- Use meaningful variable names

### Commit Convention

```
<type>(<scope>): <subject>

feat(screens): add trade filter
fix(components): resolve timer bug
docs(README): update setup
```

### Pull Request Process

1. Create feature branch from `main`
2. Make changes following code standards
3. Test thoroughly
4. Push and create PR
5. Request review
6. Merge when approved

---

## Troubleshooting

### Common Issues

**Gradle Sync Failing**

```bash
./gradlew clean
./gradlew build
```

**Android Studio Issues**

```bash
# Invalidate caches
File → Invalidate Caches
```

**Build Errors**

```bash
# Clean build
./gradlew clean build

# Check Java version
java -version  # Should be 17+
```

---

## Resources

### Documentation

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Guide](https://dagger.dev/hilt/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### Tools

- [Android Studio](https://developer.android.com/studio)
- [Android Developer Docs](https://developer.android.com/)
- [GitHub Documentation](https://docs.github.com)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contact & Support

**GitHub**: [@jsiva001](https://github.com/jsiva001)  
**Repository**: [MyAlgoTradingApp](https://github.com/jsiva001/MyAlgoTradingApp)

---

## Roadmap

### Phase 1 ✅ (Current)
- ✅ Frontend UI implementation
- ✅ Mock data and state management
- ✅ Component library
- ✅ Navigation setup

### Phase 2 (Next)
- API client setup (Retrofit)
- Backend integration
- WebSocket for real-time data
- Authentication system

### Phase 3 (Future)
- Local database (Room)
- Advanced analytics
- Real-time notifications
- Push notifications
- Machine learning for predictions

---

**Last Updated**: December 10, 2025  
**Status**: Production Ready (Frontend)  
**Next Phase**: Backend API Integration

---

Made with ❤️ by the Development Team
