# 🔄 Mock → Real API Migration Strategy

**Project:** MyAlgoTradeApp (ORB Strategy Algo Trading)  
**Status:** Phase 1 (Mock) ✅ → Phase 2 (Real API) 🔄  
**Target Broker:** Angel One  
**Last Updated:** December 14, 2024

---

## 📋 Migration Overview

```
PHASE 1: MOCK (CURRENT) ✅
├─ Mock WebSocket for LTP
├─ Mock Order Executor
├─ Development & Testing
└─ Build: assembleDebug

           ↓ (When ready)

PHASE 2: REAL API (NEXT) 🔄
├─ Angel One WebSocket API
├─ Angel One REST API
├─ Paper Trading Testing
└─ Build: assembleRelease

           ↓ (After validation)

PHASE 3: PRODUCTION ⏳
├─ Real Trading Account
├─ Live Orders
├─ Real Money
└─ Full Monitoring
```

---

## 🧪 Current Mock Implementation

### What's Working (Phase 1 ✅)

```
┌──────────────────────────────────────────┐
│  MOCK MARKET DATA SOURCE                 │
├──────────────────────────────────────────┤
│  ✅ Simulates WebSocket LTP updates      │
│  ✅ Generates realistic prices           │
│  ✅ Configurable volatility              │
│  ✅ Fixed intervals (1000ms default)     │
│  ✅ No network calls                     │
│  ✅ Instant data delivery                │
│  ✅ Perfect for testing                  │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│  MOCK ORDER EXECUTOR                     │
├──────────────────────────────────────────┤
│  ✅ Simulates order execution            │
│  ✅ Configurable delay (500ms)           │
│  ✅ Generates order IDs                  │
│  ✅ No broker connectivity needed        │
│  ✅ Instant feedback                     │
│  ✅ Failure scenario simulation          │
│  ✅ Perfect for testing logic            │
└──────────────────────────────────────────┘
```

### Code Structure (Current)

```kotlin
// interface (same for both mock and real)
interface MarketDataSource {
    fun subscribeLTP(symbol: String): Flow<Double>
    suspend fun getCandles(...): List<Candle>
    // ... other methods
}

// CURRENT: Mock implementation
class MockMarketDataSource : MarketDataSource {
    override fun subscribeLTP(symbol: String): Flow<Double> = flow {
        while (isActive) {
            emit(generatePrice())  // Fake price
            delay(1000)
        }
    }
}

// FUTURE: Real implementation (same interface!)
class AngelMarketDataSource : MarketDataSource {
    override fun subscribeLTP(symbol: String): Flow<Double> = callbackFlow {
        // Connect to Angel WebSocket
        // Subscribe to real ticks
        // Emit real prices
    }
}
```

---

## 🌐 Angel One API Integration (Phase 2)

### Prerequisites

Before integrating Angel One API, you'll need:

1. **Angel One Account**
   - Trader account with Angel One
   - API credentials (API Key, Access Token)
   - Paper trading enabled

2. **Angel SDK**
   ```gradle
   // Add to build.gradle.kts
   implementation("com.angelbroking:smartapi:1.x.x")
   ```

3. **API Documentation**
   - Angel One WebSocket API docs
   - REST API documentation
   - Token refresh mechanism

---

### Step 1: Create AngelMarketDataSource

**File:** `app/src/main/java/com/trading/orb/data/engine/live/AngelMarketDataSource.kt`

