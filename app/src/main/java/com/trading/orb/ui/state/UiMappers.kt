package com.trading.orb.ui.state

/**
 * UI Model Mappers
 * Convert domain/data models to UI models
 */

/**
 * Dashboard Screen Mappers
 */
fun mapToQuickStatsUiModel(
    totalProfit: Double = 0.0,
    totalReturn: Double = 0.0,
    winRate: Double = 0.0,
    totalTrades: Int = 0
): QuickStatsUiModel {
    return QuickStatsUiModel(
        totalProfit = totalProfit,
        totalReturn = totalReturn,
        winRate = winRate,
        totalTrades = totalTrades
    )
}

fun mapToStrategyStatusUiModel(
    isActive: Boolean = false,
    strategyName: String = "",
    uptime: String = "00:00:00",
    lastUpdated: String = ""
): StrategyStatusUiModel {
    return StrategyStatusUiModel(
        isActive = isActive,
        strategyName = strategyName,
        uptime = uptime,
        lastUpdated = lastUpdated
    )
}

fun mapToOrbLevelsUiModel(
    symbol: String = "",
    openPrice: Double = 0.0,
    orbHigh: Double = 0.0,
    orbLow: Double = 0.0,
    lastPrice: Double = 0.0
): OrbLevelsUiModel {
    val deviation = if (orbHigh > 0) {
        ((lastPrice - openPrice) / openPrice) * 100
    } else {
        0.0
    }
    return OrbLevelsUiModel(
        symbol = symbol,
        openPrice = openPrice,
        orbHigh = orbHigh,
        orbLow = orbLow,
        lastPrice = lastPrice,
        deviation = deviation
    )
}

fun mapToRecentTradeUiModel(
    tradeId: String = "",
    symbol: String = "",
    type: String = "",
    quantity: Int = 0,
    entryPrice: Double = 0.0,
    exitPrice: Double? = null,
    profitLoss: Double = 0.0,
    status: String = "",
    timestamp: String = ""
): RecentTradeUiModel {
    return RecentTradeUiModel(
        tradeId = tradeId,
        symbol = symbol,
        type = type,
        quantity = quantity,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        profitLoss = profitLoss,
        status = status,
        timestamp = timestamp
    )
}

fun mapToPerformanceMetricsUiModel(
    dailyProfit: Double = 0.0,
    monthlyProfit: Double = 0.0,
    bestTrade: Double = 0.0,
    worstTrade: Double = 0.0,
    averageTrade: Double = 0.0,
    profitFactor: Double = 0.0
): PerformanceMetricsUiModel {
    return PerformanceMetricsUiModel(
        dailyProfit = dailyProfit,
        monthlyProfit = monthlyProfit,
        bestTrade = bestTrade,
        worstTrade = worstTrade,
        averageTrade = averageTrade,
        profitFactor = profitFactor
    )
}

/**
 * Positions Screen Mappers
 */
fun mapToPositionUiModel(
    positionId: String = "",
    symbol: String = "",
    type: String = "",
    quantity: Int = 0,
    entryPrice: Double = 0.0,
    currentPrice: Double = 0.0,
    stopLoss: Double? = null,
    takeProfit: Double? = null,
    openTime: String = ""
): PositionUiModel {
    val profitLoss = (currentPrice - entryPrice) * quantity
    val profitLossPercent = if (entryPrice > 0) {
        ((currentPrice - entryPrice) / entryPrice) * 100
    } else {
        0.0
    }
    val riskLevel = when {
        profitLossPercent >= 5 -> "LOW"
        profitLossPercent >= -5 -> "MEDIUM"
        else -> "HIGH"
    }

    return PositionUiModel(
        positionId = positionId,
        symbol = symbol,
        type = type,
        quantity = quantity,
        entryPrice = entryPrice,
        currentPrice = currentPrice,
        profitLoss = profitLoss,
        profitLossPercent = profitLossPercent,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        openTime = openTime,
        riskLevel = riskLevel
    )
}

/**
 * Risk Screen Mappers
 */
