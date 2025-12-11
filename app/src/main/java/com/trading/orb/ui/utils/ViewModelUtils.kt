package com.trading.orb.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

/**
 * Reusable LaunchedEffect for loading data on screen composition
 */
@Composable
fun <T : ViewModel> LaunchDataLoader(
    viewModel: T,
    loadAction: suspend () -> Unit
) {
    LaunchedEffect(Unit) {
        loadAction()
    }
}

/**
 * Reusable LaunchedEffect for collecting UI events
 */
@Composable
fun <T> LaunchEventCollector(
    eventFlow: Flow<T>,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(key1 = eventFlow) {
        eventFlow.collect { event ->
            onEvent(event)
        }
    }
}