```kotlin
package com.trading.orb.data.engine.live

import com.trading.orb.data.engine.MarketDataSource
import com.trading.orb.data.model.Candle
import com.smartapi.SmartConnect
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.time.LocalDateTime

class AngelMarketDataSource(
    private val apiKey: String,
    private val accessToken: String
) : MarketDataSource {

    private val smartAPI = SmartConnect(apiKey).apply {
        setAccessToken(accessToken)
    }

    override fun subscribeLTP(symbol: String): Flow<Double> = callbackFlow {
        Timber.d("Angel: Connecting WebSocket for $symbol")
        
        try {
            // Connect to Angel WebSocket
            val webSocket = smartAPI.connectWebSocket()
            
            // Subscribe to symbol LTP
            webSocket.subscribe(
                mode = "LTP",
                token = getTokenForSymbol(symbol),
                exch = "NFO"
            ) { tick ->
                // Called every time new tick arrives
                val ltp = tick.lastTradedPrice
                Timber.v("Angel LTP: $symbol = $ltp")
                trySend(ltp)
            }
            
            // Cleanup on cancel
            awaitClose {
                webSocket.unsubscribe(token = getTokenForSymbol(symbol))
                webSocket.close()
                Timber.d("Angel: WebSocket closed for $symbol")
            }
        } catch (e: Exception) {
            Timber.e(e, "Angel: WebSocket error for $symbol")
            close(e)
        }
    }

    override suspend fun getCandles(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime,
        interval: String
    ): List<Candle> {
        return try {
            Timber.d("Angel: Fetching candles for $symbol")
            
            val response = smartAPI.getCandleData(
                exchange = "NFO",
                symbolToken = getTokenForSymbol(symbol),
                interval = interval,  // "15min", "1h", etc.
                fromDate = from.toString(),
                toDate = to.toString()
            )
            
            // Convert Angel format to our Candle model
            response.data.map { angelCandle ->
                Candle(
                    timestamp = parseDateTime(angelCandle[0]),
                    open = angelCandle[1].toDouble(),
                    high = angelCandle[2].toDouble(),
                    low = angelCandle[3].toDouble(),
                    close = angelCandle[4].toDouble(),
                    volume = angelCandle[5].toLong()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Angel: Error fetching candles for $symbol")
            emptyList()
        }
    }

    override suspend fun getCurrentLTP(symbol: String): Double {
        return try {
            val quote = smartAPI.getQuote(
                mode = "LTP",
                exchangeTokens = listOf(getTokenForSymbol(symbol))
            )
            quote.lastPrice
        } catch (e: Exception) {
            Timber.e(e, "Angel: Error getting LTP for $symbol")
            0.0
        }
    }

    override suspend fun isMarketOpen(): Boolean {
        return try {
            val status = smartAPI.getMarketStatus()
            status.contains("open", ignoreCase = true)
        } catch (e: Exception) {
            Timber.e(e, "Angel: Error checking market status")
            false
        }
    }

    private fun getTokenForSymbol(symbol: String): String {
        // Map symbol to Angel token
        // Maintain a cache of symbol → token mappings
        return symbolToTokenMap[symbol] 
            ?: throw IllegalArgumentException("Token not found for $symbol")
    }

    companion object {
        // Build this map during app initialization
        private val symbolToTokenMap = mutableMapOf(
            "BANKNIFTY24DEC22000CE" to "29015061",
            "FINIFTY24DEC21000PE" to "31094009",
            // ... add more symbols as needed
        )
    }
}
```

---

### Step 2: Create AngelOrderExecutor

**File:** `app/src/main/java/com/trading/orb/data/engine/live/AngelOrderExecutor.kt`

```kotlin
package com.trading.orb.data.engine.live

import com.trading.orb.data.engine.OrderExecutor
import com.trading.orb.data.model.Order, OrderType
import com.smartapi.SmartConnect
import timber.log.Timber

class AngelOrderExecutor(
    private val apiKey: String,
    private val accessToken: String
) : OrderExecutor {

    private val smartAPI = SmartConnect(apiKey).apply {
        setAccessToken(accessToken)
    }

    override suspend fun placeOrder(
        symbol: String,
        quantity: Int,
        price: Double,
        orderType: OrderType
    ): Order {
        return try {
            Timber.i("Angel: Placing $orderType order for $symbol at ₹$price x$quantity")
            
            val response = smartAPI.placeOrder(
                variety = "NORMAL",
                tradingsymbol = symbol,
                symboltoken = getTokenForSymbol(symbol),
                transactiontype = orderType.toAngelType(),  // "BUY" or "SELL"
                exchange = "NFO",
                ordertype = "LIMIT",
                producttype = "INTRADAY",
                duration = "DAY",
                price = price.toString(),
                quantity = quantity.toString(),
                triggerprice = "0",
                disclosedquantity = "0",
                amo = "NO"
            )
            
            // Parse response
            val orderId = response.data.orderid
            Timber.d("Angel: Order placed successfully - ID: $orderId")
            
            Order(
                orderId = orderId,
                symbol = symbol,
                quantity = quantity,
                price = price,
                orderType = orderType,
                status = OrderStatus.EXECUTED,
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Angel: Failed to place order for $symbol")
            throw OrderExecutionException("Order failed: ${e.message}", e)
        }
    }

    override suspend fun modifyOrder(
        orderId: String,
        newPrice: Double,
        newQuantity: Int?
    ): Order {
        return try {
            Timber.i("Angel: Modifying order $orderId to ₹$newPrice")
            
            smartAPI.modifyOrder(
                variety = "NORMAL",
                orderid = orderId,
                ordertype = "LIMIT",
                price = newPrice.toString(),
                quantity = newQuantity?.toString() ?: "0",
                triggerprice = "0",
                disclosedquantity = "0"
            )
            
            Timber.d("Angel: Order $orderId modified successfully")
            // Return updated order details
            // (fetch from Angel or construct from response)
        } catch (e: Exception) {
            Timber.e(e, "Angel: Failed to modify order $orderId")
            throw OrderExecutionException("Modify failed: ${e.message}", e)
        }
    }

    override suspend fun cancelOrder(orderId: String): Boolean {
        return try {
            Timber.i("Angel: Cancelling order $orderId")
            
            val response = smartAPI.cancelOrder(
                variety = "NORMAL",
                orderid = orderId
            )
            
            Timber.d("Angel: Order $orderId cancelled successfully")
            true
        } catch (e: Exception) {
            Timber.e(e, "Angel: Failed to cancel order $orderId")
            false
        }
    }

    private fun getTokenForSymbol(symbol: String): String {
        return symbolToTokenMap[symbol]
            ?: throw IllegalArgumentException("Token not found for $symbol")
    }

    companion object {
        private val symbolToTokenMap = mutableMapOf(
            "BANKNIFTY24DEC22000CE" to "29015061",
            "FINIFTY24DEC21000PE" to "31094009",
        )
    }
}

private fun OrderType.toAngelType(): String {
    return when (this) {
        OrderType.BUY -> "BUY"
        OrderType.SELL -> "SELL"
    }
}

class OrderExecutionException(message: String, cause: Throwable?) : Exception(message, cause)
```

