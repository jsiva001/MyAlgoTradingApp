package com.trading.orb.ui

import com.trading.orb.data.model.*
import java.time.LocalDateTime

object SampleData {

    private val instrument = Instrument("NIFTY 50", "NSE", 50, 0.05, "Nifty 50")

    val positions = listOf(
        Position(
            id = "1",
            instrument = instrument,
            side = OrderSide.BUY,
            quantity = 50,
            entryPrice = 18500.0,
            currentPrice = 18550.0,
            stopLoss = 18450.0,
            target = 18600.0,
            entryTime = LocalDateTime.now().minusMinutes(30)
        ),
        Position(
            id = "2",
            instrument = instrument,
            side = OrderSide.SELL,
            quantity = 50,
            entryPrice = 18600.0,
            currentPrice = 18580.0,
            stopLoss = 18650.0,
            target = 18500.0,
            entryTime = LocalDateTime.now().minusMinutes(10)
        )
    )

    val trades = listOf(
        Trade(
            id = "1",
            instrument = instrument,
            side = OrderSide.BUY,
            quantity = 50,
            entryPrice = 18400.0,
            exitPrice = 18450.0,
            entryTime = LocalDateTime.now().minusHours(2),
            exitTime = LocalDateTime.now().minusHours(1),
            exitReason = ExitReason.TARGET_HIT,
            pnl = 2500.0
        ),
        Trade(
            id = "2",
            instrument = instrument,
            side = OrderSide.SELL,
            quantity = 50,
            entryPrice = 18550.0,
            exitPrice = 18570.0,
            entryTime = LocalDateTime.now().minusMinutes(30),
            exitTime = LocalDateTime.now().minusMinutes(15),
            exitReason = ExitReason.SL_HIT,
            pnl = -1000.0
        )
    )

    val logs = listOf(
        LogEntry(LocalDateTime.now().minusMinutes(5), LogLevel.INFO, "Strategy started"),
        LogEntry(LocalDateTime.now().minusMinutes(4), LogLevel.SUCCESS, "Placed BUY order for Nifty 50"),
        LogEntry(LocalDateTime.now().minusMinutes(3), LogLevel.WARNING, "High volatility detected"),
        LogEntry(LocalDateTime.now().minusMinutes(2), LogLevel.ERROR, "Failed to place SELL order"),
    )

    val appState = AppState(
        tradingMode = TradingMode.PAPER,
        strategyStatus = StrategyStatus.ACTIVE,
        connectionStatus = ConnectionStatus.CONNECTED,
        brokerName = "Zerodha",
        dailyStats = DailyStats(
            totalPnl = 1500.0,
            winRate = 50.0,
            totalTrades = 2,
        ),
        orbLevels = OrbLevels(
            instrument = instrument,
            high = 18520.0,
            low = 18480.0,
            ltp = 18510.0,
            breakoutBuffer = 2
        ),
        activePositions = positions,
        closedTrades = trades,
        logs = logs
    )
}