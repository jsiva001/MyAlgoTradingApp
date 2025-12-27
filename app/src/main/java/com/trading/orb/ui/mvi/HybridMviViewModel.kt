package com.trading.orb.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow

/**
 * Hybrid MVVM-MVI ViewModel Base Class
 * 
 * This base class combines the best of both patterns:
 * - MVVM: Traditional ViewModel with exposed StateFlow for UI binding
 * - MVI: Unidirectional data flow with clear intent handling
 * 
 * Benefits:
 * - Clear separation of concerns (State, Intent, Effect)
 * - Predictable state transitions
 * - Easier testing (pure reducer functions)
 * - Side effects are explicit (Effects)
 * - Compatible with Compose and traditional Data Binding
 * 
 * Usage:
 * ```
 * sealed class DashboardState : MviState {
 *     object Loading : DashboardState()
 *     data class Success(val data: Dashboard) : DashboardState()
 *     data class Error(val message: String) : DashboardState()
 * }
 * 
 * sealed class DashboardIntent : MviIntent {
 *     object LoadDashboard : DashboardIntent()
 *     data class StartStrategy(val config: StrategyConfig) : DashboardIntent()
 * }
 * 
 * sealed class DashboardEffect : MviEffect {
 *     data class ShowToast(val message: String) : DashboardEffect()
 *     data class NavigateTo(val screen: String) : DashboardEffect()
 * }
 * ```
 */
abstract class HybridMviViewModel<State : MviState, Intent : MviIntent, Effect : MviEffect> :
    ViewModel(), MviViewModel<State, Intent, Effect> {

    /**
     * Internal mutable state flow
     */
    private val _state: MutableStateFlow<State> by lazy {
        MutableStateFlow(createInitialState())
    }

    /**
     * Public immutable state flow - single source of truth
     * Observe this in Compose UI
     */
    override val state: StateFlow<State> by lazy { _state.asStateFlow() }
    
    /**
     * Alias for state - use in Compose screens
     * Provides clearer naming in UI layer
     */
    val uiState: StateFlow<State> by lazy { state }

    /**
     * Internal mutable effects flow - for side effects
     */
    private val _effects = MutableSharedFlow<Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public immutable effects flow - observe this for one-time events
     */
    override val effects: SharedFlow<Effect> by lazy { _effects.asSharedFlow() }

    /**
     * Intent receiver - collects all intents
     */
    private val intents = MutableSharedFlow<Intent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    init {
        subscribeToIntents()
    }

    /**
     * Return the initial state
     * Override in subclasses
     */
    abstract fun createInitialState(): State

    /**
     * Pure reducer: (currentState, intent) -> newState
     * Override to define state transitions based on intents
     */
    abstract fun reduce(currentState: State, intent: Intent): State

    /**
     * Handle intent with side effects
     * Optional: Override to add side effects handling
     * Call updateState() to change state, emitEffect() for side effects
     */
    open suspend fun handleIntent(intent: Intent) {
        val newState = reduce(state.value, intent)
        _state.value = newState
    }

    /**
     * Process a new intent from the UI
     * This method is safe to call from any coroutine context
     */
    override suspend fun processIntent(intent: Intent) {
        intents.emit(intent)
    }

    /**
     * Non-suspending intent processor for Compose callbacks
     * Automatically launches in viewModelScope
     * Use this in UI callbacks instead of processIntent
     */
    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            processIntent(intent)
        }
    }

    /**
     * Emit a side effect to be handled by the UI
     * Examples: Show Toast, Navigate, Open Dialog
     */
    protected suspend fun emitEffect(effect: Effect) {
        _effects.emit(effect)
    }

    /**
     * Update state with a reducer function
     * Use this for complex state updates
     */
    protected fun updateState(reducer: suspend (State) -> State) {
        viewModelScope.launch {
            _state.update { currentState ->
                reducer(currentState)
            }
        }
    }

    /**
     * Synchronous state update
     * Use when you already have the new state
     */
    protected fun updateStateImmediate(newState: State) {
        _state.value = newState
    }

    /**
     * Safe state update with reducer
     * Useful when you need to base new state on current state
     */
    protected fun updateStateImmediate(reducer: (State) -> State) {
        _state.update(reducer)
    }

    /**
     * Process intent with side effects handling
     * Override handleIntent() to customize behavior
     */
    private fun subscribeToIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                try {
                    handleIntent(intent)
                } catch (e: Exception) {
                    handleException(intent, e)
                }
            }
        }
    }

    /**
     * Handle exceptions during intent processing
     * Override to provide custom error handling
     */
    protected open suspend fun handleException(intent: Intent, exception: Exception) {
        // Default implementation: log and emit error effect
        emitEffect(
            (createErrorEffect(exception) as? Effect)
                ?: return
        )
    }

    /**
     * Create an error effect from an exception
     * Override this to create app-specific error effects
     */
    protected open fun createErrorEffect(exception: Exception): Effect? = null
}
