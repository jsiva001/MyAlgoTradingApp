package com.trading.orb.data.model

import java.time.LocalDateTime
import java.time.LocalTime
import com.trading.orb.ui.utils.*

/**
 * Helper function to convert time string (HH:mm) to LocalTime
 */
private fun parseTime(timeString: String): LocalTime {
    val parts = timeString.split(":")
    return LocalTime.of(parts[0].toInt(), parts[1].toInt())
}

/**
 * Trading mode enum
 */
enum class TradingMode {
    PAPER,
    LIVE
}

/**
 * Strategy status enum
 */
enum class StrategyStatus {
    INACTIVE,
    ACTIVE,
    PAUSED,
    ERROR
}

/**
 * Order side enum
 */
enum class OrderSide {
    BUY,
    SELL
}

/**
 * Trade exit reason
 */
enum class ExitReason {
    TARGET_HIT,
    SL_HIT,
    TIME_EXIT,
    MANUAL,
    MANUAL_EXIT,      // Manual close from UI
    EMERGENCY_EXIT,   // Emergency stop button
    CIRCUIT_BREAKER
}

/**
 * Order type
 */
enum class OrderType {
    MARKET,
    LIMIT
}

/**
 * Connection status
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}

/**
 * Instrument/Symbol data model
 */
data class Instrument(
    val symbol: String,
    val exchange: String,
    val lotSize: Int,
    val tickSize: Double,
    val displayName: String = symbol
)

/**
 * ORB levels for the 15-minute candle
 */
data class OrbLevels(
    val instrument: Instrument,
    val high: Double,
    val low: Double,
    val ltp: Double,
    val breakoutBuffer: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isOrbCaptured: Boolean = false // True only after ORB window completes
) {
    val buyTrigger: Double get() = high + (breakoutBuffer * instrument.tickSize)
    val sellTrigger: Double get() = low - (breakoutBuffer * instrument.tickSize)
}

/**
 * Active position data model
 */
data class Position(
    val id: String,
    val instrument: Instrument,
    val side: OrderSide,
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val target: Double,
    val entryTime: LocalDateTime,
    val status: String = "OPEN"
) {
    val pnl: Double get() = when (side) {
        OrderSide.BUY -> (currentPrice - entryPrice) * quantity
        OrderSide.SELL -> (entryPrice - currentPrice) * quantity
    }
    
    val pnlPercentage: Double get() = (pnl / (entryPrice * quantity)) * 100
    
    val isProfit: Boolean get() = pnl > 0
}

/**
 * Closed trade data model
 */
data class Trade(
    val id: String,
    val instrument: Instrument,
    val side: OrderSide,
    val quantity: Int,
    val entryPrice: Double,
    val exitPrice: Double,
    val entryTime: LocalDateTime,
    val exitTime: LocalDateTime,
    val exitReason: ExitReason,
    val pnl: Double,
    val charges: Double = 0.0
) {
    val netPnl: Double get() = pnl - charges
    val pnlPercentage: Double get() = (pnl / (entryPrice * quantity)) * 100
    val isProfit: Boolean get() = netPnl > 0
    val duration: Long get() = java.time.Duration.between(entryTime, exitTime).toMinutes()
}

/**
 * Strategy configuration
 */
data class StrategyConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "ORB 15-Min",
    val instrument: Instrument,
    val orbStartTime: LocalTime = parseTime(ORB_START_TIME),
    val orbEndTime: LocalTime = parseTime(ORB_END_TIME),
    val autoExitTime: LocalTime = parseTime(AUTO_EXIT_TIME_DEFAULT),
    val noReentryTime: LocalTime = parseTime(NO_REENTRY_TIME_DEFAULT),
    val breakoutBuffer: Int = DEFAULT_BREAKOUT_BUFFER,
    val orderType: OrderType = OrderType.MARKET,
    val targetPoints: Double = DEFAULT_TARGET_POINTS.toDouble(),
    val stopLossPoints: Double = DEFAULT_STOP_LOSS_POINTS.toDouble(),
    val trailingStop: Boolean = false,
    val lotSize: Int = DEFAULT_LOT_SIZE,
    val maxPositions: Int = DEFAULT_MAX_POSITION,
    val enableAutoExit: Boolean = true,
    val enabled: Boolean = false
)

