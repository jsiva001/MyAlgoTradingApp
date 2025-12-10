package com.trading.orb.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.theme.*
import java.time.LocalTime

@Composable
fun StrategyConfigScreen(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedConfig by remember { mutableStateOf(config) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InstrumentSection(
            instrument = editedConfig.instrument,
            onInstrumentChange = { editedConfig = editedConfig.copy(instrument = it) }
        )
        TimeSettingsSection(config = editedConfig) { editedConfig = it }
        EntryParametersSection(config = editedConfig) { editedConfig = it }
        ExitRulesSection(config = editedConfig) { editedConfig = it }
        PositionSizingSection(config = editedConfig) { editedConfig = it }

        SaveButton {
            onConfigChange(editedConfig)
            onSave()
        }
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Save Configuration", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun InstrumentSection(
    instrument: Instrument,
    onInstrumentChange: (Instrument) -> Unit
) {
    OrbCard {
        SectionHeader(text = "Instrument")
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = instrument.displayName,
            onValueChange = { },
            label = { Text("Search symbol...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Column {
            listOf("NIFTY 24DEC 22000 CE", "BANKNIFTY 27DEC 48000 PE", "NIFTY FUT 28DEC")
                .forEach { InstrumentSuggestion(it) }
        }
    }
}

@Composable
private fun InstrumentSuggestion(name: String) {
    TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
        Text(name, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium)
    }
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun TimeSettingsSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    OrbCard {
        SectionHeader(text = "Time Settings")
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "ORB Window",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeFieldRow(
                time = config.orbStartTime,
                label = "Start",
                onTimeChange = { onConfigChange(config.copy(orbStartTime = it)) },
                modifier = Modifier.weight(1f)
            )
            TimeFieldRow(
                time = config.orbEndTime,
                label = "End",
                onTimeChange = { onConfigChange(config.copy(orbEndTime = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        TimeFieldRow(
            time = config.autoExitTime,
            label = "Auto Exit Time",
            onTimeChange = { onConfigChange(config.copy(autoExitTime = it)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        TimeFieldRow(
            time = config.noReentryTime,
            label = "No Re-enter after this Time",
            onTimeChange = { onConfigChange(config.copy(noReentryTime = it)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TimeFieldRow(
    time: LocalTime,
    label: String,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = time.toString(),
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
    )
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    OrbCard {
        SectionHeader(text = title)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EntryParametersSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection("Entry Parameters") {
        NumberField(
            value = config.breakoutBuffer.toString(),
            label = "Breakout Buffer (ticks)",
            onValueChange = {
                it.toIntOrNull()?.let { value ->
                    onConfigChange(config.copy(breakoutBuffer = value))
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownField(
            value = config.orderType.name,
            label = "Order Type",
            options = OrderType.entries.map { it.name },
            onValueChange = { onConfigChange(config.copy(orderType = OrderType.valueOf(it))) }
        )
    }
}

@Composable
private fun ExitRulesSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection("Exit Rules") {
        NumberField(
            value = config.targetPoints.toString(),
            label = "Target Points",
            onValueChange = {
                it.toDoubleOrNull()?.let { value ->
                    onConfigChange(config.copy(targetPoints = value))
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        NumberField(
            value = config.stopLossPoints.toString(),
            label = "Stop Loss Points",
            onValueChange = {
                it.toDoubleOrNull()?.let { value ->
                    onConfigChange(config.copy(stopLossPoints = value))
                }
            }
        )
    }
}

@Composable
private fun PositionSizingSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection("Position Sizing") {
        NumberField(
            value = config.lotSize.toString(),
            label = "Lot Size",
            onValueChange = { updateIfValid(it, { it.toIntOrNull() }, 1) { value ->
                onConfigChange(config.copy(lotSize = value))
            } }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        NumberField(
            value = config.maxPositions.toString(),
            label = "Max Positions",
            onValueChange = { updateIfValid(it, { it.toIntOrNull() }, 1) { value ->
                onConfigChange(config.copy(maxPositions = value))
            } }
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun DropdownField(
    value: String,
    label: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(indication = null, interactionSource = interactionSource) { expanded = !expanded },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun <T> updateIfValid(
    input: String,
    parser: (String) -> T?,
    minValue: T,
    onValid: (T) -> Unit
) where T : Comparable<T> {
    parser(input)?.let { value ->
        if (value >= minValue) {
            onValid(value)
        }
    }
}

@Preview
@Composable
fun StrategyConfigScreenPreview() {
    val defaultConfig = StrategyConfig(
        instrument = Instrument(
            symbol = "NIFTY",
            exchange = "NSE",
            lotSize = 50,
            tickSize = 0.05,
            displayName = "NIFTY 50"
        )
    )

    StrategyConfigScreen(
        config = defaultConfig,
        onConfigChange = { },
        onSave = { }
    )
}

