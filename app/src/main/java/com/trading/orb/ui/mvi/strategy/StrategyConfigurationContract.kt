package com.trading.orb.ui.mvi.strategy

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.data.model.StrategyConfig
import com.trading.orb.ui.state.TradingHoursUiModel
import com.trading.orb.ui.state.AdvancedSettingsUiModel

/**
 * Strategy Configuration Screen State
 */
data class StrategyConfigurationScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Configuration
    val strategyConfig: StrategyConfig? = null,
    val tradingHours: TradingHoursUiModel = TradingHoursUiModel(),
    val advancedSettings: AdvancedSettingsUiModel = AdvancedSettingsUiModel(),
    
    // Edit state
    val isEditing: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val selectedTabIndex: Int = 0
) : MviState

sealed class StrategyConfigurationScreenIntent : MviIntent {
    // Lifecycle
    object LoadConfiguration : StrategyConfigurationScreenIntent()
    object RetryLoadConfiguration : StrategyConfigurationScreenIntent()
    object RefreshConfiguration : StrategyConfigurationScreenIntent()

    // Edit mode
    object EnableEditMode : StrategyConfigurationScreenIntent()
    object DisableEditMode : StrategyConfigurationScreenIntent()
    object ResetChanges : StrategyConfigurationScreenIntent()

    // Configuration updates
    data class UpdateStrategyConfig(val config: StrategyConfig) : StrategyConfigurationScreenIntent()
    data class UpdateTradingHours(val hours: TradingHoursUiModel) : StrategyConfigurationScreenIntent()
    data class UpdateAdvancedSettings(val settings: AdvancedSettingsUiModel) : StrategyConfigurationScreenIntent()

    // Tab navigation
    data class SelectTab(val tabIndex: Int) : StrategyConfigurationScreenIntent()

    // Actions
    object SaveConfiguration : StrategyConfigurationScreenIntent()
    object ApplyDefaults : StrategyConfigurationScreenIntent()
    object ExportConfiguration : StrategyConfigurationScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : StrategyConfigurationScreenIntent()
}

sealed class StrategyConfigurationScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : StrategyConfigurationScreenEffect()
    data class ShowError(val message: String) : StrategyConfigurationScreenEffect()
    data class ShowSuccess(val message: String) : StrategyConfigurationScreenEffect()

    // Navigation
    object NavigateBack : StrategyConfigurationScreenEffect()

    // Dialogs
    object ShowUnsavedChangesDialog : StrategyConfigurationScreenEffect()
    object ShowApplyDefaultsConfirm : StrategyConfigurationScreenEffect()

    // Actions
    data class ExportConfigFile(val filePath: String) : StrategyConfigurationScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : StrategyConfigurationScreenEffect()
}
