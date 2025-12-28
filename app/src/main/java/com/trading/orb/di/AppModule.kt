package com.trading.orb.di

import com.trading.orb.BuildConfig
import com.trading.orb.data.engine.MarketDataSource
import com.trading.orb.data.engine.OrderExecutor
import com.trading.orb.data.engine.mock.MockMarketDataSource
import com.trading.orb.data.engine.mock.MockOrderExecutor
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.data.repository.TradingRepositoryImpl
import com.trading.orb.ui.utils.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTradingRepository(impl: TradingRepositoryImpl): TradingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Add more providers for API services, WebSocket, etc.

    @Module
    @InstallIn(SingletonComponent::class)
    object EngineModule {

        @Provides
        @Singleton
        fun provideMarketDataSource(
            @Named("use_mock") useMock: Boolean
        ): MarketDataSource {
            return if (useMock) {
                Timber.d("🧪 [DEBUG] Using MOCK Market Data Source - Easy testing mode!")
                MockMarketDataSource(
                    basePrice = DEFAULT_BASE_PRICE,
                    volatility = DEFAULT_VOLATILITY,
                    updateIntervalMs = DEFAULT_UPDATE_INTERVAL_MS
                )
            } else {
                Timber.w("⚠️ [RELEASE] Real Market Data Source not yet implemented")
                MockMarketDataSource(
                    basePrice = DEFAULT_BASE_PRICE,
                    volatility = DEFAULT_VOLATILITY,
                    updateIntervalMs = DEFAULT_UPDATE_INTERVAL_MS
                )
            }
        }

        @Provides
        @Singleton
        fun provideOrderExecutor(
            @Named("use_mock") useMock: Boolean
        ): OrderExecutor {
            return if (useMock) {
                Timber.d("🧪 [DEBUG] Using MOCK Order Executor - Easy testing mode!")
                MockOrderExecutor(
                    executionDelayMs = DEFAULT_EXECUTION_DELAY_MS,
                    failureRate = DEFAULT_FAILURE_RATE
                )
            } else {
                Timber.w("⚠️ [RELEASE] Real Order Executor not yet implemented")
                MockOrderExecutor(
                    executionDelayMs = DEFAULT_EXECUTION_DELAY_MS,
                    failureRate = DEFAULT_FAILURE_RATE
                )
            }
        }

        @Provides
        @Named("use_mock")
        fun provideUseMockFlag(): Boolean = BuildConfig.USE_MOCK_DATA

        @Provides
        @Named("api_key")
        fun provideApiKey(): String = BuildConfig.ANGEL_API_KEY

        @Provides
        @Named("access_token")
        fun provideAccessToken(): String = BuildConfig.ANGEL_ACCESS_TOKEN
    }
}

