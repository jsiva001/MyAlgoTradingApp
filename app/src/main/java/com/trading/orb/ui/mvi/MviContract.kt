package com.trading.orb.ui.mvi

/**
 * MVI Architecture Contract
 * 
 * Model-View-Intent (MVI) is a unidirectional data flow architecture pattern
 * where:
 * - View: UI layer that renders the state and sends intents
 * - Intent: User actions/events that drive the state machine
 * - Model: State holder that processes intents and produces new states
 */

/**
 * Base class for all UI States in MVI pattern
 * Immutable data class representing the complete UI state at a point in time
 */
interface MviState

/**
 * Base class for all User Intents/Actions in MVI pattern
 * Represents user interactions, events, or system triggers
 */
interface MviIntent

/**
 * Base class for all Side Effects in MVI pattern
 * Represents one-time events that are not part of the state
 * Examples: Navigation, Toast, Dialog, Error callbacks
 */
interface MviEffect

/**
 * Base ViewModel interface for MVI pattern
 * All MVI-based ViewModels should implement this interface
 */
interface MviViewModel<State : MviState, Intent : MviIntent, Effect : MviEffect> {
    /**
     * Observable state flow - the single source of truth
     * Emits the complete UI state whenever it changes
     */
    val state: kotlinx.coroutines.flow.StateFlow<State>

    /**
     * One-time effects flow - for side effects
     * Emits effects that should be handled once by the UI
     */
    val effects: kotlinx.coroutines.flow.SharedFlow<Effect>

    /**
     * Main entry point for user intents
     * All user interactions should go through this method
     */
    suspend fun processIntent(intent: Intent)
}
