# 📊 Expected Logcat Output When Running Strategy

## How to View Logcat

### Option 1: Android Studio (Easiest)
1. Open Android Studio
2. Click View → Tool Windows → Logcat (or press Cmd+6)
3. Filter by package: `com.trading.orb`
4. Log level: `Info` or `Verbose`

### Option 2: Terminal
```bash
adb logcat | grep -E "TradingViewModel|OrbStrategyEngine"
```

### Option 3: Terminal with Colors
```bash
adb logcat | grep -E "Trading|Strategy" | while IFS= read -r line; do
  echo "$line"
done
```

---

## Phase 1: Strategy Initialization (First 2 seconds)

```
I/TradingViewModel: 🧪 Initializing MOCK ORB Strategy Engine - Scenario: normal
I/TradingViewModel: ✅ Using MOCK DATA - Market time validation skipped
I/TradingViewModel: ✅ MOCK Strategy Engine initialized successfully
I/TradingViewModel: 📊 Market Data Source: MockMarketDataSource
I/TradingViewModel: 🎯 Trading Symbol: NIFTY24DEC22000CE | Lot Size: 50
I/TradingViewModel: ⏰ ORB Window: 09:15 - 10:00
I/TradingViewModel: 💰 Stop Loss: 10 points | Target: 20 points
I/TradingViewModel: ✅ MOCK Strategy started!
I/OrbStrategyEngine: 🟢 Strategy Started - Symbol: NIFTY24DEC22000CE
```

**What this means**: Strategy engine is initialized and waiting for ORB window to start.

---

## Phase 2: ORB Capture (45 minutes duration: 9:15-10:00)

```
I/OrbStrategyEngine: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 🎯 Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
```

**What this means**: 
- High level of 15-min candle: ₹22050.50
- Low level of 15-min candle: ₹22000.00
- Buy Trigger (High + buffer): ₹22050.50
- Sell Trigger (Low - buffer): ₹22000.00

---

## Phase 3: Breakout Monitoring (Continuous)

**These logs appear every time LTP updates (multiple times per second):**

```
D/OrbStrategyEngine: 📊 LTP: ₹22040.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22041.50 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22042.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22045.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22048.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22050.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
```

**What this means**: 
- Price is gradually rising but hasn't triggered buy yet
- LTP < Buy Trigger (₹22050.50), so no signal

---

## Phase 4: Breakout Signal! 🎉

**One of these will appear (depending on price direction):**

### BUY Signal (LTP breaks above High)
```
D/OrbStrategyEngine: 📊 LTP: ₹22051.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
I/OrbStrategyEngine: 🟢 BUY SIGNAL! LTP ₹22051.00 >= Buy Trigger ₹22050.50
```

### OR SELL Signal (LTP breaks below Low)
```
D/OrbStrategyEngine: 📊 LTP: ₹21999.50 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
I/OrbStrategyEngine: 🔴 SELL SIGNAL! LTP ₹21999.50 <= Sell Trigger ₹22000.00
```

**What this means**: 
- Breakout detected!
- Now placing an entry order
- Position will be created if order succeeds

---

## Phase 5: Order Execution (Few milliseconds)

**You'll see:**
```
I/OrbStrategyEngine: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00
I/TradingViewModel: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00
I/TradingViewModel: ✅ Position opened at ₹22051.00
```

**Or for SELL:**
```
I/OrbStrategyEngine: 🟢 Position Opened - Side: SELL, Entry Price: ₹21998.00, SL: ₹22008.00, Target: ₹21978.00
I/TradingViewModel: 🟢 Position Opened - Side: SELL, Entry Price: ₹21998.00, SL: ₹22008.00, Target: ₹21978.00
```

**What this means**:
- Position successfully opened
- Entry Price: Where we entered
- SL (Stop Loss): Entry ± 10 points
- Target: Entry ± 20 points

---

## Phase 6: Position Management (Real-time updates)

**As price moves, you'll see multiple:**
```
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22055.00 | P&L: ₹4.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22060.00 | P&L: ₹9.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22065.00 | P&L: ₹14.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22070.00 | P&L: ₹19.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22070.50 | P&L: ₹19.50
```

**What this means**:
- Position is live and tracking price
- Current Price is the LTP
- P&L shows profit/loss in points
- Every update is silent (no toasts)

---

## Phase 7: Position Exit 🏁

**One of these scenarios will happen:**

### Scenario A: Target Hit ✅
```
I/OrbStrategyEngine: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
I/TradingViewModel: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
I/TradingViewModel: ✅ Trade closed with P&L: +₹20
```

### Scenario B: Stop Loss Hit ❌
```
I/OrbStrategyEngine: 🏁 Position Closed - Exit Price: ₹22041.00, Reason: SL_HIT, P&L: ₹-10.00
I/TradingViewModel: 🏁 Position Closed - Exit Price: ₹22041.00, Reason: SL_HIT, P&L: ₹-10.00
I/TradingViewModel: ✅ Trade closed with P&L: ₹-10
```

