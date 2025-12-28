package com.trading.orb.data.repository

import com.trading.orb.data.model.*
import com.trading.orb.ui.state.HistoryFilter
import com.trading.orb.ui.utils.TimberLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingRepositoryImpl @Inject constructor() : TradingRepository {

    private val _appState = MutableStateFlow(
        AppState(
            tradingMode = TradingMode.PAPER,
            strategyStatus = StrategyStatus.INACTIVE,
            connectionStatus = ConnectionStatus.DISCONNECTED
        )
    )
    override val appState: Flow<AppState> = _appState

    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    override val positions: Flow<List<Position>> = _positions

    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    override val trades: Flow<List<Trade>> = _trades

    private val _strategyConfig = MutableStateFlow(
        StrategyConfig(
            instrument = Instrument(
                symbol = "NIFTY24DEC22000CE",
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            // FOR MOCK TESTING: Set ORB window to be 15 minutes from START button click
            // In real implementation with Angel One API, this will be 9:15-9:30 AM
            orbStartTime = java.time.LocalTime.of(0, 0), // Start immediately when strategy starts
            orbEndTime = java.time.LocalTime.of(23, 59), // Keep it open all day for testing
            autoExitTime = java.time.LocalTime.of(23, 50)
        )
    )
    override val strategyConfig: Flow<StrategyConfig> = _strategyConfig

    private val _riskSettings = MutableStateFlow(RiskSettings())
    override val riskSettings: Flow<RiskSettings> = _riskSettings

    override suspend fun startStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.ACTIVE,
                connectionStatus = ConnectionStatus.CONNECTED,
                strategyConfig = _strategyConfig.value
            )
            Timber.d(TimberLogs.STRATEGY_STARTED_SUCCESSFULLY)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_START_STRATEGY)
            Result.failure(e)
        }
    }

    override suspend fun stopStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.INACTIVE,
                strategyConfig = null,
                orbLevels = null
            )
            Timber.d(TimberLogs.STRATEGY_STOPPED_SUCCESSFULLY)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_STOP_STRATEGY)
            Result.failure(e)
        }
    }

    override suspend fun toggleTradingMode(): Result<Unit> {
        return try {
            val newMode = if (_appState.value.tradingMode == TradingMode.PAPER) {
                TradingMode.LIVE
            } else {
                TradingMode.PAPER
            }
            _appState.value = _appState.value.copy(tradingMode = newMode)
            Timber.d(TimberLogs.TRADING_MODE_TOGGLED, newMode.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_TOGGLE_TRADING_MODE)
            Result.failure(e)
        }
    }

    override suspend fun pauseStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.PAUSED
            )
            Timber.d(TimberLogs.STRATEGY_PAUSED)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_PAUSE_STRATEGY)
            Result.failure(e)
        }
    }

    override suspend fun closeAllPositions(): Result<Unit> {
        return try {
            _positions.value = emptyList()
            Timber.d(TimberLogs.ALL_POSITIONS_CLOSED)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_CLOSE_ALL_POSITIONS)
            Result.failure(e)
        }
    }

    override suspend fun updateStrategyConfig(config: StrategyConfig): Result<Unit> {
        return try {
            _strategyConfig.value = config
            Timber.d(TimberLogs.STRATEGY_CONFIG_UPDATED)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_UPDATE_STRATEGY_CONFIG)
            Result.failure(e)
        }
    }

    override suspend fun updateRiskSettings(settings: RiskSettings): Result<Unit> {
        return try {
            _riskSettings.value = settings
            Timber.d(TimberLogs.RISK_SETTINGS_UPDATED)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_UPDATE_RISK_SETTINGS)
            Result.failure(e)
        }
    }

    override suspend fun updateAppState(state: AppState): Result<Unit> {
        return try {
            _appState.value = state
            // Also update _positions to keep it in sync with AppState
            _positions.value = state.activePositions
            // Also update _trades with closed trades for history screen
            _trades.value = state.closedTrades
            Timber.d(TimberLogs.APP_STATE_UPDATED, state.activePositions.size, state.closedTrades.size)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_UPDATE_APP_STATE)
            Result.failure(e)
        }
    }

    override suspend fun fetchTradesHistory(filter: HistoryFilter): Result<Unit> {
        return try {
            // Placeholder for actual API call
            Timber.d(TimberLogs.TRADE_HISTORY_FETCHED, filter.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.FAILED_FETCH_TRADE_HISTORY)
            Result.failure(e)
        }
    }
}

