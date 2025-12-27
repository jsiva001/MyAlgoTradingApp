package com.trading.orb.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base class for implementing MVI pattern in Android
 * Provides a framework for managing UI state and effects in a unidirectional data flow
 * 
 * Usage:
 * 1. Create your State, Intent, and Effect classes
 * 2. Extend this base class
 * 3. Override createInitialState()
 * 4. Implement reduce() for state transitions
 * 5. Implement handleIntents() for handling intents with side effects
 */
abstract class BaseMviViewModel<State : MviState, Intent : MviIntent, Effect : MviEffect> :
    ViewModel(), MviViewModel<State, Intent, Effect> {

    /**
     * Internal mutable state flow - not exposed directly
     */
    private val _state: MutableStateFlow<State> by lazy {
        MutableStateFlow(createInitialState())
    }

    /**
     * Public immutable state flow - the single source of truth
     */
    override val state: StateFlow<State> by lazy { _state.asStateFlow() }

    /**
     * Internal mutable effects flow
     */
    private val _effects = MutableSharedFlow<Effect>(replay = 0)

    /**
     * Public immutable effects flow - for one-time events
     */
    override val effects: SharedFlow<Effect> by lazy { _effects.asSharedFlow() }

    /**
     * Intent receiver channel
     */
    private val intents = MutableSharedFlow<Intent>()

    init {
        subscribeToIntents()
    }

    /**
     * Create and return the initial state
     * Override this method in subclasses
     */
    abstract fun createInitialState(): State

    /**
     * Reduce function: (currentState, intent) -> newState
     * Override this method to define how intents transform the state
     */
    abstract suspend fun reduce(currentState: State, intent: Intent): State

    /**
     * Optional: Override this to handle intents with side effects
     * Call emitEffect() and updateState() from here
     */
    open suspend fun handleIntents(intent: Intent) {
        // Default implementation: just reduce the state
        updateState { currentState ->
            reduce(currentState, intent)
        }
    }

    /**
     * Main entry point for user intents
     * Launches the intent handling in the viewModelScope
     */
    override suspend fun processIntent(intent: Intent) {
        intents.emit(intent)
    }

    /**
     * Emit a one-time effect to the UI
     */
    protected suspend fun emitEffect(effect: Effect) {
        _effects.emit(effect)
    }

    /**
     * Update the state using a reducer function
     * This is the safe way to update state from anywhere in the ViewModel
     */
    protected fun updateState(reducer: suspend (currentState: State) -> State) {
        viewModelScope.launch {
            _state.update { currentState ->
                reducer(currentState)
            }
        }
    }

    /**
     * Update the state synchronously
     */
    protected fun updateStateImmediate(reducer: (currentState: State) -> State) {
        _state.update { currentState ->
            reducer(currentState)
        }
    }

    /**
     * Subscribe to all intents and process them
     */
    private fun subscribeToIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                handleIntents(intent)
            }
        }
    }
}
