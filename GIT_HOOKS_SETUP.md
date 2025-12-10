# 🔧 Git Hooks Setup - Local Quality Checks

This document explains the Git pre-commit hooks configuration for automatic local quality checks before pushing code.

---

## 📋 Overview

**Git Hooks** automatically run quality checks on your local machine BEFORE you can push code to GitHub.

```
You make changes → You try to git push → Pre-commit hook runs → 
  Lint/Detekt/Tests execute → If pass → Push allowed ✅ → 
  If fail → Push blocked ❌ (fix and retry)
```

---

## 🎯 What Gets Checked (Pre-Push)

| Check | Tool | Blocks Push? | Time |
|-------|------|--------------|------|
| **Lint** | Android Lint | ✅ YES | 1-2 min |
| **Detekt** | Detekt | ⚠️ WARNING | 1-2 min |
| **Tests** | JUnit | ✅ YES | 2-3 min |
| **Total** | All | | 5-10 min |

---

## 🚀 How It Works

### Local Development Flow

```
1. Make code changes in Android Studio
   ↓
2. git add .
   ↓
3. git commit -m "feat: your feature"
   ↓
4. Pre-commit hook AUTOMATICALLY runs:
   ├─ Lint analysis (must pass)
   ├─ Detekt analysis (warning only)
   └─ Unit tests (must pass)
   ↓
5. If all checks pass → Commit succeeds → Ready to push
   If checks fail → Commit blocked → Fix issues → Retry

6. git push origin main
   ↓
7. Code pushed to GitHub
```

### GitHub Side (Pull Requests Only)

```
1. On PR creation → GitHub Actions trigger
2. CI workflow runs on remote servers:
   ├─ Lint (again, for verification)
   ├─ Detekt (again, for verification)
   ├─ Build (compile release APK)
   └─ Tests (run again)
3. Results shown as PR checks
4. PR can be merged only if checks pass
```

---

## ⚙️ Pre-Commit Hook Details

**File Location:** `.git/hooks/pre-commit`

**What It Does:**

```bash
#!/bin/bash
1. Check lint
   └─ Runs: ./gradlew lint
   └─ Blocks push if fails

2. Check detekt  
   └─ Runs: ./gradlew detekt
   └─ Shows warning if issues found (doesn't block)

3. Run tests
   └─ Runs: ./gradlew test
   └─ Blocks push if fails

4. Summary
   └─ Shows results
   └─ Allows push only if lint & tests pass
```

---

## 💾 When Hooks Run

### Automatic Trigger

Hooks run automatically on:
- `git commit` - **YES** (pre-commit hook)
- `git push` - NO (use commit hooks instead)

### Complete Development Workflow

```
Day 1:
  $ git checkout -b feature/my-feature
  $ (edit code in Android Studio)
  
  $ git add .
  $ git commit -m "feat: add feature"
     ↓ PRE-COMMIT HOOK RUNS
     ├─ Lint checks
     ├─ Detekt checks
     └─ Unit tests
     ↓ If all pass → commit succeeds
     ↓ If fail → commit blocked, fix and retry
  
  $ git push origin feature/my-feature

Day 2:
  $ (open PR on GitHub)
     ↓ GITHUB ACTIONS TRIGGER
     ├─ CI workflow runs
     ├─ Detekt workflow runs
     └─ Results shown as checks

  $ (team reviews, approves)
  
  $ git merge to main
```

---

## 🔍 Understanding the Checks

### 1. Lint Analysis

**What:** Android platform-specific issues  
**Blocks:** YES (must pass)  
**Time:** 1-2 minutes  
**Example Issues:**
- Deprecated API usage
- Missing translations
- Performance problems
- Security issues

**If fails:**
```
❌ Lint failed!
Fix lint issues and try again

Solution:
$ ./gradlew lint  (to see full report)
$ open app/build/reports/lint-results-debug.html
$ Fix the reported issues
$ git add .
$ git commit -m "fix: resolve lint issues"
```

### 2. Detekt Analysis

**What:** Kotlin code quality issues  
**Blocks:** NO (warning only)  
**Time:** 1-2 minutes  
**Example Issues:**
- Code complexity too high
- Long methods
- Unused variables
- Style violations

**If warns:**
```
⚠️ Detekt found issues (non-blocking)
Review detekt report: build/reports/detekt/index.html

You CAN still push, but should fix:
$ ./gradlew detekt
$ open build/reports/detekt/index.html
$ Review and fix issues
```

### 3. Unit Tests

**What:** JUnit tests  
**Blocks:** YES (must pass)  
**Time:** 2-3 minutes  
**Example:**
- Test failures
- Assertion errors
- Null pointer exceptions

**If fails:**
```
❌ Tests failed!
Fix failing tests and try again

Solution:
$ ./gradlew test  (to see failures)
$ Fix the failing test code
$ git add .
$ git commit -m "fix: resolve test failures"
```

---

## 📱 Running Checks Manually

You can run checks manually anytime without committing:

```bash
# Run lint only
./gradlew lint
open app/build/reports/lint-results-debug.html

# Run detekt only
./gradlew detekt
open build/reports/detekt/index.html

# Run tests only
./gradlew test

# Run all checks (like pre-commit does)
./gradlew clean build lint detekt test

# Check specific test file
./gradlew test --tests "com.trading.orb.ui.viewmodel.*"
```

---

## ⏭️ Bypassing Hooks (Emergency Only)

Sometimes you need to skip checks (not recommended):

```bash
# Skip pre-commit hook (USE WITH CAUTION!)
git commit --no-verify -m "your message"

# Then push
git push origin main

# ⚠️ WARNING: GitHub Actions will still run and may fail!
```