---

### Step 3: Update AppModule to Use Real APIs

**File:** `app/src/main/java/com/trading/orb/di/AppModule.kt`

```kotlin
// Add imports
import com.trading.orb.data.engine.live.AngelMarketDataSource
import com.trading.orb.data.engine.live.AngelOrderExecutor

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideMarketDataSource(
        @Named("use_mock") useMock: Boolean,
        @Named("api_key") apiKey: String,
        @Named("access_token") accessToken: String
    ): MarketDataSource {
        return if (useMock) {
            Timber.d("🧪 [DEBUG] Using MOCK Market Data Source")
            MockMarketDataSource(
                basePrice = 185.0,
                volatility = 0.5,
                updateIntervalMs = 1000
            )
        } else {
            Timber.d("🌐 [PRODUCTION] Using REAL Angel Market Data Source")
            AngelMarketDataSource(apiKey, accessToken)  // ← Now real!
        }
    }

    @Provides
    @Singleton
    fun provideOrderExecutor(
        @Named("use_mock") useMock: Boolean,
        @Named("api_key") apiKey: String,
        @Named("access_token") accessToken: String
    ): OrderExecutor {
        return if (useMock) {
            Timber.d("🧪 [DEBUG] Using MOCK Order Executor")
            MockOrderExecutor(
                executionDelayMs = 500,
                failureRate = 0
            )
        } else {
            Timber.d("🌐 [PRODUCTION] Using REAL Angel Order Executor")
            AngelOrderExecutor(apiKey, accessToken)  // ← Now real!
        }
    }
}
```

---

## 📝 Checklist for Integration

### Pre-Integration
- [ ] Angel One broker account created
- [ ] API credentials obtained (API Key, Access Token)
- [ ] Paper trading enabled
- [ ] Angel SDK documentation reviewed
- [ ] Symbol-to-token mapping prepared

### Integration
- [ ] AngelMarketDataSource implemented
- [ ] AngelOrderExecutor implemented
- [ ] AppModule updated with real API providers
- [ ] Error handling added for network failures
- [ ] Token refresh mechanism implemented
- [ ] Timber logging added for debugging
- [ ] Build configured with environment variables

### Testing
- [ ] Paper trading test with mock data
- [ ] Single trade execution test
- [ ] Target/Stop Loss hit test
- [ ] Position closing test
- [ ] Error scenario handling
- [ ] Network disconnection recovery
- [ ] Trade history logging

### Production Readiness
- [ ] All error cases handled
- [ ] Retry logic for failed orders
- [ ] Market hours checking
- [ ] Position tracking accuracy
- [ ] P&L calculation verification
- [ ] Trade history persistence
- [ ] Monitoring/alerting setup

---

## 🔐 Security Considerations

### API Credentials

**NEVER hardcode in source code!**

```gradle
// WRONG ❌
buildConfigField("String", "API_KEY", "\"abc123def456\"")

// RIGHT ✅
buildConfigField(
    "String",
    "API_KEY",
    "\"${System.getenv("ANGEL_API_KEY") ?: ""}\""
)
```

### Environment Variables Setup

```bash
# For local development
export ANGEL_API_KEY="your-api-key"
export ANGEL_ACCESS_TOKEN="your-access-token"

# For CI/CD (GitHub Actions example)
# Set in repository secrets
```

