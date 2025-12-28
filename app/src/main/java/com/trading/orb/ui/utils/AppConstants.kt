package com.trading.orb.ui.utils

// ============ TIME VALUES ============
const val ORB_START_TIME = "09:15"
const val ORB_END_TIME = "09:30"
const val AUTO_EXIT_TIME_DEFAULT = "15:15"
const val NO_REENTRY_TIME_DEFAULT = "15:00"
const val MARKET_OPEN_HOUR = 9
const val MARKET_OPEN_MINUTE = 15
const val MARKET_CLOSE_HOUR = 15
const val MARKET_CLOSE_MINUTE = 30
const val MOCK_STRATEGY_EXECUTION_DELAY_MS = 500L
const val MOCK_STRATEGY_FAILURE_RATE = 0
const val DEFAULT_CANDLE_INTERVAL = "1m"

// ============ UI CONSTANTS ============
const val DEFAULT_MAX_POSITION = 1
const val DEFAULT_BREAKOUT_BUFFER = 1
const val DEFAULT_LOT_SIZE = 1
const val DEFAULT_TARGET_POINTS = 5
const val DEFAULT_STOP_LOSS_POINTS = 3
const val MIN_BREAKOUT_BUFFER = 1
const val MAX_BREAKOUT_BUFFER = 10
const val MIN_MAX_POSITION = 1
const val MAX_MAX_POSITION = 4
const val MIN_LOT_SIZE = 1
const val MAX_LOT_SIZE = 20
const val QUANTITY_PER_LOT = 75

// ============ INSTRUMENT DEFAULTS ============
const val DEFAULT_INSTRUMENT_SYMBOL = "NIFTY24DEC22000CE"
const val DEFAULT_INSTRUMENT_EXCHANGE = "NSE"
const val DEFAULT_INSTRUMENT_LOT_SIZE = 50
const val DEFAULT_INSTRUMENT_TICK_SIZE = 0.05
const val DEFAULT_INSTRUMENT_DISPLAY_NAME = "NIFTY 22000 CE"

// ============ EXPORT PATHS ============
const val EXPORT_DIRECTORY = "/exports/"

// ============ DIALOG MESSAGES ============
object DialogMessages {
    const val VALIDATION_ERROR = "Invalid Values"
    const val OK = "OK"
    const val ERROR = "Error"
    const val CANCEL = "Cancel"
}

// ============ DEFAULT UI VALUES ============
const val DEFAULT_UPTIME = "00:00:00"
const val DEFAULT_APP_VERSION = "1.0.0"
const val DEFAULT_BUILD_NUMBER = "100"
const val DEFAULT_LAST_UPDATED = ""
const val DEFAULT_SEARCH_QUERY = ""
const val DEFAULT_SELECTED_TAB = "ALL"

// ============ FILTER TYPES ============
const val POSITION_FILTER_LONG = "LONG"
const val POSITION_FILTER_SHORT = "SHORT"
const val TRADE_FILTER_ALL = "ALL"
const val TRADE_FILTER_PROFIT = "PROFIT"
const val TRADE_FILTER_LOSS = "LOSS"

// ============ LOG DEFAULTS ============
const val DEFAULT_LOG_LEVEL = "INFO"
const val DEFAULT_LOG_ID = "1"
const val DEFAULT_LOG_MESSAGE = "App started"
const val DEFAULT_LOG_SOURCE = "MainActivity"

// ============ FILE NAMES & FORMATS ============
const val STRATEGY_CONFIG_EXPORT_FORMAT = "strategy_config_%d.json"
const val TRADE_HISTORY_EXPORT_FORMAT = "trade_history_%d.csv"

