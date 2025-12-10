# CI/CD Workflows & Automation

This document describes the automated quality checks configured for MyAlgoTradeApp using GitHub Actions.

---

## Overview

Whenever code is pushed to the repository, the following automated checks run:

1. **Lint Analysis** - Android Lint checks
2. **Detekt Analysis** - Static code analysis for Kotlin
3. **Build Verification** - Debug APK build
4. **Unit Tests** - Run test suite
5. **Quality Gates** - Summary of all checks

---

## Workflows

### 1. CI Workflow (`ci.yml`)

**Trigger:** Push to `main`, `develop`, or `feature/*` branches, or Pull Request

**Jobs:**

#### a. Lint & Quality Check
- Runs Android Lint analysis
- Generates HTML lint reports
- Uploads artifacts for review
- Fails if critical issues found

**Status Badge:**
```markdown
[![CI Status](https://github.com/jsiva001/MyAlgoTradingApp/workflows/CI%20-%20Code%20Quality%20%26%20Build/badge.svg)](https://github.com/jsiva001/MyAlgoTradingApp/actions)
```

#### b. Build Debug APK
- Builds debug APK
- Uploads build artifacts
- Verifies compilation success
- Runs only after lint passes

#### c. Unit Tests
- Executes all unit tests
- Generates test reports
- Uploads test artifacts
- Checks for test failures

#### d. Quality Gates
- Aggregates all check results
- Generates summary report
- Provides recommendations
- Updates GitHub Actions summary

---

### 2. Detekt Workflow (`detekt.yml`)

**Trigger:** Push to `main`, `develop`, or `feature/*` branches, or Pull Request

**Jobs:**

#### Detekt Code Analysis
- Runs comprehensive code quality analysis
- Checks for code smells
- Verifies style guidelines
- Generates detailed reports
- Runs in parallel with CI workflow

**Detekt Checks Include:**
- Complexity metrics
- Style violations
- Potential bugs
- Performance issues
- Naming conventions
- Exception handling
- Coroutine usage
- Android-specific issues

---

## Workflow Artifacts

Each workflow run generates artifacts that can be downloaded:

### From CI Workflow
- `lint-reports/` - HTML lint analysis reports
- `debug-apk/` - Compiled debug APK
- `test-reports/` - Unit test reports

### From Detekt Workflow
- `detekt-report/` - HTML detekt analysis report

**Artifacts are retained for:**
- Lint reports: 30 days
- Debug APK: 7 days
- Test reports: 30 days
- Detekt reports: 30 days

---

## How to View Results

### In GitHub UI

1. Go to repository → **Actions** tab
2. Select the workflow run
3. View job logs in **Summary**
4. Download artifacts from **Artifacts** section
5. Check **Annotations** for issues found

### Direct Access

**Workflow Runs:**
- CI: `https://github.com/jsiva001/MyAlgoTradingApp/actions/workflows/ci.yml`
- Detekt: `https://github.com/jsiva001/MyAlgoTradingApp/actions/workflows/detekt.yml`

### In Pull Requests

Workflow status appears as **Checks** on PR:
- Shows pass/fail status for each job
- Comments with issues if found
- Allows dismissing checks if needed
- Blocks merge if critical issues

---

## Local Workflow Testing

### Test Lint Locally

```bash
./gradlew lint
open app/build/reports/lint-results-debug.html
```

### Test Detekt Locally

```bash
./gradlew detekt
open build/reports/detekt/index.html
```

### Test Build Locally

```bash
./gradlew assembleDebug
```

### Test Full Workflow

```bash
./gradlew clean build lint detekt
```

---

## Configuration Files

### CI Workflow
**File:** `.github/workflows/ci.yml`
- 170+ lines
- 5 jobs (lint, build, test, gates)
- Runs on Ubuntu latest
- Uses JDK 17
- Gradle caching enabled
- Concurrent runs canceled (cost optimization)

### Detekt Workflow
**File:** `.github/workflows/detekt.yml`
- 50+ lines
- 1 job (detekt)
- Runs on Ubuntu latest
- Uses JDK 17
- Parallel with CI workflow

### Detekt Configuration
**File:** `detekt.yml`
- 600+ lines
- Comprehensive rule configuration
- Customized for Compose projects
- Excludes test directories
- Performance optimized

### Build Configuration
**File:** `build.gradle.kts` (updated)
- Detekt plugin added
- HTML reports enabled
- SARIF format enabled
- Custom configuration file referenced

---

## Quality Gates

