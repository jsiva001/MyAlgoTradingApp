package com.trading.orb.ui.state

import java.time.LocalDateTime

/**
 * Dashboard Screen UI Models
 */

data class QuickStatsUiModel(
    val totalProfit: Double = 0.0,
    val totalReturn: Double = 0.0,
    val winRate: Double = 0.0,
    val totalTrades: Int = 0
)

data class StrategyStatusUiModel(
    val isActive: Boolean = false,
    val strategyName: String = "",
    val uptime: String = "00:00:00",
    val lastUpdated: String = ""
)

data class OrbLevelsUiModel(
    val symbol: String = "",
    val openPrice: Double = 0.0,
    val orbHigh: Double = 0.0,
    val orbLow: Double = 0.0,
    val lastPrice: Double = 0.0,
    val deviation: Double = 0.0
)

data class RecentTradeUiModel(
    val tradeId: String = "",
    val symbol: String = "",
    val type: String = "", // BUY, SELL
    val quantity: Int = 0,
    val entryPrice: Double = 0.0,
    val exitPrice: Double? = null,
    val profitLoss: Double = 0.0,
    val status: String = "", // OPEN, CLOSED
    val timestamp: String = ""
)

data class PerformanceMetricsUiModel(
    val dailyProfit: Double = 0.0,
    val monthlyProfit: Double = 0.0,
    val bestTrade: Double = 0.0,
    val worstTrade: Double = 0.0,
    val averageTrade: Double = 0.0,
    val profitFactor: Double = 0.0
)

/**
 * Strategy Config Screen UI Models
 */

data class TradingHoursUiModel(
    val startTime: String = "09:30",
    val endTime: String = "16:00",
    val timeZone: String = "EST",
    val mondayEnabled: Boolean = true,
    val tuesdayEnabled: Boolean = true,
    val wednesdayEnabled: Boolean = true,
    val thursdayEnabled: Boolean = true,
    val fridayEnabled: Boolean = true,
    val saturdayEnabled: Boolean = false,
    val sundayEnabled: Boolean = false
)

data class AdvancedSettingsUiModel(
    val useTrailingStop: Boolean = false,
    val trailingStopPercent: Float = 0.5f,
    val maxSlippage: Float = 0.1f,
    val enablePartialExit: Boolean = false,
    val partialExitPercent: Float = 50f,
    val useMarketOrders: Boolean = false,
    val enableAutoRebalance: Boolean = false,
    val rebalanceInterval: Int = 60 // minutes
)

/**
 * Positions Screen UI Models
 */

data class PositionUiModel(
    val positionId: String = "",
    val symbol: String = "",
    val type: String = "", // LONG, SHORT
    val quantity: Int = 0,
    val entryPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val openTime: String = "",
    val riskLevel: String = "MEDIUM" // LOW, MEDIUM, HIGH
)

/**
 * Risk Screen UI Models
 */

data class RiskMetricsUiModel(
    val portfolioValue: Double = 0.0,
    val dayDrawdown: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val sharpeRatio: Double = 0.0,
    val exposurePercent: Double = 0.0,
    val leverageRatio: Double = 1.0
)

data class RiskLevelUiModel(
    val symbol: String = "",
    val riskLevel: String = "", // LOW, MEDIUM, HIGH
    val exposure: Double = 0.0,
    val concentration: Double = 0.0
)

data class RiskLimitsUiModel(
    val dailyLossLimit: Double = 1000.0,
    val weeklyLossLimit: Double = 5000.0,
    val maxDrawdownLimit: Double = 20.0,
    val maxExposureLimit: Double = 50.0,
    val maxPositionSize: Double = 10000.0
)

data class RiskAlertUiModel(
    val alertId: String = "",
    val type: String = "", // DRAWDOWN, EXPOSURE, LOSS
    val severity: String = "", // INFO, WARNING, CRITICAL
    val message: String = "",
    val value: Double = 0.0,
    val threshold: Double = 0.0,
    val timestamp: String = ""
)

/**
 * Trade History Screen UI Models
 */

data class TradeHistoryUiModel(
    val tradeId: String = "",
    val symbol: String = "",
    val tradeType: String = "", // BUY, SELL
    val quantity: Int = 0,
    val entryPrice: Double = 0.0,
    val exitPrice: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0,
    val duration: String = "", // e.g., "2h 30m"
    val status: String = "", // PROFIT, LOSS, BREAKEVEN
    val entryTime: String = "",
    val exitTime: String = "",
    val reason: String = "" // WHY_SOLD
)

data class TradeStatisticsUiModel(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val winRate: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val netProfit: Double = 0.0,
    val averageWin: Double = 0.0,
    val averageLoss: Double = 0.0,
    val profitFactor: Double = 0.0,
    val expectancy: Double = 0.0
)

data class DateRangeUiModel(
    val startDate: String = "",
    val endDate: String = "",
    val rangeType: String = "ALL" // ALL, TODAY, WEEK, MONTH, CUSTOM
)

/**
 * Live Logs Screen UI Models
 */

data class LogEntryUiModel(
    val logId: String = "",
    val level: String = "", // INFO, DEBUG, WARNING, ERROR
    val message: String = "",
    val timestamp: String = "",
    val source: String = "",
    val details: String? = null,
    val isRead: Boolean = true
)

/**
 * More Screen UI Models
 */

data class UserSettingsUiModel(
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoStartStrategy: Boolean = false,
    val emergencyStopOnExit: Boolean = true,
    val logLevel: String = "INFO",
    val dateFormat: String = "MM/dd/yyyy",
    val timeFormat: String = "HH:mm:ss"
)

data class AboutInfoUiModel(
    val appVersion: String = "1.0.0",
    val buildNumber: String = "100",
    val buildDate: String = "",
    val developer: String = "MyAlgoTrade",
    val website: String = "www.myalgotrade.com",
    val supportEmail: String = "support@myalgotrade.com"
)