### Token Management

```kotlin
// Token expiry handling
if (token.isExpired()) {
    val newToken = refreshToken()
    smartAPI.setAccessToken(newToken)
}

// Automatic refresh
fun refreshToken(): String {
    val response = smartAPI.generateToken()
    return response.accessToken
}
```

---

## 🧪 Testing the Migration

### Stage 1: Debug Build (Still Mock)
```bash
./gradlew assembleDebug

# Logs show:
# 🧪 [DEBUG] Using MOCK Market Data Source
# 🧪 [DEBUG] Using MOCK Order Executor
```

### Stage 2: Release Build (Real API)
```bash
export ANGEL_API_KEY="your-key"
export ANGEL_ACCESS_TOKEN="your-token"
./gradlew assembleRelease

# Logs show:
# 🌐 [PRODUCTION] Using REAL Angel Market Data Source
# 🌐 [PRODUCTION] Using REAL Angel Order Executor
```

### Stage 3: Gradual Rollout
```
Day 1: Single symbol, limited trading hours
Day 2: Expand to multiple symbols
Day 3: Full strategy parameters
Day 4-7: Monitor and validate
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: Token Expiry
```kotlin
// Solution: Implement token refresh
val tokenExpiryTime = LocalDateTime.now().plusHours(24)
val shouldRefresh = LocalDateTime.now().isAfter(tokenExpiryTime)
if (shouldRefresh) {
    refreshToken()
}
```

### Issue 2: WebSocket Disconnection
```kotlin
// Solution: Implement reconnection logic
fun reconnectWebSocket() {
    var retries = 0
    while (retries < 3) {
        try {
            webSocket = smartAPI.connectWebSocket()
            break
        } catch (e: Exception) {
            retries++
            delay(1000 * retries)  // Exponential backoff
        }
    }
}
```

### Issue 3: Order Rejection
```kotlin
// Solution: Better error handling
try {
    placeOrder(...)
} catch (e: OrderRejectedException) {
    Timber.e("Order rejected: ${e.reason}")
    // Inform user, log, retry
}
```

### Issue 4: Wrong Symbol Token
```kotlin
// Solution: Dynamic token lookup
fun getTokenForSymbol(symbol: String): String {
    // First try cache
    symbolTokenMap[symbol]?.let { return it }
    
    // If not cached, fetch from Angel
    val token = smartAPI.searchScrip(symbol)?.token
    if (token != null) {
        symbolTokenMap[symbol] = token
        return token
    }
    
    throw IllegalArgumentException("Token not found for $symbol")
}
```

---

## 📊 Performance Metrics

### Mock Mode (Current)
```
LTP Updates: Every 1000ms
Latency: < 1ms (in-memory)
Order Execution: ~500ms
Network Calls: 0
Data: Predictable
```

### Real Mode (After Integration)
```
LTP Updates: Every 100ms+ (real market)
Latency: 100-500ms (network dependent)
Order Execution: 500ms-2s (broker dependent)
Network Calls: Yes (WebSocket + REST)
Data: Live market data
```

---

## 🎯 Migration Timeline

**Week 1: Preparation**
- Set up Angel One account
- Get API credentials
- Review Angel documentation

**Week 2: Development**
- Implement AngelMarketDataSource
- Implement AngelOrderExecutor
- Add error handling

**Week 3: Testing**
- Paper trading validation
- Edge case testing
- Performance testing

**Week 4: Production**
- Gradual rollout
- Monitoring
- Production support

---

## 📚 Reference Resources

- **Angel One API Docs:** https://smartapi.angelone.in/
- **WebSocket Implementation:** Angel SDK examples
- **REST API:** Order placement, modification, cancellation
- **Error Codes:** Angel One error documentation
- **Symbol Tokens:** Angel scrip list

---

## ✅ Success Criteria

The migration is successful when:

✅ Mock mode still works (backward compatibility)  
✅ Real mode connects to Angel API  
✅ Orders execute on both mock and real  
✅ Positions close correctly  
✅ Trade history logs accurately  
✅ P&L calculations match  
✅ No code changes needed in UI  
✅ Easy to switch between mock and real  

---

## 🎓 Summary

This migration strategy allows you to:

1. **Develop & Test** with mock data (fast, reliable)
2. **Validate Strategy** before real money
3. **Integrate Real API** without changing UI code
4. **Deploy** with confidence to production

The beauty: **Same code, different data source!** 🚀

---

**Next Step:** When ready, follow the integration checklist and implement AngelMarketDataSource and AngelOrderExecutor.

For questions, refer to Angel One documentation and the implementation examples above.