### Scenario C: Time Exit ⏰
```
I/OrbStrategyEngine: 🏁 Position Closed - Exit Price: ₹22065.00, Reason: TIME_EXIT, P&L: ₹14.00
I/TradingViewModel: 🏁 Position Closed - Exit Price: ₹22065.00, Reason: TIME_EXIT, P&L: ₹14.00
```

**What this means**: Position has been closed and profit/loss calculated

---

## Phase 8: Strategy Stop (When you click STOP)

```
I/TradingViewModel: 🛑 Stopping strategy...
I/OrbStrategyEngine: ⏹️ Strategy Stopped
I/TradingViewModel: ✅ Strategy stopped successfully
I/TradingViewModel: ✅ Strategy stopped
```

**What this means**: Strategy is now stopped, no more trading

---

## Complete Trading Cycle Example

```
I/TradingViewModel: 🧪 Initializing MOCK ORB Strategy Engine - Scenario: normal
I/TradingViewModel: ✅ Using MOCK DATA - Market time validation skipped
I/TradingViewModel: ✅ MOCK Strategy Engine initialized successfully
I/TradingViewModel: 📊 Market Data Source: MockMarketDataSource
I/TradingViewModel: 🎯 Trading Symbol: NIFTY24DEC22000CE | Lot Size: 50
I/TradingViewModel: ⏰ ORB Window: 09:15 - 10:00
I/TradingViewModel: 💰 Stop Loss: 10 points | Target: 20 points
I/TradingViewModel: ✅ MOCK Strategy started!
I/OrbStrategyEngine: 🟢 Strategy Started - Symbol: NIFTY24DEC22000CE

[... waiting for ORB window ...]

I/OrbStrategyEngine: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 📈 ORB Captured - High: 22050.50, Low: 22000.00
I/TradingViewModel: 🎯 Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00

[... monitoring for breakout ...]

D/OrbStrategyEngine: 📊 LTP: ₹22040.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22045.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
D/OrbStrategyEngine: 📊 LTP: ₹22051.00 | Buy Trigger: ₹22050.50 | Sell Trigger: ₹22000.00
I/OrbStrategyEngine: 🟢 BUY SIGNAL! LTP ₹22051.00 >= Buy Trigger ₹22050.50

I/OrbStrategyEngine: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00
I/TradingViewModel: 🟢 Position Opened - Side: BUY, Entry Price: ₹22051.00, SL: ₹22041.00, Target: ₹22071.00

[... position management ...]

D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22055.00 | P&L: ₹4.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22060.00 | P&L: ₹9.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22070.00 | P&L: ₹19.00
D/OrbStrategyEngine: 💹 Position Update - Current Price: ₹22071.00 | P&L: ₹20.00

I/OrbStrategyEngine: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
I/TradingViewModel: 🏁 Position Closed - Exit Price: ₹22071.00, Reason: TARGET_HIT, P&L: ₹20.00
I/TradingViewModel: ✅ Trade closed with P&L: +₹20
```

---

## Log Levels Explained

| Level | Prefix | Usage | Frequency |
|-------|--------|-------|-----------|
| Error | E/ | Errors | Rare |
| Warn | W/ | Warnings | Occasional |
| Info | I/ | Important events | Frequent |
| Debug | D/ | Debug info | Very frequent |
| Verbose | V/ | All details | Continuous |

**We use:**
- **I/** for major events (ORB Captured, Position Opened, Signals)
- **D/** for continuous updates (Price updates, position tracking)
- **V/** for very detailed tracing

---

## Troubleshooting Logcat Issues

### No logs appearing?
1. Make sure you're filtering correctly:
   ```bash
   adb logcat "*:S" com.trading.orb:I
   ```

2. Check logcat buffer isn't full:
   ```bash
   adb logcat -c  # Clear logs
   ```

3. Rebuild and reinstall:
   ```bash
   ./gradlew installDebug
   ```

### Too many logs?
Filter by just OrbStrategyEngine:
```bash
adb logcat | grep "OrbStrategyEngine"
```

### Want to save logs to file?
```bash
adb logcat > trading_logs.txt
# Let it run for a while, then Ctrl+C
```

---

## Expected Log Emojis

| Emoji | Meaning |
|-------|---------|
| 🧪 | Testing/Initialization |
| ✅ | Success |
| 🟢 | Positive action (Buy, Position Open) |
| 🔴 | Negative action (Sell, Loss) |
| 📊 | Data/Stats |
| 📈 | ORB capture |
| 💹 | Price update |
| 🏁 | Position closed |
| ⏹️ | Stopped |
| 🎯 | Target/Trigger |
| ⏰ | Time-related |
| 💰 | Money/Points |
| ❌ | Error |
| ⚠️ | Warning |
| 🚨 | Emergency |

---

## Real vs Mock Mode Logs

### Mock Mode (Debug Build)
```
I/TradingViewModel: ✅ Using MOCK DATA - Market time validation skipped
I/TradingViewModel: 📊 Market Data Source: MockMarketDataSource
```

### Real Mode (Release Build)
```
I/TradingViewModel: ✅ Using REAL DATA - Market time validation enforced
I/TradingViewModel: 📊 Market Data Source: AngelMarketDataSource
```

---

**Last Updated**: 2024-12-14
**For Questions**: Check logcat output against this guide

