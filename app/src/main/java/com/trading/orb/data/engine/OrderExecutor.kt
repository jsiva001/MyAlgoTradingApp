package com.trading.orb.data.engine

import com.trading.orb.data.model.*

/**
 * Interface for order execution - Mock or Real (Angel One)
 */
interface OrderExecutor {
    suspend fun placeMarketOrder(
        symbol: String,
        side: OrderSide,
        quantity: Int,
        tag: String? = null
    ): Result<OrderResponse>

    suspend fun placeLimitOrder(
        symbol: String,
        side: OrderSide,
        quantity: Int,
        price: Double,
        tag: String? = null
    ): Result<OrderResponse>

    suspend fun cancelOrder(orderId: String): Result<Unit>
    suspend fun getPositions(): Result<List<Position>>
    suspend fun closePosition(positionId: String): Result<Unit>
}