# 🧪 Mock ORB Strategy Testing Guide

**Status:** ✅ READY TO TEST  
**Last Updated:** December 14, 2024

---

## 📱 Testing the Mock ORB Strategy

Now that the mock API server is connected with the dashboard, you can initiate and test the ORB strategy directly from the dashboard screen!

### 🎮 How to Test

#### Step 1: Build and Run the App
```bash
./gradlew assembleDebug
# Install on emulator/device
```

#### Step 2: Open Dashboard
The app opens directly to the Dashboard screen with:
- Today's P&L (₹0.00 initially)
- Active positions (0)
- Win Rate (0%)
- Strategy Status: **INACTIVE** (○)
- **START** button (green)

#### Step 3: Click "Start" Button

**What happens:**
```
User clicks "START" button
    ↓
toggleStrategy() called
    ↓
appState.strategyStatus == INACTIVE → initialize mock
    ↓
initializeAndStartMockStrategy("normal") called
    ↓
🧪 Create MockMarketDataSource
🧪 Create MockOrderExecutor
🎯 Initialize OrbStrategyEngine
    ↓
Strategy starts running
    ↓
Dashboard updates in real-time
```

---

## 📊 What You'll See During Testing

### Phase 1: Strategy Initialization (0-1 seconds)
```
LOG: 🧪 Initializing MOCK ORB Strategy Engine
LOG: ✅ MOCK Strategy Engine initialized successfully
LOG: ✅ MOCK Strategy started!
UI: Shows "Strategy started successfully!" notification
UI: Strategy Status changes to "● Running" (green)
```

### Phase 2: ORB Capture (0-5 seconds)
```
Mock generates 15-minute candle with:
├─ Opening price
├─ High (Resistance)
├─ Low (Support)
└─ Updates every 1 second

LOG: 📈 ORB Captured - High: 185.50, Low: 184.50
UI: ORB Levels Card appears showing:
    ├─ Resistance: ₹185.50
    ├─ Support: ₹184.50
    └─ Range: ₹1.00

UI: Shows "ORB Levels Captured!" notification
```

### Phase 3: Breakout Monitoring (5-15 seconds)
```
Mock continuously updates LTP every 1 second:
├─ Price movements within range (waiting)
├─ Price moving towards resistance/support
└─ Checking against breakout triggers

LOG: 📊 Price Update: ₹185.15
LOG: 📊 Price Update: ₹185.25
LOG: 📊 Price Update: ₹185.48
(Silent updates - no notifications)
```

### Phase 4: Breakout Detected! (10-15 seconds)
```
Price breaks above Resistance OR below Support:

Example: Price goes above 185.50 + offset

LOG: 🟢 Position Opened - Side: BUY, Price: 185.52
UI: Shows "Position opened at ₹185.52" notification
UI: Active positions changes from 0 to 1
UI: Position appears in real-time tracking

Order Details:
├─ Entry: ₹185.52
├─ Target: ₹185.52 + 15.0 = ₹200.52
└─ Stop Loss: ₹185.52 - 8.0 = ₹177.52
```

### Phase 5: Position Management (15-30 seconds)
```
Continuous monitoring for TP/SL:

Scenario A: Target Hit ✅
LOG: 🏁 Position Closed - P&L: +₹15.00
UI: Shows "Trade closed with P&L: +₹15.00" notification
UI: Active positions changes to 0
UI: Today's P&L updates to +₹15.00
UI: Win Rate updates

Scenario B: Stop Loss Hit ❌
LOG: 🏁 Position Closed - P&L: -₹8.00
UI: Shows "Trade closed with P&L: -₹8.00" notification
UI: Active positions changes to 0
UI: Today's P&L updates to -₹8.00
```

### Phase 6: Strategy Complete
```
After position closes:
├─ Ready for next opportunity
├─ Continues monitoring for next breakout
└─ Can run multiple trades per session
```

---

## 🎯 Expected Behavior by Scenario

### Scenario 1: Successful Breakout (Default)
**File Used:** MockScenarios.successfulHighBreakout()

