package com.trading.orb.ui.utils

import com.trading.orb.data.model.Position
import com.trading.orb.data.model.Trade

/**
 * Common profit calculation utilities for consistent P&L display across all screens
 */
object ProfitCalculationUtils {
    
    /**
     * Calculate P&L amount from a Position
     * For active positions, uses current price and entry price
     */
    fun calculatePositionPnL(position: Position): Double {
        return position.pnl // Already calculated correctly in Position model
    }
    
    /**
     * Calculate P&L amount from a Trade
     * For closed trades, uses exit price and entry price
     */
    fun calculateTradePnL(trade: Trade): Double {
        return trade.pnl // Use gross P&L (before charges)
    }
    
    /**
     * Calculate P&L amount from a Trade (after charges)
     */
    fun calculateTradeNetPnL(trade: Trade): Double {
        return trade.netPnl // After charges
    }
    
    /**
     * Calculate P&L percentage
     * Formula: (P&L / (Entry Price * Quantity)) * 100
     */
    fun calculatePnLPercentage(pnl: Double, entryPrice: Double, quantity: Int): Double {
        return if (entryPrice > 0) {
            (pnl / (entryPrice * quantity)) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Determine if P&L is profit, loss, or breakeven
     */
    fun getPnLStatus(pnl: Double): String {
        return when {
            pnl > 0 -> "PROFIT"
            pnl < 0 -> "LOSS"
            else -> "BREAKEVEN"
        }
    }
}