// ============ ERROR MESSAGES ============
object ErrorMessages {
    const val UNKNOWN_ERROR = "Unknown error"
    const val REFRESH_FAILED = "Refresh failed"
    const val AN_ERROR_OCCURRED = "An error occurred"
    const val STRATEGY_ERROR_FORMAT = "Strategy error: %s"
    const val ORDER_FAILED_FORMAT = "Order failed: %s"
    const val RISK_LIMIT_REACHED = "Risk limit reached"
    const val FAILED_STOP_STRATEGY = "Failed to stop strategy: %s"
    const val FAILED_TOGGLE_MODE = "Failed to toggle mode"
    const val POSITION_NOT_FOUND = "Position not found"
    const val FAILED_CLOSE_ALL_POSITIONS = "Failed to close all positions"
    const val FAILED_SAVE_CONFIGURATION = "Failed to save configuration"
    const val FAILED_SAVE_RISK_SETTINGS = "Failed to save risk settings"
    const val FAILED_SAVE_LIMITS = "Failed to save limits"
    const val FAILED_EMERGENCY_STOP = "Failed to trigger emergency stop"
    const val FAILED_REDUCE_EXPOSURE = "Failed to reduce exposure"
    const val FAILED_CLOSE_POSITION = "Failed to close position"
}

// ============ SCREEN LABELS ============
object Labels {
    // Time Settings
    const val TIME_SETTINGS = "Time Settings"
    const val ORB_WINDOW = "ORB Window (Min 15-min duration)"
    const val AUTO_EXIT_TIME_LABEL = "Auto Exit Time (Max 3:15 PM)"
    const val NO_REENTRY_TIME_LABEL = "No Re-entry Time (Before Auto Exit)"
    
    // Entry Parameters
    const val ENTRY_PARAMETERS = "Entry Parameters"
    const val BREAKOUT_BUFFER_LABEL = "Breakout Buffer (ticks)"
    const val ORDER_TYPE_LABEL = "Order Type"
    const val ORDER_TYPE_MARKET = "Market"
    
    // Exit Rules
    const val EXIT_RULES = "Exit Rules"
    const val TARGET_POINTS_LABEL = "Target Points (Min 5)"
    const val STOP_LOSS_POINTS_LABEL = "Stop Loss Points (Min 3)"
    
    // Position Sizing
    const val POSITION_SIZING = "Position Sizing"
    const val LOT_SIZE_LABEL = "Lot Size"
    const val MAX_POSITION_LABEL = "Max Position"
    
    // General
    const val INSTRUMENT = "Instrument"
    const val SEARCH_SYMBOL = "Search symbol..."
    const val SAVE_CONFIGURATION = "Save Configuration"
}

// ============ QUANTITY DISPLAY ============
const val QUANTITY_FORMAT = "Qty : %d ( %d X  %d)"

fun getQuantityDisplay(lotSize: Int): String {
    val totalQuantity = lotSize * QUANTITY_PER_LOT
    return String.format(QUANTITY_FORMAT, totalQuantity, QUANTITY_PER_LOT, lotSize)
}

// ============ VALIDATION MESSAGES ============
fun buildValidationMessage(
    targetIsZero: Boolean,
    stopLossIsZero: Boolean,
    lotSizeInvalid: Boolean,
    maxPositionsInvalid: Boolean
): String {
    val invalidFields = mutableListOf<String>()
    val resetValues = mutableListOf<String>()
    
    if (targetIsZero) {
        invalidFields.add("Target Points")
        resetValues.add("Target: $DEFAULT_TARGET_POINTS")
    }
    
    if (stopLossIsZero) {
        invalidFields.add("Stop Loss Points")
        resetValues.add("Stop Loss: $DEFAULT_STOP_LOSS_POINTS")
    }
    
    if (lotSizeInvalid) {
        invalidFields.add("Lot Size")
        resetValues.add("Lot Size: $DEFAULT_LOT_SIZE")
    }
    
    if (maxPositionsInvalid) {
        invalidFields.add("Max Positions")
        resetValues.add("Max Positions: $DEFAULT_MAX_POSITION")
    }
    
    val fieldsText = invalidFields.joinToString(" and ")
    val valuesText = resetValues.joinToString(", ")
    
    val message = when {
        lotSizeInvalid && !targetIsZero && !stopLossIsZero && !maxPositionsInvalid ->
            "$fieldsText is out of range (1-$MAX_LOT_SIZE). Default value has been set ($valuesText)"
        maxPositionsInvalid && !targetIsZero && !stopLossIsZero && !lotSizeInvalid ->
            "$fieldsText is out of range ($MIN_MAX_POSITION-$MAX_MAX_POSITION). Default value has been set ($valuesText)"
        else ->
            "$fieldsText cannot be 0 or out of range. Default value(s) have been set ($valuesText)"
    }
    
    return message
}