```
Configuration:
├─ Base Price: ₹185.00
├─ Volatility: 0.3 (low - stable prices)
├─ Target Points: 15.0
└─ Stop Loss Points: 8.0

Expected Flow:
1. ORB captures levels
2. Price gradually increases towards resistance
3. Price breaks above resistance
4. BUY order executed
5. Price continues upward
6. Target hit → Trade closes with +₹15
7. Result: WIN ✅
```

**Timeline:** ~20-30 seconds

---

### Scenario 2: Stop Loss Hit
**File Used:** MockScenarios.stopLossScenario()

```
Configuration:
├─ Base Price: ₹189.00
├─ Volatility: 1.5 (high - volatile prices)
├─ Target Points: 15.0
└─ Stop Loss Points: 5.0

Expected Flow:
1. ORB captures levels
2. Price fluctuates wildly
3. Breakout occurs
4. Order executed
5. Price quickly reverses
6. Stop Loss hit → Trade closes with -₹5
7. Result: LOSS ❌
```

**Timeline:** ~15-25 seconds

---

## 📊 Dashboard Real-Time Updates

As the strategy runs, the dashboard updates in real-time:

### Stats Card (Top)
```
Before:               During/After:
┌─────────────────┐  ┌─────────────────┐
│ Today's P&L     │  │ Today's P&L     │
│ ₹0.00           │→ │ +₹15.00 or -₹8  │
├─────────────────┤  ├─────────────────┤
│ Active          │  │ Active          │
│ 0               │→ │ 1 (during trade)│
├─────────────────┤  ├─────────────────┤
│ Win Rate        │  │ Win Rate        │
│ 0%              │→ │ 100% or 0%      │
└─────────────────┘  └─────────────────┘
```

### Strategy Status Card
```
Before:                  During:
┌──────────────────────┐ ┌──────────────────────┐
│ Strategy Status      │ │ Strategy Status      │
│ ○ Inactive           │→│ ● Running            │
│ [START]              │ │ [STOP]               │
└──────────────────────┘ └──────────────────────┘

After Position Closes:
┌──────────────────────┐
│ Strategy Status      │
│ ● Running            │
│ [STOP]               │
│ (Waiting for next)   │
└──────────────────────┘
```

### ORB Levels Card
```
After ORB Capture:
┌──────────────────────┐
│ ORB Levels           │
│ Resistance: ₹185.50  │
│ Support: ₹184.50     │
│ Current LTP: ₹185.15 │
└──────────────────────┘
```

---

## 🔍 Monitoring via Logcat

Open Android Studio Logcat and filter for our logs:

### Filter: `orb|ORB|strategy|MOCK`

You'll see:

```
D/TradingViewModel: 🧪 Initializing MOCK ORB Strategy Engine
D/OrbStrategyEngine: ORB Strategy started for NIFTY24DEC22000CE
D/OrbStrategyEngine: 📈 ORB Captured - High: 185.50, Low: 184.50
V/OrbStrategyEngine: 📊 Price Update: ₹185.25
V/OrbStrategyEngine: 📊 Price Update: ₹185.35
D/OrbStrategyEngine: 🟢 Position Opened - Side: BUY, Price: ₹185.52
V/OrbStrategyEngine: 💹 Position Update - Current Price: ₹185.75
V/OrbStrategyEngine: 💹 Position Update - Current Price: ₹186.00
D/OrbStrategyEngine: 🏁 Position Closed - P&L: +₹15.00
```

---

## 🎮 Interactive Testing

### Test 1: Basic Startup
**Steps:**
1. Open app
2. Click "START"
3. Observe notifications

**Expected:** Strategy starts, notifications appear

---

### Test 2: Multiple Trades
**Steps:**
1. Click "START"
2. Wait for first trade to close
3. Strategy automatically waits for second opportunity
4. Observe multiple trades executing

**Expected:** Multiple P&L updates, win rate changes

---

### Test 3: Emergency Stop
**Steps:**
1. Click "START"
2. While strategy running, click "STOP"
3. Observe strategy halts

**Expected:** Positions closed, P&L locked

---

### Test 4: Different Scenarios
**Code location:** TradingViewModel.initializeAndStartMockStrategy()

**To test stop loss scenario:**
Edit:
```kotlin
// Change from:
initializeAndStartMockStrategy("normal")

// To:
initializeAndStartMockStrategy("stop_loss")
```

Then rebuild and test.

---

## 📈 What's Happening Behind the Scenes

