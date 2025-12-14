package com.trading.orb.data.engine

import com.trading.orb.data.model.*
import java.time.LocalTime

/**
 * Calculates ORB levels from candle data
 */
class OrbLevelsCalculator {

    fun calculateOrbLevels(
        candles: List<Candle>,
        startTime: LocalTime,
        endTime: LocalTime,
        instrument: Instrument,
        breakoutBuffer: Int
    ): OrbLevels? {
        val orbCandles = candles.filter { candle ->
            val candleTime = candle.timestamp.toLocalTime()
            candleTime >= startTime && candleTime < endTime
        }

        if (orbCandles.isEmpty()) return null

        val high = orbCandles.maxOf { it.high }
        val low = orbCandles.minOf { it.low }
        val currentLtp = orbCandles.lastOrNull()?.close ?: 0.0

        return OrbLevels(
            instrument = instrument,
            high = high,
            low = low,
            ltp = currentLtp,
            breakoutBuffer = breakoutBuffer,
            timestamp = java.time.LocalDateTime.now()
        )
    }
}