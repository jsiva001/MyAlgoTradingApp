package com.trading.orb.data.engine.mock

import com.trading.orb.data.engine.OrderExecutor
import com.trading.orb.data.model.*
import kotlinx.coroutines.delay
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class MockOrderExecutor(
    private val executionDelayMs: Long = 500,
    private val failureRate: Int = 0
) : OrderExecutor {

    private val positions = mutableMapOf<String, Position>()

    override suspend fun placeMarketOrder(
        symbol: String,
        side: OrderSide,
        quantity: Int,
        tag: String?
    ): Result<OrderResponse> {
        Timber.i("Mock: Placing MARKET order - $side $quantity x $symbol")
        delay(executionDelayMs)

        if (Random.nextInt(100) < failureRate) {
            return Result.failure(Exception("Mock order failed"))
        }

        val orderId = UUID.randomUUID().toString()
        val mockPrice = 185.0 + Random.nextDouble(-1.0, 1.0)

        val response = OrderResponse(
            orderId = orderId,
            status = "COMPLETE",
            message = "Mock order executed",
            price = mockPrice
        )

        return Result.success(response)
    }

    override suspend fun placeLimitOrder(
        symbol: String,
        side: OrderSide,
        quantity: Int,
        price: Double,
        tag: String?
    ): Result<OrderResponse> {
        delay(executionDelayMs)

        val orderId = UUID.randomUUID().toString()
        return Result.success(
            OrderResponse(orderId, "PENDING", "Mock limit order placed", price)
        )
    }

    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        delay(executionDelayMs)
        return Result.success(Unit)
    }

    override suspend fun getPositions() = Result.success(positions.values.toList())

    override suspend fun closePosition(positionId: String): Result<Unit> {
        delay(executionDelayMs)
        positions.remove(positionId)
        return Result.success(Unit)
    }

    fun reset() {
        positions.clear()
    }
}