fun mapToRiskMetricsUiModel(
    portfolioValue: Double = 0.0,
    dayDrawdown: Double = 0.0,
    maxDrawdown: Double = 0.0,
    sharpeRatio: Double = 0.0,
    exposurePercent: Double = 0.0,
    leverageRatio: Double = 1.0
): RiskMetricsUiModel {
    return RiskMetricsUiModel(
        portfolioValue = portfolioValue,
        dayDrawdown = dayDrawdown,
        maxDrawdown = maxDrawdown,
        sharpeRatio = sharpeRatio,
        exposurePercent = exposurePercent,
        leverageRatio = leverageRatio
    )
}

fun mapToRiskAlertUiModel(
    alertId: String = "",
    type: String = "",
    severity: String = "",
    message: String = "",
    value: Double = 0.0,
    threshold: Double = 0.0,
    timestamp: String = ""
): RiskAlertUiModel {
    return RiskAlertUiModel(
        alertId = alertId,
        type = type,
        severity = severity,
        message = message,
        value = value,
        threshold = threshold,
        timestamp = timestamp
    )
}

/**
 * Trade History Screen Mappers
 */
fun mapToTradeHistoryUiModel(
    tradeId: String = "",
    symbol: String = "",
    tradeType: String = "",
    quantity: Int = 0,
    entryPrice: Double = 0.0,
    exitPrice: Double = 0.0,
    entryTime: String = "",
    exitTime: String = ""
): TradeHistoryUiModel {
    val profitLoss = (exitPrice - entryPrice) * quantity
    val profitLossPercent = if (entryPrice > 0) {
        ((exitPrice - entryPrice) / entryPrice) * 100
    } else {
        0.0
    }
    val status = when {
        profitLoss > 0 -> "PROFIT"
        profitLoss < 0 -> "LOSS"
        else -> "BREAKEVEN"
    }

    return TradeHistoryUiModel(
        tradeId = tradeId,
        symbol = symbol,
        tradeType = tradeType,
        quantity = quantity,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        profitLoss = profitLoss,
        profitLossPercent = profitLossPercent,
        entryTime = entryTime,
        exitTime = exitTime,
        status = status
    )
}

fun mapToTradeStatisticsUiModel(
    totalTrades: Int = 0,
    winningTrades: Int = 0,
    losingTrades: Int = 0,
    totalProfit: Double = 0.0,
    totalLoss: Double = 0.0
): TradeStatisticsUiModel {
    val winRate = if (totalTrades > 0) {
        (winningTrades.toDouble() / totalTrades) * 100
    } else {
        0.0
    }
    val netProfit = totalProfit - totalLoss
    val averageWin = if (winningTrades > 0) totalProfit / winningTrades else 0.0
    val averageLoss = if (losingTrades > 0) totalLoss / losingTrades else 0.0
    val profitFactor = if (totalLoss > 0) totalProfit / totalLoss else 0.0

    return TradeStatisticsUiModel(
        totalTrades = totalTrades,
        winningTrades = winningTrades,
        losingTrades = losingTrades,
        winRate = winRate,
        totalProfit = totalProfit,
        totalLoss = totalLoss,
        netProfit = netProfit,
        averageWin = averageWin,
        averageLoss = averageLoss,
        profitFactor = profitFactor
    )
}

/**
 * Live Logs Screen Mappers
 */
fun mapToLogEntryUiModel(
    logId: String = "",
    level: String = "",
    message: String = "",
    timestamp: String = "",
    source: String = "",
    details: String? = null
): LogEntryUiModel {
    return LogEntryUiModel(
        logId = logId,
        level = level,
        message = message,
        timestamp = timestamp,
        source = source,
        details = details
    )
}

/**
 * More Screen Mappers
 */
fun mapToAboutInfoUiModel(
    appVersion: String = "1.0.0",
    buildNumber: String = "100",
    buildDate: String = "",
    developer: String = "MyAlgoTrade",
    website: String = "www.myalgotrade.com",
    supportEmail: String = "support@myalgotrade.com"
): AboutInfoUiModel {
    return AboutInfoUiModel(
        appVersion = appVersion,
        buildNumber = buildNumber,
        buildDate = buildDate,
        developer = developer,
        website = website,
        supportEmail = supportEmail
    )
}
