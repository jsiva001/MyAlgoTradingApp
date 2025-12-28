package com.trading.orb.data.engine

import com.trading.orb.data.model.*
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ORB_ENTRY_TAG
import com.trading.orb.ui.utils.ORDER_FAILED_MESSAGE
import com.trading.orb.ui.utils.DEFAULT_DELAY_MS
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Core ORB Strategy Engine
 * Works with any MarketDataSource and OrderExecutor
 */
class OrbStrategyEngine(
    private val marketDataSource: MarketDataSource,
    private val orderExecutor: OrderExecutor,
    private val config: StrategyConfig,
    private val riskSettings: RiskSettings
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var orbLevels: OrbLevels? = null
    private var activePosition: Position? = null
    private var isRunning = false
    private var orbCaptured = false
    
    // FOR MOCK TESTING: Track when strategy started to measure elapsed time
    private var strategyStartTime: LocalDateTime? = null
    private val orbDurationMinutes = 1 // 15-minute ORB window for testing

    private val _events = MutableSharedFlow<StrategyEvent>()
    val events: SharedFlow<StrategyEvent> = _events.asSharedFlow()

    suspend fun start() {
        if (isRunning) return

        isRunning = true
        orbCaptured = false
        strategyStartTime = LocalDateTime.now()

        _events.emit(StrategyEvent.Started(config))
        Timber.i(TimberLogs.ORB_STRATEGY_STARTED, config.instrument.symbol)

        scope.launch { runStrategy() }
    }

    suspend fun stop() {
        isRunning = false
        scope.coroutineContext.cancelChildren()
        _events.emit(StrategyEvent.Stopped)
        Timber.i(TimberLogs.ORB_STRATEGY_STOPPED)
    }

    private suspend fun runStrategy() {
        try {
            waitAndCaptureOrb()

            if (orbLevels != null) {
                monitorForBreakout()
            }

            if (activePosition != null) {
                managePosition()
            }
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.STRATEGY_ERROR.replace("%s", ""))
            _events.emit(StrategyEvent.Error(e.message ?: TimberLogs.STRATEGY_UNKNOWN_ERROR))
        }
    }

    private suspend fun waitAndCaptureOrb() {
        val candles = mutableListOf<Candle>()
        val startTimestamp = LocalDateTime.now()
        
        // Determine ORB time window based on mock/real mode
        val orbStartTime: LocalTime
        val orbEndTime: LocalTime
        
        if (com.trading.orb.BuildConfig.USE_MOCK_DATA) {
            // MOCK MODE: Use duration-based ORB window (15 minutes from start)
            Timber.i(TimberLogs.ORB_MOCK_MODE_STARTED, orbDurationMinutes)
            orbStartTime = startTimestamp.toLocalTime()
            orbEndTime = orbStartTime.plusMinutes(orbDurationMinutes.toLong())
        } else {
            // REAL MODE: Use absolute time-based ORB window (9:15-9:30 AM)
            Timber.i(TimberLogs.ORB_REAL_MODE_WAITING, config.orbStartTime)
            orbStartTime = config.orbStartTime
            orbEndTime = config.orbEndTime
            waitUntilTime(orbStartTime)
            Timber.i(TimberLogs.ORB_CAPTURE_WINDOW_OPENED, orbStartTime)
        }

        marketDataSource.subscribeLTP(config.instrument.symbol)
            .takeWhile { isInOrbWindowCondition(orbStartTime, orbEndTime) && isRunning }
            .collect { ltp ->
                Timber.d(TimberLogs.ORB_CAPTURE_LTP_UPDATE, ltp)
                // EMIT PriceUpdate event so UI can show live LTP during capture
                _events.emit(StrategyEvent.PriceUpdate(ltp))
                val candle = buildCandle(ltp, startTimestamp)
                candles.add(candle)
            }

        if (candles.isNotEmpty()) {
            val calculator = OrbLevelsCalculator()
            val calculatedLevels = calculator.calculateOrbLevels(
                candles, orbStartTime, orbEndTime,
                config.instrument, config.breakoutBuffer
            )
            
            // Create OrbLevels with isOrbCaptured = true (window completed)
            if (calculatedLevels != null) {
                orbLevels = calculatedLevels.copy(isOrbCaptured = true)

                orbCaptured = true
                Timber.i(TimberLogs.ORB_CAPTURED_SUCCESS, orbLevels?.high ?: 0.0, orbLevels?.low ?: 0.0)
                orbLevels?.let { _events.emit(StrategyEvent.OrbCaptured(it)) }
            }
        }
    }

    private suspend fun monitorForBreakout() {
        val levels = orbLevels ?: return
        val buyTrigger = levels.buyTrigger
        val sellTrigger = levels.sellTrigger

        marketDataSource.subscribeLTP(config.instrument.symbol)
            .takeWhile { isRunning && activePosition == null }
            .collect { ltp ->
                _events.emit(StrategyEvent.PriceUpdate(ltp))
                
                Timber.d(TimberLogs.ORB_BREAKOUT_LTP_MONITORING, ltp, buyTrigger, sellTrigger)

                if (ltp >= buyTrigger) {
                    Timber.i(TimberLogs.ORB_BUY_SIGNAL, ltp, buyTrigger)
                    placeEntryOrder(OrderSide.BUY, ltp)
                } else if (ltp <= sellTrigger) {
                    Timber.i(TimberLogs.ORB_SELL_SIGNAL, ltp, sellTrigger)
                    placeEntryOrder(OrderSide.SELL, ltp)
                }
            }
    }

    private suspend fun managePosition() {
        val position = activePosition ?: return

        marketDataSource.subscribeLTP(config.instrument.symbol)
            .takeWhile { isRunning && activePosition != null }
            .collect { ltp ->
                activePosition = position.copy(currentPrice = ltp)
                Timber.d(TimberLogs.ORB_POSITION_MONITORING, ltp, ltp - position.entryPrice)
                _events.emit(StrategyEvent.PositionUpdate(activePosition!!))

                when {
                    isStopLossHit(position, ltp) -> exitPosition(ExitReason.SL_HIT, ltp)
                    isTargetHit(position, ltp) -> exitPosition(ExitReason.TARGET_HIT, ltp)
                    config.enableAutoExit && isAutoExitTime() -> exitPosition(ExitReason.TIME_EXIT, ltp)
                }
            }
    }

    private suspend fun placeEntryOrder(side: OrderSide, price: Double) {
        if (!canTakeNewTrade()) {
            _events.emit(StrategyEvent.RiskLimitReached)
            return
        }

        val result = when (config.orderType) {
            OrderType.MARKET -> orderExecutor.placeMarketOrder(
                config.instrument.symbol, side, config.lotSize, ORB_ENTRY_TAG
            )
            OrderType.LIMIT -> orderExecutor.placeLimitOrder(
                config.instrument.symbol, side, config.lotSize, price, ORB_ENTRY_TAG
            )
        }

        result.onSuccess { orderResponse ->
            val stopLoss = calculateStopLoss(side, price)
            val target = calculateTarget(side, price)

            activePosition = Position(
                id = orderResponse.orderId,
                instrument = config.instrument,
                side = side,
                quantity = config.lotSize,
                entryPrice = orderResponse.price ?: price,
                currentPrice = price,
                stopLoss = stopLoss,
                target = target,
                entryTime = LocalDateTime.now()
            )

            _events.emit(StrategyEvent.PositionOpened(activePosition!!))
        }

        result.onFailure { error ->
            _events.emit(StrategyEvent.OrderFailed(error.message ?: ORDER_FAILED_MESSAGE))
        }
    }

    private suspend fun exitPosition(reason: ExitReason, exitPrice: Double) {
        val position = activePosition ?: return

        orderExecutor.closePosition(position.id).onSuccess {
            val trade = Trade(
                id = position.id,
                instrument = position.instrument,
                side = position.side,
                quantity = position.quantity,
                entryPrice = position.entryPrice,
                exitPrice = exitPrice,
                entryTime = position.entryTime,
                exitTime = LocalDateTime.now(),
                exitReason = reason,
                pnl = calculatePnL(position, exitPrice)
            )

            _events.emit(StrategyEvent.PositionClosed(trade))
            activePosition = null
        }
    }

    // Helper methods
    // Check if current time/elapsed time is within ORB window
    private fun isInOrbWindowCondition(orbStartTime: LocalTime, orbEndTime: LocalTime): Boolean {
        return if (com.trading.orb.BuildConfig.USE_MOCK_DATA) {
            // MOCK MODE: Check elapsed time
            isInOrbCaptureDuration()
        } else {
            // REAL MODE: Check absolute time
            LocalTime.now() in orbStartTime..orbEndTime
        }
    }
    
    // FOR MOCK TESTING: Check elapsed time instead of absolute time
    private fun isInOrbCaptureDuration(): Boolean {
        val elapsed = java.time.Duration.between(strategyStartTime, LocalDateTime.now()).toMinutes()
        return elapsed < orbDurationMinutes
    }
    
    private fun isInOrbWindow() = LocalTime.now() in config.orbStartTime..config.orbEndTime
    private fun isAutoExitTime() = LocalTime.now() >= config.autoExitTime

    private fun isStopLossHit(position: Position, currentPrice: Double) = when (position.side) {
        OrderSide.BUY -> currentPrice <= position.stopLoss
        OrderSide.SELL -> currentPrice >= position.stopLoss
    }

    private fun isTargetHit(position: Position, currentPrice: Double) = when (position.side) {
        OrderSide.BUY -> currentPrice >= position.target
        OrderSide.SELL -> currentPrice <= position.target
    }

    private fun calculateStopLoss(side: OrderSide, entryPrice: Double) = when (side) {
        OrderSide.BUY -> entryPrice - config.stopLossPoints
        OrderSide.SELL -> entryPrice + config.stopLossPoints
    }

    private fun calculateTarget(side: OrderSide, entryPrice: Double) = when (side) {
        OrderSide.BUY -> entryPrice + config.targetPoints
        OrderSide.SELL -> entryPrice - config.targetPoints
    }

    private fun calculatePnL(position: Position, exitPrice: Double): Double {
        val priceDiff = when (position.side) {
            OrderSide.BUY -> exitPrice - position.entryPrice
            OrderSide.SELL -> position.entryPrice - exitPrice
        }
        return priceDiff * position.quantity
    }

    private fun canTakeNewTrade() = !riskSettings.isCircuitBreakerTriggered &&
            riskSettings.currentDailyLoss < riskSettings.maxDailyLoss &&
            riskSettings.currentDailyTrades < riskSettings.maxDailyTrades

    private suspend fun waitUntilTime(targetTime: LocalTime) {
        while (LocalTime.now() < targetTime && isRunning) {
            delay(DEFAULT_DELAY_MS)
        }
    }

    private fun buildCandle(ltp: Double, timestamp: LocalDateTime) = Candle(
        timestamp, ltp, ltp, ltp, ltp, 0
    )
}