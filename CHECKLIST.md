# 📋 Development Checklist

Quick reference for developers working on MyAlgoTradeApp

---

## Before Starting Development

- [ ] Clone/pull latest from `main` branch
- [ ] Create feature branch: `git checkout -b feature/your-feature`
- [ ] Sync gradle: `./gradlew clean build`
- [ ] Read [PROJECT_STANDARDS.md](PROJECT_STANDARDS.md)
- [ ] Check existing issues for context

---

## During Development

### Code Quality

- [ ] Follow naming conventions (Kotlin style guide)
- [ ] No hardcoded values (use constants)
- [ ] Proper error handling (try-catch, Result type)
- [ ] Add documentation for public APIs
- [ ] Use meaningful variable names
- [ ] Max line length: 120 characters
- [ ] No unused imports or variables

### Composition Best Practices

- [ ] Pass only necessary parameters to Composables
- [ ] Hoist state to appropriate level
- [ ] Use proper scoping for LaunchedEffect
- [ ] Implement onDispose for resource cleanup
- [ ] Avoid recomposing entire screens
- [ ] Use remember for local state

### State Management

- [ ] Use StateFlow for persistent state
- [ ] Use MutableStateFlow only in ViewModel/Repository
- [ ] Expose as StateFlow (read-only)
- [ ] Handle state changes in Repository
- [ ] Implement proper error handling

---

## Testing

- [ ] Write unit tests for new ViewModels
- [ ] Write tests for Repository logic
- [ ] Test error scenarios
- [ ] Verify state updates correctly
- [ ] Run tests: `./gradlew test`
- [ ] Aim for 80%+ coverage on critical code

---

## Before Committing

### Code Review (Own Code)

- [ ] Re-read your changes
- [ ] Check for obvious bugs
- [ ] Verify logic is correct
- [ ] Ensure no debug code left
- [ ] Remove commented-out code

### Build & Lint

```bash
# Build successfully
./gradlew build

# No new lint warnings
./gradlew lint

# All tests passing
./gradlew test
```

### Git Hygiene

- [ ] Commit messages follow convention
- [ ] Each commit is logical and focused
- [ ] No merge commits in feature branch
- [ ] Linear history maintained

---

## Committing

### Commit Message Format

```
<type>(<scope>): <subject>

<body (optional)>

<footer (optional)>
```

### Valid Types

- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code refactoring
- `docs` - Documentation
- `style` - Code formatting
- `test` - Adding tests
- `chore` - Dependency updates
- `perf` - Performance improvement

### Examples

```bash
git commit -m "feat(screens): add position alerts screen"
git commit -m "fix(components): resolve timer display bug"
git commit -m "refactor(repository): improve error handling"
git commit -m "docs(README): update setup instructions"
```

---

## Before Creating Pull Request

- [ ] Synced with upstream main: `git pull upstream main`
- [ ] Resolved any merge conflicts
- [ ] Tests pass locally
- [ ] No lint warnings introduced
- [ ] Feature branch pushed: `git push origin feature/name`
- [ ] Changes are atomic and focused
- [ ] No sensitive data committed
- [ ] Documentation updated if needed

---

## Pull Request Checklist

### PR Title & Description

- [ ] Clear, descriptive title
- [ ] Explains what changed and why
- [ ] References related issues
- [ ] Screenshots/GIFs for UI changes
- [ ] Lists testing performed

### Code Quality

- [ ] No hardcoded values
- [ ] No commented-out code
- [ ] Follows project standards
- [ ] Proper error handling
- [ ] Documentation complete

### Testing

- [ ] Unit tests added
- [ ] All tests passing
- [ ] Edge cases covered
- [ ] No breaking changes

---

## During Code Review

### Addressing Feedback

- [ ] Understand the feedback
- [ ] Make requested changes
- [ ] Re-test locally
- [ ] Push updated changes
- [ ] Mark conversations as resolved
- [ ] Request re-review

### Self Review Before Submitting

```markdown
## Testing Performed
- [ ] Unit tests added
- [ ] Manual testing completed
- [ ] No regressions found

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactoring

## Checklist
- [ ] Code follows standards
- [ ] No lint warnings
- [ ] Documentation updated
- [ ] Tests added/updated
```

---

## After Merge

- [ ] Delete feature branch locally: `git branch -d feature/name`
- [ ] Delete feature branch remotely: `git push origin -d feature/name`
- [ ] Pull latest main: `git checkout main && git pull`
- [ ] Verify changes are live on GitHub
- [ ] Close related issues
- [ ] Update project board if applicable

---

## Release Checklist

Before releasing a new version:

### Preparation

- [ ] All features merged to main
- [ ] Version updated in build.gradle.kts
- [ ] CHANGELOG.md updated
- [ ] All tests passing
- [ ] No lint warnings
- [ ] Code reviewed

### Testing

- [ ] Full build successful: `./gradlew build`
- [ ] Lint clean: `./gradlew lint`
- [ ] Release APK builds: `./gradlew assembleRelease`
- [ ] Manual testing on device/emulator

### Deployment

- [ ] Tag release: `git tag v1.0.0`
- [ ] Push tag: `git push origin v1.0.0`
- [ ] Create GitHub release with notes
- [ ] Upload APK/AAB if applicable

---

## Quick Commands

```bash
# Setup
git clone https://github.com/jsiva001/MyAlgoTradingApp.git
cd MyAlgoTradingApp

# Development
git checkout -b feature/your-feature
./gradlew build
./gradlew test

# Commit & Push
git add .
git commit -m "feat(scope): description"
git push -u origin feature/your-feature

# Sync with upstream
git fetch upstream
git rebase upstream/main

# Clean up
git branch -d feature/your-feature
git push origin -d feature/your-feature
```

---

## Useful Links

- 📖 [PROJECT_STANDARDS.md](PROJECT_STANDARDS.md) - Development standards
- 📋 [README.md](README.md) - Project documentation
- 🤝 [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guide
- 🐛 [Issues](https://github.com/jsiva001/MyAlgoTradingApp/issues)
- 📊 [Pull Requests](https://github.com/jsiva001/MyAlgoTradingApp/pulls)

---

## Quick Tips

### Lint Report

```bash
./gradlew lint
open app/build/reports/lint-results-debug.html
```

### Format Code (if configured)

```bash
./gradlew spotlessApply
```

### Run Specific Test

```bash
./gradlew test --tests "com.trading.orb.ui.viewmodel.TradingViewModelTest"
```

### Clean Build

```bash
./gradlew clean build
```

### Check Dependencies

```bash
./gradlew dependencies
```

---

## Troubleshooting

**Build Failing?**
```bash
./gradlew clean build --stacktrace
```

**Gradle Sync Issues?**
```bash
File → Invalidate Caches → Restart
```

**Test Failures?**
```bash
./gradlew test --info
```

**Git Merge Conflict?**
```bash
git status  # See conflicts
# Fix files in editor
git add .
git commit -m "resolve merge conflict"
```

---

## Standards Summary

| Aspect | Standard |
|--------|----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM + Repository |
| **DI** | Hilt |
| **State Mgmt** | StateFlow |
| **Min SDK** | 26 |
| **Target SDK** | 34 |
| **Build System** | Gradle 8.5+ |
| **Java Version** | 17+ |
| **Line Length** | 120 chars max |
| **Commit Style** | Conventional |
| **Test Coverage** | 75%+ |

---

**Last Updated**: December 10, 2025  
**Remember**: Quality > Speed 🚀