// ============ TIMBER LOG MESSAGES ============
object TimberLogs {
    // ORB Strategy Engine
    const val ORB_STRATEGY_STARTED = "ORB Strategy started for %s"
    const val ORB_STRATEGY_STOPPED = "ORB Strategy stopped"
    const val ORB_MOCK_MODE_STARTED = "🚀 MOCK MODE: ORB Capture window opened - Will collect for %d minutes"
    const val ORB_REAL_MODE_WAITING = "🚀 REAL MODE: ORB Capture window - Waiting for %s"
    const val ORB_CAPTURE_WINDOW_OPENED = "🚀 ORB Capture window opened at %s"
    const val ORB_CAPTURE_LTP_UPDATE = "📊 ORB Capture - LTP: ₹%.2f"
    const val ORB_CAPTURED_SUCCESS = "✅ ORB Captured - High: ₹%.2f, Low: ₹%.2f"
    const val ORB_BREAKOUT_LTP_MONITORING = "📊 LTP: ₹%.2f | Buy Trigger: ₹%.2f | Sell Trigger: ₹%.2f"
    const val ORB_BUY_SIGNAL = "🟢 BUY SIGNAL! LTP ₹%.2f >= Buy Trigger ₹%.2f"
    const val ORB_SELL_SIGNAL = "🔴 SELL SIGNAL! LTP ₹%.2f <= Sell Trigger ₹%.2f"
    const val ORB_POSITION_MONITORING = "💹 Position Monitoring - LTP: ₹%.2f | P&L: ₹%.2f"
    
    // Dashboard ViewModel
    const val DASHBOARD_LTP_UPDATE = "💹 LTP Update: ₹%.2f"
    const val DASHBOARD_FAILED_LOAD = "Failed to load dashboard"
    const val DASHBOARD_FAILED_REFRESH = "Failed to refresh dashboard"
    const val DASHBOARD_INIT_MOCK_STRATEGY = "🧪 Initializing MOCK ORB Strategy Engine"
    const val DASHBOARD_FAILED_INIT_STRATEGY = "Failed to initialize strategy"
    const val DASHBOARD_FAILED_STOP_STRATEGY = "Failed to stop strategy"
    const val DASHBOARD_CLOSING_POSITIONS = "Closing %d position(s) before mode switch"
    const val DASHBOARD_FAILED_TOGGLE_MODE = "Failed to toggle trading mode"
    const val DASHBOARD_ERROR_CLOSE_TRADE = "Error closing trade"
    const val DASHBOARD_EMERGENCY_STOP = "🚨 EMERGENCY STOP triggered!"
    
    // Strategy Events
    const val STRATEGY_STARTED = "🟢 Strategy Started"
    const val ORB_CAPTURED = "📈 ORB Captured - High: %s, Low: %s"
    const val LTP_UPDATE = "💹 LTP Update: %s"
    const val POSITION_OPENED = "🟢 Position Opened"
    const val POSITION_CLOSED = "🏁 Position Closed"
    const val STRATEGY_STOPPED = "⏹️ Strategy Stopped"
    const val STRATEGY_ERROR = "❌ Strategy Error: %s"
    const val ORDER_FAILED = "❌ Order Failed: %s"
    const val RISK_LIMIT_REACHED = "⚠️ Risk Limit Reached"
    const val STRATEGY_UNKNOWN_ERROR = "Unknown error"
    
