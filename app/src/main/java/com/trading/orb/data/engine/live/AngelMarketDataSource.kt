/*
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
) : MarketDataSource {  // ← Same interface as Mock!

    private val smartAPI = SmartConnect(apiKey)

    init {
        smartAPI.setAccessToken(accessToken)
    }

    */
/**
     * 🔥 THIS IS THE KEY METHOD
     * Mock version: Generates fake prices
     * Real version: Connects to Angel WebSocket
     *
     * Both return Flow<Double> - So engine doesn't know the difference!
     *//*

    override fun subscribeLTP(symbol: String): Flow<Double> = callbackFlow {
        Timber.i("Angel: Subscribing to $symbol WebSocket")

        // Connect to Angel One WebSocket
        val webSocketClient = smartAPI.connectWebSocket()

        // Subscribe to symbol
        webSocketClient.subscribe(symbol, "LTP") { tick ->
            // When tick received from Angel, emit to flow
            val ltp = tick.lastTradedPrice
            Timber.v("Angel LTP: $ltp")
            trySend(ltp)  // ← Send to engine (just like Mock does!)
        }

        // Cleanup when flow is cancelled
        awaitClose {
            webSocketClient.unsubscribe(symbol)
            webSocketClient.close()
            Timber.i("Angel: WebSocket closed for $symbol")
        }
    }

    override suspend fun getCandles(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime,
        interval: String
    ): List<Candle> {
        // Call Angel historical data API
        val response = smartAPI.getCandleData(
            exchange = "NFO",
            symboltoken = getTokenForSymbol(symbol),
            interval = interval,
            fromdate = from.toString(),
            todate = to.toString()
        )

        // Convert Angel format to our Candle model
        return response.data.map { angelCandle ->
            Candle(
                timestamp = parseDateTime(angelCandle[0]),
                open = angelCandle[1].toDouble(),
                high = angelCandle[2].toDouble(),
                low = angelCandle[3].toDouble(),
                close = angelCandle[4].toDouble(),
                volume = angelCandle[5].toLong()
            )
        }
    }

    override suspend fun getCurrentLTP(symbol: String): Double {
        val quote = smartAPI.getQuote(
            exchange = "NFO",
            symboltoken = getTokenForSymbol(symbol)
        )
        return quote.ltp
    }

    override suspend fun isMarketOpen(): Boolean {
        // Check Angel market status
        val status = smartAPI.marketStatus()
        return status == "OPEN"
    }

    // Helper: Get token for symbol (Angel uses tokens)
    private fun getTokenForSymbol(symbol: String): String {
        // Implement symbol → token mapping
        // You can maintain a cache or call Angel search API
        return symbolTokenCache[symbol] ?: throw Exception("Token not found for $symbol")
    }

    companion object {
        private val symbolTokenCache = mutableMapOf<String, String>()
    }
}*/