### Lint
- **Status:** ✅ PASSING
- **Threshold:** < 10 critical errors
- **Current Issues:** 104 non-critical warnings
- **Action:** Warnings addressed in future sprints

### Detekt
- **Status:** ✅ CONFIGURED
- **Check Types:** 40+ rules enabled
- **Exclusions:** Test directories
- **Threshold:** Configurable per rule

### Build
- **Status:** ✅ PASSING
- **Target:** API 26-35
- **Type:** Debug & Release APKs
- **Size:** ~35 MB (debug), ~15-20 MB (release)

### Tests
- **Status:** ⏳ PENDING
- **Target:** 75%+ coverage
- **Framework:** JUnit/Compose Testing
- **Scope:** Unit + Instrumented tests

---

## Failure Scenarios

### When Workflows Fail

1. **Lint Failure**
   - Workflow continues (non-blocking)
   - Report uploaded for review
   - Manual action recommended
   - Doesn't block merge (configurable)

2. **Build Failure**
   - Workflow stops
   - Issue: Compilation error
   - Action: Fix code and repush
   - PR merge blocked

3. **Test Failure**
   - Workflow continues
   - Report uploaded
   - Action: Fix failing tests
   - Can block merge (configurable)

4. **Detekt Failure**
   - Workflow continues (non-blocking)
   - Report generated
   - Action: Review and fix issues
   - Recommendations provided

---

## Troubleshooting

### Workflow Not Running

**Check:**
1. Push was to correct branch (main, develop, feature/*)
2. GitHub Actions enabled in repository settings
3. Workflows files are in `.github/workflows/`
4. No `[skip ci]` in commit message

**Fix:**
```bash
# Retrigger workflow
git commit --allow-empty -m "trigger ci"
git push
```

### Lint Report Not Generated

**Issue:** Gradle task failed before lint ran

**Solution:**
```bash
./gradlew clean build
./gradlew lint
```

### Detekt Not Found

**Issue:** Plugin not configured

**Solution:**
1. Ensure `build.gradle.kts` has detekt plugin
2. Ensure `detekt.yml` exists in root
3. Run: `./gradlew detekt`

### Timeout Issues

**Cause:** Large project or slow runner

**Solution:**
```bash
# Locally run heavy tasks
./gradlew lint --no-daemon
./gradlew test --no-daemon
```

---

## Best Practices

### 1. Commit Messages
```bash
# Include [skip ci] only if necessary
git commit -m "docs: update readme [skip ci]"

# Otherwise always run CI
git commit -m "feat(screens): add new feature"
```

### 2. Push Frequently
- Push small, focused commits
- Faster feedback loops
- Easier to fix failures

### 3. Monitor Artifacts
- Download reports regularly
- Track lint trends
- Review detekt findings

### 4. Fix Issues Early
- Address warnings promptly
- Fix style violations
- Improve test coverage

### 5. Read Reports
- Check lint HTML reports
- Review detekt recommendations
- Understand failure reasons

---

## Performance Metrics

### Workflow Execution Time

| Job | Typical Time | Notes |
|-----|--------------|-------|
| Lint | 1-2 min | Gradle cache helps |
| Detekt | 1-2 min | Runs in parallel |
| Build | 3-5 min | First run slower |
| Tests | 2-3 min | Depends on test count |
| **Total** | **5-10 min** | Parallel execution |

### Cost Optimization

- **Concurrent cancellation** enabled (saves time)
- **Gradle caching** enabled (saves bandwidth)
- **Minimal dependencies** to download
- **Matrix builds** not used (single config)

---

## Future Enhancements

### Planned
- [ ] Code coverage reporting (Jacoco/Kover)
- [ ] SonarQube integration
- [ ] Dependency vulnerability scanning
- [ ] Performance benchmarking
- [ ] Visual regression testing
- [ ] Automated code review comments

### Optional
- [ ] APK size tracking
- [ ] Build time analysis
- [ ] Custom notifications
- [ ] Slack integration
- [ ] Email reports

---

## Related Documentation

- [PROJECT_STANDARDS.md](PROJECT_STANDARDS.md) - Development standards
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [CHECKLIST.md](CHECKLIST.md) - Pre-commit checklist

---

## Support

**Workflow Issues:**
- Check workflow logs in Actions tab
- Review error messages
- Run commands locally to debug
- Check GitHub Actions documentation

**Questions:**
- See [CONTRIBUTING.md](CONTRIBUTING.md)
- Open GitHub issue
- Contact maintainers

---

**Last Updated:** December 10, 2025  
**Status:** ✅ CONFIGURED & ACTIVE  
**Next Review:** Q1 2026
