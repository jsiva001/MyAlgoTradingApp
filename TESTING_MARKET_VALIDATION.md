# ✅ Testing Market Validation & LTP Logging

## Quick Start Guide

### 1. Run App in Debug Mode
```bash
./gradlew installDebug
# App will use USE_MOCK_DATA = true
```

### 2. Monitor Logcat
```bash
# In Android Studio: View → Tool Windows → Logcat
# Or in terminal:
adb logcat | grep -E "TradingViewModel|OrbStrategyEngine"
```

### 3. Click START Button on Dashboard
- **Time**: Any time (mock mode bypasses market check)
- **Expected**: Strategy initializes and shows logs in logcat

## Expected Logcat Output

### Phase 1: Initialization
```
I/TradingViewModel: 🧪 Initializing MOCK ORB Strategy Engine - Scenario: normal
I/TradingViewModel: ✅ Using MOCK DATA - Market time validation skipped
I/TradingViewModel: ✅ MOCK Strategy Engine initialized successfully
I/TradingViewModel: 📊 Market Data Source: MockMarketDataSource
I/TradingViewModel: 🎯 Trading Symbol: NIFTY24DEC22000CE | Lot Size: 50
I/TradingViewModel: ⏰ ORB Window: 09:15 - 10:00
I/TradingViewModel: 💰 Stop Loss: 10 points | Target: 20 points
I/TradingViewModel: ✅ MOCK Strategy started!
```

### Phase 2: ORB Capture (First 15 minutes)
```
I/OrbStrategyEngine: 🟢 Strategy Started - Symbol: NIFTY24DEC22000CE
I/OrbStrategyEngine: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 🎯 Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
```

### Phase 3: Breakout Monitoring
```
D/OrbStrategyEngine: 📊 LTP: ₹22040.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22041.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22050.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22051.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
I/OrbStrategyEngine: 🟢 BUY SIGNAL! LTP ₹22051.00 >= Buy Trigger ₹22050.50
```

### Phase 4: Position Opened
```
I/OrbStrategyEngine: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00
I/TradingViewModel: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00
```

### Phase 5: Position Management
```
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22055.50 | P&L: ₹4.50
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22060.00 | P&L: ₹9.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22070.50 | P&L: ₹19.50
I/OrbStrategyEngine: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
I/TradingViewModel: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
```

## Testing Checklist

### Market Validation
- [ ] **Mock Mode (Debug)**
  - Click START at any time
  - Expected: Strategy starts ✅
  - Note: "Using MOCK DATA - Market time validation skipped" in logs

- [ ] **Real Mode (Release)**
  - Click START outside 9:15 AM - 3:30 PM
  - Expected: Error dialog "Market is closed..." ❌
  - Click START inside 9:15 AM - 3:30 PM
  - Expected: Strategy starts ✅

### LTP Price Logging
- [ ] **Price Updates**
  - Look for "📊 LTP: ₹XX.XX" in logcat
  - Should show every tick (frequent updates)

- [ ] **Breakout Signals**
  - Look for "🟢 BUY SIGNAL!" or "🔴 SELL SIGNAL!"
  - Should show when LTP crosses triggers

- [ ] **Position Details**
  - Look for "🟢 Position Opened"
  - Should show Entry Price, SL, Target
  - Example: `Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00`

- [ ] **Position P&L**
  - Look for "💹 Position Update"
  - Should show Current Price and P&L
  - Example: `Current Price: ₹22055.50 | P&L: ₹4.50`

- [ ] **Position Closure**
  - Look for "🏁 Position Closed"
  - Should show Exit Price, Reason, Final P&L
  - Example: `Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00`

## Troubleshooting

### No Logs Appearing
1. **Check Build Type**
   ```bash
   # Run in debug mode
   ./gradlew installDebug
   ```

2. **Filter Logcat Correctly**
   ```bash
   adb logcat | grep -E "Trading|Strategy"
   ```

3. **Enable Logcat in Android Studio**
   - View → Tool Windows → Logcat
   - Click Filter Config
   - Add new filter with:
     - Package Name: `com.trading.orb`
     - Log Level: `Info` (or Verbose for all logs)

### Strategy Not Starting
1. **Check Market Time (if release build)**
   - Must be 9:15 AM - 3:30 PM IST
   - Check device time

2. **Check Logs for Errors**
   - Look for "❌" symbols
   - Check error messages

3. **Check Button State**
   - START button should change to STOP
   - Check DashboardScreen UI updates

## Verification Commands

### Quick Verification
```bash
# Build and install
./gradlew installDebug

# Watch logs in real-time
adb logcat | grep -E "📊|🟢|🔴|🏁|💹|✅"

# Filter by class
adb logcat | grep "OrbStrategyEngine"
adb logcat | grep "TradingViewModel"
```

### Save Logs to File
```bash
# Capture logs to file
adb logcat > strategy_logs.txt

# Watch and save simultaneously
adb logcat | tee strategy_logs.txt | grep -E "🎯|📊|🟢"
```

## Success Criteria

✅ **All tests passed when you see**:
1. Strategy starts with initialization logs
2. ORB levels captured with High/Low prices
3. LTP prices updating continuously
4. Breakout signal triggered
5. Position opened with SL and Target
6. Position closed with P&L

🎉 **If all above logs appear → Implementation is working correctly!**

