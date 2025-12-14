package com.trading.orb.data.engine.mock

import com.trading.orb.data.engine.MarketDataSource
import com.trading.orb.data.model.Candle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

class MockMarketDataSource(
    private val basePrice: Double = 185.0,
    private val volatility: Double = 0.5,
    private val updateIntervalMs: Long = 1000
) : MarketDataSource {

    private var currentPrice = basePrice
    private val random = Random.Default

    override fun subscribeLTP(symbol: String): Flow<Double> = flow {
        Timber.d("Mock: Subscribing to $symbol")

        while (true) {
            val priceChange = random.nextDouble(-volatility, volatility)
            currentPrice += priceChange

            if (random.nextInt(100) < 20) {
                currentPrice += random.nextDouble(-2.0, 2.0)
            }

            currentPrice = currentPrice.coerceAtLeast(1.0)
            emit(currentPrice)

            delay(updateIntervalMs)
        }
    }

    override suspend fun getCandles(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime,
        interval: String
    ): List<Candle> {
        val candles = mutableListOf<Candle>()
        var time = from
        var price = basePrice

        while (time < to) {
            val open = price
            val high = price + random.nextDouble(0.0, 1.5)
            val low = price - random.nextDouble(0.0, 1.0)
            val close = price + random.nextDouble(-0.5, 0.5)

            candles.add(Candle(time, open, high, low, close, random.nextLong(1000, 10000)))

            price = close
            time = time.plusMinutes(1)
        }

        return candles
    }

    override suspend fun getCurrentLTP(symbol: String) = currentPrice

    override suspend fun isMarketOpen(): Boolean {
        val now = LocalTime.now()
        return now in LocalTime.of(9, 15)..LocalTime.of(15, 30)
    }

    fun setPrice(price: Double) {
        currentPrice = price
    }
}