### 1. MockMarketDataSource
```kotlin
// Generates fake LTP every 1000ms
Flow<Double> with realistic price movements
├─ Base price: ₹185.00
├─ Random walk: ±0.5 points per update
├─ Volatility factor: adjusts magnitude
└─ Result: Realistic price action
```

### 2. OrbStrategyEngine
```kotlin
// Core ORB logic
├─ Wait for 15-minute opening window
├─ Build candle from LTP ticks
├─ Extract High and Low
├─ Calculate resistance/support
├─ Monitor for breakout
├─ Execute entry order
├─ Manage position
└─ Close on TP/SL/error
```

### 3. StrategyEvent Flow
```
ORB Engine emits events:
├─ Started
├─ OrbCaptured
├─ PriceUpdate (frequent)
├─ PositionOpened
├─ PositionUpdate (frequent)
├─ PositionClosed
├─ Stopped
└─ Error

ViewModel listens and:
├─ Updates dashboard UI
├─ Shows notifications
├─ Logs everything
└─ Handles errors
```

---

## 🐛 Debugging Tips

### View Real-Time Logs
```bash
adb logcat | grep -E "orb|ORB|Strategy|MOCK"
```

### Check Strategy Status
Look at Dashboard:
- Strategy Status indicator (● Running or ○ Inactive)
- Active positions count
- Today's P&L

### Monitor Position Updates
```bash
adb logcat | grep "Position Update"
```

### Track Price Changes
```bash
adb logcat | grep "Price Update"
```

---

## 🚀 Next Steps After Testing

### If Tests Pass ✅
1. Try multiple scenarios (default, stop loss, etc.)
2. Test emergency stop button
3. Verify P&L calculations
4. Check trade history updates

### Then Proceed To:
1. **MOCK_TO_REAL_MIGRATION.md** - Integrate real Angel One API
2. Implement paper trading
3. Validate with real market data

---

## ❌ Common Issues & Solutions

### Issue 1: "Strategy started" but no logs
**Solution:**
- Check Logcat filter is correct
- Verify Timber logging is enabled
- Check if app is in debug mode

### Issue 2: ORB never captures
**Solution:**
- MockMarketDataSource might not be generating ticks
- Check if flow is being collected
- Verify candle building logic

### Issue 3: Position doesn't close
**Solution:**
- Price might not be hitting TP/SL
- Check volatility in MockScenarios
- Increase target/reduce stop loss

### Issue 4: Notifications don't appear
**Solution:**
- Ensure permissions for notifications
- Check notification settings
- Verify _uiEvent emission

---

## 📊 Example Test Run Output

**Timeline: ~25 seconds**

```
00:00 - App opens
       Dashboard shows: P&L ₹0, Active 0, Win Rate 0%
       Strategy Status: ○ Inactive [START]

00:01 - User clicks [START] button

00:02 - Log: 🧪 Initializing MOCK ORB Strategy Engine
       Log: ✅ MOCK Strategy Engine initialized
       Notification: "Strategy started successfully!"
       UI: Status changes to ● Running [STOP]

00:08 - Log: 📈 ORB Captured - High: 185.50, Low: 184.50
       UI: ORB Levels Card appears
       Notification: "ORB Levels Captured!"

00:15 - Log: 📊 Price Updates every 1 second
       (silent - no notifications)

00:22 - Log: 🟢 Position Opened at ₹185.52
       UI: Active positions changes to 1
       Notification: "Position opened at ₹185.52"

00:25 - Log: 🏁 Position Closed - P&L: +₹15.00
       UI: Active positions changes to 0
       UI: P&L changes to +₹15.00
       UI: Win Rate becomes 100%
       Notification: "Trade closed with P&L: +₹15.00"

00:26 - Strategy continues waiting for next breakout
```

---

## ✨ Summary

You can now:

✅ Click "START" on dashboard to run mock strategy  
✅ Watch real-time ORB capture  
✅ See automatic breakout detection  
✅ Monitor position management  
✅ View live P&L updates  
✅ Test stop/emergency stop buttons  

**The entire ORB strategy is now operational with mock data!**

Next: Integrate real Angel One API when ready using MOCK_TO_REAL_MIGRATION.md

---

**Status:** 🚀 READY FOR TESTING!