    // Trade History ViewModel
    const val HISTORY_FAILED_LOAD = "Failed to load trades"
    const val HISTORY_FAILED_REFRESH = "Failed to refresh trades"
    const val HISTORY_FAILED_EXPORT = "Failed to export history"
    const val HISTORY_FAILED_CLEAR = "Failed to clear history"
    
    // Positions ViewModel
    const val POSITIONS_FAILED_LOAD = "Failed to load positions"
    const val POSITIONS_FAILED_REFRESH = "Failed to refresh positions"
    const val POSITIONS_FAILED_CLOSE = "Failed to close position"
    const val POSITIONS_FAILED_CLOSE_ALL = "Failed to close all positions"
    
    // Risk Management ViewModel
    const val RISK_FAILED_LOAD = "Failed to load risk data"
    const val RISK_FAILED_REFRESH = "Failed to refresh risk data"
    const val RISK_FAILED_SAVE = "Failed to save risk limits"
    const val RISK_EMERGENCY_STOP = "Emergency stop triggered"
    const val RISK_FAILED_EMERGENCY_STOP = "Failed to trigger emergency stop"
    const val RISK_FAILED_REDUCE = "Failed to reduce exposure"
    
    // Strategy Configuration ViewModel
    const val CONFIG_FAILED_LOAD = "Failed to load configuration"
    const val CONFIG_FAILED_REFRESH = "Failed to refresh configuration"
    const val CONFIG_FAILED_SAVE = "Failed to save configuration"
    const val CONFIG_FAILED_APPLY_DEFAULTS = "Failed to apply defaults"
    const val CONFIG_FAILED_EXPORT = "Failed to export configuration"
    
    // Live Logs ViewModel
    const val LOGS_FAILED_LOAD = "Failed to load logs"
    const val LOGS_FAILED_REFRESH = "Failed to refresh logs"
    const val LOGS_FAILED_CLEAR = "Failed to clear logs"
    const val LOGS_FAILED_EXPORT = "Failed to export logs"
    
    // More ViewModel
    const val SETTINGS_FAILED_LOAD = "Failed to load settings"
    const val SETTINGS_FAILED_REFRESH = "Failed to refresh settings"
    const val SETTINGS_FAILED_SAVE = "Failed to save settings"
    const val SETTINGS_FAILED_CACHE_CLEAR = "Failed to clear cache"
    const val SETTINGS_FAILED_CHECK_UPDATES = "Failed to check updates"
    const val LOGOUT_CONFIRMED = "User confirmed logout"
    const val LOGOUT_FAILED = "Failed to logout"
}

// ============ DASHBOARD EFFECT MESSAGES ============
object DashboardEffectMessages {
    const val ORB_CAPTURED_FORMAT = "ORB Captured! High: ₹%.2f, Low: ₹%.2f"
    const val POSITION_CLOSED_AT_FORMAT = "Position closed at ₹%.2f"
    const val MARKET_CLOSED_ERROR = "Market is closed (9:15 AM - 3:30 PM IST). Cannot initiate strategy!"
    const val STRATEGY_STARTED = "Strategy started successfully!"
    const val STRATEGY_STOPPED = "Strategy stopped"
    const val DASHBOARD_REFRESHED = "Dashboard refreshed"
    const val POSITION_NOT_FOUND = "Position not found"
    const val SWITCHED_TO_MODE_FORMAT = "Switched to %s mode"
    const val EMERGENCY_STOP_EXECUTED = "Emergency stop executed"
    const val CONFIGURATION_SAVED = "Configuration saved"
    const val RISK_SETTINGS_SAVED = "Risk settings saved"
    const val POSITION_OPENED_AT_FORMAT = "Position opened at ₹%.2f"
    const val POSITION_UPDATE = "💹 Position Update"
}

// ============ DASHBOARD DATA LOADING ============
const val DASHBOARD_DATA_LOAD_DELAY_MS = 500L
const val DASHBOARD_REFRESH_DELAY_MS = 1000L