**When to use:**
- Documentation-only commits: `git commit --no-verify -m "docs: update readme [skip ci]"`
- Emergency hotfixes
- Work-in-progress branches

**Normal case:** ALWAYS run the checks!

---

## 🔄 Hook Execution Timeline

```
9:00 AM
  You: git add .
  You: git commit -m "feat: add feature"
  
9:00-9:02 AM
  Git Hook: Running lint...
  Git Hook: Running detekt...
  Git Hook: Running tests...
  
9:02 AM
  Git Hook: ✅ All checks passed!
  Git Hook: Commit succeeded
  
9:02 AM
  You: git push origin main
  
9:03 AM
  Code reaches GitHub
  
9:03-9:13 AM
  GitHub Actions: Running CI workflow
  
9:13 AM
  Results available on GitHub
```

---

## 🎯 Best Practices

### ✅ DO

```bash
# 1. Make changes
(edit code in Android Studio)

# 2. Commit with hooks
git add .
git commit -m "feat: your feature"
(hooks run automatically)

# 3. If hooks fail, fix and retry
./gradlew lint  (check report)
(fix issues)
git add .
git commit -m "fix: resolve issues"

# 4. Push when ready
git push origin main

# 5. Create PR for code review
(on GitHub)
```

### ❌ DON'T

```bash
# ❌ Don't skip hooks on main branch
git commit --no-verify -m "quick fix" (on main)

# ❌ Don't commit broken code
git add .
git commit --no-verify (skipping checks)

# ❌ Don't ignore detekt warnings
(Detekt warnings can hide issues)

# ❌ Don't push failing tests
(GitHub will fail anyway)
```

---

## 📊 Workflow Comparison

### Before (Without Hooks)

```
Developer → Commits anything → Pushes to GitHub → 
  GitHub Actions runs (5-10 min) → FAILS → 
  Developer fixes → Retries → Success
```

**Problem:** Wasted time, failed builds visible to team

### After (With Hooks) ✅

```
Developer → Pre-commit hook runs (5-10 min) → 
  Checks pass → Commits → Pushes → 
  GitHub (verification only) → Success
```

**Benefit:** Fast feedback, only working code pushed, team sees green builds

---

## 🔧 Customizing Hooks

If you want to modify the hook behavior:

**Edit hook file:**
```bash
nano .git/hooks/pre-commit
```

**Example modifications:**

```bash
# Skip detekt (already has checks on GitHub)
# Comment out:
# ./gradlew detekt --quiet

# Make lint warnings non-blocking
# Change: LINT_STATUS check to warning only
# if [ $LINT_STATUS -ne 0 ]; then
#     echo "⚠️ Lint warnings found"
# fi

# Skip tests for documentation commits
# if [[ $1 == *"[skip ci]"* ]]; then
#     exit 0
# fi
```

**After editing:**
```bash
chmod +x .git/hooks/pre-commit
```

---

## 🚨 Troubleshooting

### Problem: Hook not running

**Solution:**
```bash
# Check if hook exists
ls -la .git/hooks/pre-commit

# Make it executable
chmod +x .git/hooks/pre-commit

# Verify it's executable
file .git/hooks/pre-commit
# Should output: Bourne-Again shell script, ASCII text executable
```

### Problem: "Permission denied"

**Solution:**
```bash
chmod +x .git/hooks/pre-commit
git commit -m "your message"
```

### Problem: "gradle not found"

**Solution:**
```bash
cd /Users/siva/Siva/Dev/Projects/MyAlgoTradeApp
chmod +x gradlew
git commit -m "your message"
```

### Problem: Hook taking too long

**Solution:**
```bash
# Run tests in parallel
./gradlew test --parallel

# Or skip optional checks locally
git commit --no-verify -m "wip: work in progress"
(fix later before final push)
```

---

## 📈 Comparing Check Locations

| Aspect | Local (Android Studio) | Remote (GitHub) |
|--------|------------------------|-----------------|
| **When** | Before commit | On PR creation |
| **Who** | You (developer) | Entire team |
| **Block** | YES (can't push broken code) | YES (can't merge PR) |
| **Visible** | Only to you | Visible to team |
| **Time** | Immediate | 5-10 min |
| **Feedback** | Instant | Delayed |
| **Cost** | Your CPU | GitHub free |

---

## ✅ Setup Verification

To verify hooks are working:

```bash
# Test 1: Make a deliberate lint error
(edit code to violate lint rule)
git add .
git commit -m "test: lint error"
# Expected: Commit should be blocked

# Test 2: Broken test
(break a test intentionally)
git add .
git commit -m "test: broken test"
# Expected: Commit should be blocked

# Test 3: Working code
(fix the code)
git add .
git commit -m "test: working code"
# Expected: Commit should succeed
```

---

## 🎊 Summary

✅ **Local Checks:** Pre-commit hooks run automatically before each commit  
✅ **Blocks Bad Code:** Can't push lint/test failures to GitHub  
✅ **Fast Feedback:** Get results in seconds locally vs minutes on GitHub  
✅ **Team Protection:** Only working code reaches GitHub  
✅ **PR Verification:** GitHub Actions verify again on pull requests  

**Result:** Clean, working code always in main branch!

---

## 📞 Support

**Issues with hooks?**
- Check `.git/hooks/pre-commit` file exists
- Verify file is executable: `chmod +x .git/hooks/pre-commit`
- Run tests manually: `./gradlew test`
- Check Android Studio console for errors

---

**Last Updated:** December 10, 2025  
**Status:** ✅ CONFIGURED & ACTIVE  
**Next:** Make a commit to test the hooks!
