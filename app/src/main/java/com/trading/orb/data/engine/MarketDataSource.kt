package com.trading.orb.data.engine

import com.trading.orb.data.model.Candle
import com.trading.orb.ui.utils.DEFAULT_CANDLE_INTERVAL
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interface for market data - Mock or Real (Angel One)
 */
interface MarketDataSource {
    fun subscribeLTP(symbol: String): Flow<Double>

    suspend fun getCandles(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime,
        interval: String = DEFAULT_CANDLE_INTERVAL
    ): List<Candle>

    suspend fun getCurrentLTP(symbol: String): Double
    suspend fun isMarketOpen(): Boolean
}