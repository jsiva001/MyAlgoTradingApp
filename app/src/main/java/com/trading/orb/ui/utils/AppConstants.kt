package com.trading.orb.ui.utils

// ============ TIME VALUES ============
const val ORB_START_TIME = "09:15"
const val ORB_END_TIME = "09:30"
const val AUTO_EXIT_TIME_DEFAULT = "15:15"
const val NO_REENTRY_TIME_DEFAULT = "15:00"

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

// ============ DIALOG MESSAGES ============
object DialogMessages {
    const val VALIDATION_ERROR = "Invalid Values"
    const val OK = "OK"
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