/**
 * Risk settings
 */
data class RiskSettings(
    val maxDailyLoss: Double = 100.0,
    val currentDailyLoss: Double = 0.0,
    val maxDailyTrades: Int = 10,
    val currentDailyTrades: Int = 0,
    val maxPositions: Int = 3,
    val maxPerInstrument: Int = 1,
    val circuitBreakerLossPercent: Double = 5.0,
    val coolDownMinutes: Int = 30,
    val orderThrottlePerMinute: Int = 10
) {
    val dailyLossPercentage: Double get() = 
        if (maxDailyLoss > 0) (currentDailyLoss / maxDailyLoss) * 100 else 0.0
    
    val tradesPercentage: Double get() = 
        if (maxDailyTrades > 0) (currentDailyTrades.toDouble() / maxDailyTrades) * 100 else 0.0
    
    val isCircuitBreakerTriggered: Boolean get() = 
        dailyLossPercentage >= circuitBreakerLossPercent
}

/**
 * Daily statistics
 */
data class DailyStats(
    val totalPnl: Double = 0.0,
    val activePositions: Int = 0,
    val winRate: Double = 0.0,
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val avgWin: Double = 0.0,
    val avgLoss: Double = 0.0,
    val largestWin: Double = 0.0,
    val largestLoss: Double = 0.0
)

/**
 * App state
 */
data class AppState(
    val tradingMode: TradingMode = TradingMode.PAPER,
    val strategyStatus: StrategyStatus = StrategyStatus.INACTIVE,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val brokerName: String = "",
    val dailyStats: DailyStats = DailyStats(),
    val riskSettings: RiskSettings = RiskSettings(),
    val activePositions: List<Position> = emptyList(),
    val closedTrades: List<Trade> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val orbLevels: OrbLevels? = null,
    val strategyConfig: StrategyConfig? = null
)

/**
 * Log entry
 */
data class LogEntry(
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val message: String,
    val data: Map<String, Any>? = null
)

enum class LogLevel {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * Backtest result
 */
data class BacktestResult(
    val id: String,
    val strategyConfig: StrategyConfig,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val trades: List<Trade>,
    val totalPnl: Double,
    val winRate: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val sharpeRatio: Double,
    val expectancy: Double
)

// ADD TO: data/model/Models.kt

// ===== NEW MODELS FOR ORB ENGINE =====

/**
 * Candle data (OHLC)
 */
data class Candle(
    val timestamp: LocalDateTime,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * Strategy events from engine
 */
sealed class StrategyEvent {
    data class Started(val config: StrategyConfig) : StrategyEvent()
    object Stopped : StrategyEvent()
    data class OrbCaptured(val levels: OrbLevels) : StrategyEvent()
    data class PriceUpdate(val ltp: Double) : StrategyEvent()
    data class PositionOpened(val position: Position) : StrategyEvent()
    data class PositionUpdate(val position: Position) : StrategyEvent()
    data class PositionClosed(val trade: Trade) : StrategyEvent()
    data class OrderFailed(val message: String) : StrategyEvent()
    data class Error(val message: String) : StrategyEvent()
    object RiskLimitReached : StrategyEvent()
}

/**
 * Order response from broker
 */
data class OrderResponse(
    val orderId: String,
    val status: String,
    val message: String,
    val price: Double? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Mock tick data (for testing)
 */
data class TickData(
    val token: String,
    val symbol: String,
    val ltp: Double,
    val volume: Long,
    val timestamp: Long,
    val bidPrice: Double,
    val askPrice: Double,
    val bidQty: Int,
    val askQty: Int
)

