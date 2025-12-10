package com.trading.orb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.RiskSettings
import com.trading.orb.ui.components.*
import com.trading.orb.ui.theme.*

@Composable
fun RiskScreen(
    riskSettings: RiskSettings,
    onRiskSettingsChange: (RiskSettings) -> Unit,
    onCloseAllPositions: () -> Unit,
    onPauseTrading: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedSettings by remember { mutableStateOf(riskSettings) }
    var showCloseAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Limits Section
        DailyLimitsSection(
            settings = editedSettings,
            onSettingsChange = { editedSettings = it }
        )

        // Position Limits Section
        PositionLimitsSection(
            settings = editedSettings,
            onSettingsChange = { editedSettings = it }
        )

        // Circuit Breaker Section
        CircuitBreakerSection(
            settings = editedSettings,
            onSettingsChange = { editedSettings = it }
        )

        // Emergency Controls
        EmergencyControlsSection(
            onCloseAllPositions = { showCloseAllDialog = true },
            onPauseTrading = onPauseTrading
        )

        // Save Button
        Button(
            onClick = { onRiskSettingsChange(editedSettings) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Save Risk Settings",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Close All Confirmation Dialog
    if (showCloseAllDialog) {
        AlertDialog(
            onDismissRequest = { showCloseAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Error
                )
            },
            title = { Text("Close All Positions?") },
            text = {
                Text("This will immediately close all open positions. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCloseAllPositions()
                        showCloseAllDialog = false
                    }
                ) {
                    Text("Close All", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DailyLimitsSection(
    settings: RiskSettings,
    onSettingsChange: (RiskSettings) -> Unit
) {
    OrbCard {
        SectionHeader(
            text = "Daily Limits",
            icon = Icons.Default.CalendarToday
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Max Daily Loss Progress
        LinearProgressIndicator(
            progress = (settings.currentDailyLoss / settings.maxDailyLoss).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = Error
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Set Max Daily Loss
        OutlinedTextField(
            value = settings.maxDailyLoss.toInt().toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { value ->
                    if (value >= 100) {
                        onSettingsChange(settings.copy(maxDailyLoss = value))
                    }
                }
            },
            label = { Text("Set Max Daily Loss (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SurfaceVariant
            ),
            leadingIcon = {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Max Daily Trades Progress
        LinearProgressIndicator(
            progress = (settings.currentDailyTrades.toDouble() / settings.maxDailyTrades).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Set Max Daily Trades
        OutlinedTextField(
            value = settings.maxDailyTrades.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { value ->
                    onSettingsChange(settings.copy(maxDailyTrades = value))
                }
            },
            label = { Text("Set Max Daily Trades") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SurfaceVariant
            ),
            leadingIcon = {
                Icon(Icons.Default.Numbers, contentDescription = null)
            }
        )
    }
}

@Composable
private fun PositionLimitsSection(
    settings: RiskSettings,
    onSettingsChange: (RiskSettings) -> Unit
) {
    OrbCard {
        SectionHeader(
            text = "Position Limits",
            icon = Icons.Default.AccountBalance
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = settings.maxPositions.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { value ->
                        onSettingsChange(settings.copy(maxPositions = value))
                    }
                },
                label = { Text("Max Positions") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant
                )
            )

            OutlinedTextField(
                value = settings.maxPerInstrument.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { value ->
                        onSettingsChange(settings.copy(maxPerInstrument = value))
                    }
                },
                label = { Text("Max Per Instrument") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun CircuitBreakerSection(
    settings: RiskSettings,
    onSettingsChange: (RiskSettings) -> Unit
) {
    OrbCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                text = "Circuit Breaker",
                icon = Icons.Default.Block
            )

            if (settings.isCircuitBreakerTriggered) {
                StatusBadge(
                    text = "TRIGGERED",
                    color = Error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = settings.circuitBreakerLossPercent.toInt().toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        onSettingsChange(settings.copy(circuitBreakerLossPercent = value))
                    }
                },
                label = { Text("Auto-pause at loss %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant
                ),
                trailingIcon = {
                    Icon(Icons.Default.Percent, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = settings.coolDownMinutes.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { value ->
                        onSettingsChange(settings.copy(coolDownMinutes = value))
                    }
                },
                label = { Text("Cool-down (min)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant
                ),
                trailingIcon = {
                    Icon(Icons.Default.Timer, contentDescription = null)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Trading will automatically pause if daily loss reaches ${settings.circuitBreakerLossPercent.toInt()}% of max daily loss",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmergencyControlsSection(
    onCloseAllPositions: () -> Unit,
    onPauseTrading: () -> Unit
) {
    OrbCard {
        SectionHeader(
            text = "Emergency Controls",
            icon = Icons.Default.Warning
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCloseAllPositions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Close All Positions",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPauseTrading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pause Trading",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true, name = "Risk Screen - Paper Mode")
@Composable
fun RiskScreenPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.PAPER) {
        RiskScreen(
            riskSettings = RiskSettings(
                maxDailyLoss = 100.0,
                currentDailyLoss = 50.0,
                maxDailyTrades = 10,
                currentDailyTrades = 5,
                maxPositions = 4,
                maxPerInstrument = 2,
                circuitBreakerLossPercent = 80.0,
                coolDownMinutes = 15
            ),
            onRiskSettingsChange = {},
            onCloseAllPositions = {},
            onPauseTrading = {}
        )
    }
}

@Preview(showBackground = true, name = "Risk Screen - Live Mode")
@Composable
fun RiskScreenLivePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        RiskScreen(
            riskSettings = RiskSettings(
                maxDailyLoss = 100.0,
                currentDailyLoss = 50.0,
                maxDailyTrades = 10,
                currentDailyTrades = 5,
                maxPositions = 4,
                maxPerInstrument = 2,
                circuitBreakerLossPercent = 80.0,
                coolDownMinutes = 15
            ),
            onRiskSettingsChange = {},
            onCloseAllPositions = {},
            onPauseTrading = {}
        )
    }
}