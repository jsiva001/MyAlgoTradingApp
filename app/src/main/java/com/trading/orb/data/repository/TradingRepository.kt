package com.trading.orb.data.repository

import com.trading.orb.data.model.*
import com.trading.orb.ui.state.HistoryFilter
import kotlinx.coroutines.flow.Flow

interface TradingRepository {
    // State flows
    val appState: Flow<AppState>
    val positions: Flow<List<Position>>
    val trades: Flow<List<Trade>>
    val strategyConfig: Flow<StrategyConfig>
    val riskSettings: Flow<RiskSettings>

    // Strategy operations
    suspend fun startStrategy(): Result<Unit>
    suspend fun stopStrategy(): Result<Unit>
    suspend fun toggleTradingMode(): Result<Unit>
    suspend fun pauseStrategy(): Result<Unit>

    // Close all positions at once
    suspend fun closeAllPositions(): Result<Unit>

    // Configuration operations
    suspend fun updateStrategyConfig(config: StrategyConfig): Result<Unit>
    suspend fun updateRiskSettings(settings: RiskSettings): Result<Unit>
    suspend fun updateAppState(state: AppState): Result<Unit>

    // Trade history
    suspend fun fetchTradesHistory(filter: HistoryFilter): Result<Unit>
}

