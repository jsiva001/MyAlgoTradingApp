package com.trading.orb.data.engine.mock


import com.trading.orb.data.model.*
import java.time.LocalTime

object MockScenarios {

    fun successfulHighBreakout(): Pair<MockMarketDataSource, StrategyConfig> {
        val dataSource = MockMarketDataSource(basePrice = 185.0, volatility = 1.5)

        val config = StrategyConfig(
            instrument = Instrument("NIFTY24DEC22000CE", "NSE", 50, 0.05, "NIFTY 22000 CE"),
            orbStartTime = LocalTime.of(9, 15),
            orbEndTime = LocalTime.of(9, 30),
            breakoutBuffer = 2,
            targetPoints = 15.0,
            stopLossPoints = 8.0
        )

        return dataSource to config
    }

    fun stopLossScenario(): Pair<MockMarketDataSource, StrategyConfig> {
        val dataSource = MockMarketDataSource(basePrice = 189.0, volatility = 1.5)

        val config = StrategyConfig(
            instrument = Instrument("NIFTY24DEC22000CE", "NSE", 50, 0.05),
            orbStartTime = LocalTime.of(9, 15),
            orbEndTime = LocalTime.of(9, 30),
            breakoutBuffer = 2,
            targetPoints = 15.0,
            stopLossPoints = 5.0
        )

        return dataSource to config
    }
}