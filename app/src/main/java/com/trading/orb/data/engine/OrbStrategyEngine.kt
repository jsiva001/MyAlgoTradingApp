package com.trading.orb.data.engine

import com.trading.orb.data.model.*
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

    private val _events = MutableSharedFlow<StrategyEvent>()
    val events: SharedFlow<StrategyEvent> = _events.asSharedFlow()

    suspend fun start() {
        if (isRunning) return

        isRunning = true
        orbCaptured = false

        _events.emit(StrategyEvent.Started(config))
        Timber.i("ORB Strategy started for ${config.instrument.symbol}")

        scope.launch { runStrategy() }
    }

    suspend fun stop() {
        isRunning = false
        scope.coroutineContext.cancelChildren()
        _events.emit(StrategyEvent.Stopped)
        Timber.i("ORB Strategy stopped")
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
            Timber.e(e, "Strategy error")
            _events.emit(StrategyEvent.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun waitAndCaptureOrb() {
        val orbStartTime = config.orbStartTime
        val orbEndTime = config.orbEndTime

        waitUntilTime(orbStartTime)

        val candles = mutableListOf<Candle>()
        val startTimestamp = LocalDateTime.now()

        marketDataSource.subscribeLTP(config.instrument.symbol)
            .takeWhile { isInOrbWindow() && isRunning }
            .collect { ltp ->
                val candle = buildCandle(ltp, startTimestamp)
                candles.add(candle)
            }

        if (candles.isNotEmpty()) {
            val calculator = OrbLevelsCalculator()
            orbLevels = calculator.calculateOrbLevels(
                candles, orbStartTime, orbEndTime,
                config.instrument, config.breakoutBuffer
            )

            orbCaptured = true
            orbLevels?.let { _events.emit(StrategyEvent.OrbCaptured(it)) }
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

                if (ltp >= buyTrigger) {
                    placeEntryOrder(OrderSide.BUY, ltp)
                } else if (ltp <= sellTrigger) {
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
                _events.emit(StrategyEvent.PositionUpdate(activePosition!!))

                when {
                    isStopLossHit(position, ltp) -> exitPosition(ExitReason.SL_HIT, ltp)
                    isTargetHit(position, ltp) -> exitPosition(ExitReason.TARGET_HIT, ltp)
                    isAutoExitTime() -> exitPosition(ExitReason.TIME_EXIT, ltp)
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
                config.instrument.symbol, side, config.lotSize, "ORB_ENTRY"
            )
            OrderType.LIMIT -> orderExecutor.placeLimitOrder(
                config.instrument.symbol, side, config.lotSize, price, "ORB_ENTRY"
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
            _events.emit(StrategyEvent.OrderFailed(error.message ?: "Order failed"))
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
            delay(1000)
        }
    }

    private fun buildCandle(ltp: Double, timestamp: LocalDateTime) = Candle(
        timestamp, ltp, ltp, ltp, ltp, 0
    )
}