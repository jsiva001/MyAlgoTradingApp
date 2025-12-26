package com.trading.orb.ui.screens.strategy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import com.trading.orb.ui.utils.*
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.event.StrategyConfigUiEvent
import com.trading.orb.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.trading.orb.ui.utils.TimePickerDialog
import com.trading.orb.ui.utils.TimePickerDialogAMPM
import com.trading.orb.ui.utils.NumberPickerDialog
import com.trading.orb.ui.utils.ShowValidationDialog

@Composable
fun StrategyConfigScreen(
    viewModel: StrategyConfigViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.strategyConfigUiState.collectAsStateWithLifecycle()
    
    LaunchEventCollector(eventFlow = viewModel.uiEvent) { event ->
        when (event) {
            is StrategyConfigUiEvent.ShowError -> {}
            is StrategyConfigUiEvent.ShowSuccess -> {}
            is StrategyConfigUiEvent.ConfigurationSaved -> {}
        }
    }
    
    StrategyConfigScreenContent(
        uiState = uiState,
        onSaveConfig = { viewModel.saveConfiguration() },
        modifier = modifier
    )
}

@Composable
private fun StrategyConfigScreenContent(
    uiState: StrategyConfigUiState,
    onSaveConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedConfig by remember { mutableStateOf(StrategyConfig(instrument = Instrument(symbol = "", exchange = "", lotSize = 0, tickSize = 0.0))) }
    var showValidationAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(PADDING_STANDARD)
                .padding(bottom = SAVE_BUTTON_HEIGHT),
            verticalArrangement = Arrangement.spacedBy(PADDING_EXTRA_LARGE)
        ) {
            InstrumentSection(
                instrument = editedConfig.instrument
            )
            TimeSettingsSection(config = editedConfig) { editedConfig = it }
            EntryParametersSection(config = editedConfig) { editedConfig = it }
            ExitRulesSection(config = editedConfig) { editedConfig = it }
            PositionSizingSection(config = editedConfig) { editedConfig = it }
        }

        SaveButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onClick = {
                val targetIsZero = editedConfig.targetPoints == 0.0
                val stopLossIsZero = editedConfig.stopLossPoints == 0.0
                val lotSizeInvalid = editedConfig.lotSize < MIN_LOT_SIZE || editedConfig.lotSize > MAX_LOT_SIZE
                val maxPositionsInvalid = editedConfig.maxPositions < MIN_MAX_POSITION || editedConfig.maxPositions > MAX_MAX_POSITION

                if (targetIsZero || stopLossIsZero || lotSizeInvalid || maxPositionsInvalid) {
                    showValidationAlert = true
                    alertMessage = buildValidationMessage(
                        targetIsZero = targetIsZero,
                        stopLossIsZero = stopLossIsZero,
                        lotSizeInvalid = lotSizeInvalid,
                        maxPositionsInvalid = maxPositionsInvalid
                    )
                    
                    editedConfig = editedConfig.copy(
                        targetPoints = if (targetIsZero) DEFAULT_TARGET_POINTS.toDouble() else editedConfig.targetPoints,
                        stopLossPoints = if (stopLossIsZero) DEFAULT_STOP_LOSS_POINTS.toDouble() else editedConfig.stopLossPoints,
                        lotSize = if (lotSizeInvalid) DEFAULT_LOT_SIZE else editedConfig.lotSize,
                        maxPositions = if (maxPositionsInvalid) DEFAULT_MAX_POSITION else editedConfig.maxPositions
                    )
                } else {
                    onSaveConfig()
                }
            }
        )
    }

    if (showValidationAlert) {
        ShowValidationDialog(
            message = alertMessage,
            onDismiss = { showValidationAlert = false }
        )
    }
}

@Composable
private fun SaveButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(PADDING_STANDARD)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(Labels.SAVE_CONFIGURATION, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun InstrumentSection(
    instrument: Instrument
) {
    OrbCard {
        SectionHeader(text = Labels.INSTRUMENT)
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        
        OutlinedTextField(
            value = instrument.displayName,
            onValueChange = { },
            label = { Text(Labels.SEARCH_SYMBOL) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
    }
}

@Composable
private fun InstrumentSuggestion(name: String) {
    TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
        Text(name, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun TimeSettingsSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    OrbCard {
        SectionHeader(text = Labels.TIME_SETTINGS)
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        
        Text(
            Labels.ORB_WINDOW,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(PADDING_SMALL))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PADDING_SMALL)
        ) {
            TimeFieldRow(
                time = config.orbStartTime,
                label = "Start",
                onTimeChange = { onConfigChange(config.copy(orbStartTime = it)) },
                modifier = Modifier.weight(1f),
                useAMPM = false,
                showTimePicker = false
            )
            TimeFieldRow(
                time = config.orbEndTime,
                label = "End",
                onTimeChange = { onConfigChange(config.copy(orbEndTime = it)) },
                modifier = Modifier.weight(1f),
                useAMPM = false,
                showTimePicker = false
            )
        }
        
        Spacer(modifier = Modifier.height(PADDING_LARGE))
        TimeFieldRow(
            time = config.autoExitTime,
            label = Labels.AUTO_EXIT_TIME_LABEL,
            onTimeChange = { onConfigChange(config.copy(autoExitTime = it)) },
            modifier = Modifier.fillMaxWidth(),
            minTime = LocalTime.of(9, 31),
            maxTime = LocalTime.of(15, 15)
        )
        
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        TimeFieldRow(
            time = config.noReentryTime,
            label = Labels.NO_REENTRY_TIME_LABEL,
            onTimeChange = { onConfigChange(config.copy(noReentryTime = it)) },
            modifier = Modifier.fillMaxWidth(),
            minTime = LocalTime.of(9, 31),
            maxTime = LocalTime.of(15, 0)
        )
    }
}

@Composable
private fun TimeFieldRow(
    time: LocalTime,
    label: String,
    onTimeChange: (LocalTime) -> Unit = {},
    modifier: Modifier = Modifier,
    useAMPM: Boolean = true,
    showTimePicker: Boolean = true,
    minTime: LocalTime? = null,
    maxTime: LocalTime? = null
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    
    val formatter = if (useAMPM) {
        DateTimeFormatter.ofPattern("hh:mm a")
    } else {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = time.format(formatter),
            onValueChange = { },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = showTimePicker,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { if (showTimePicker) isDialogOpen = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.primary
            ),
            enabled = false,
            readOnly = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = if (showTimePicker) { 
                { 
                    Icon(
                        Icons.Default.Schedule, 
                        contentDescription = "Select time",
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isDialogOpen = true }
                    )
                }
            } else {
                null
            }
        )
    }

    if (isDialogOpen && showTimePicker) {
        if (useAMPM) {
            TimePickerDialogAMPM(
                initialTime = time,
                onTimeSelected = { selectedTime ->
                    onTimeChange(selectedTime)
                    isDialogOpen = false
                },
                onDismiss = { isDialogOpen = false },
                minTime = minTime,
                maxTime = maxTime
            )
        } else {
            TimePickerDialog(
                initialTime = time,
                onTimeSelected = { selectedTime ->
                    onTimeChange(selectedTime)
                    isDialogOpen = false
                },
                onDismiss = { isDialogOpen = false }
            )
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    OrbCard {
        SectionHeader(text = title)
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        content()
    }
}

@Composable
private fun EntryParametersSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection(Labels.ENTRY_PARAMETERS) {
        NumberFieldWithDialog(
            value = config.breakoutBuffer,
            label = Labels.BREAKOUT_BUFFER_LABEL,
            onValueChange = { newValue ->
                onConfigChange(config.copy(breakoutBuffer = newValue))
            },
            range = MIN_BREAKOUT_BUFFER..MAX_BREAKOUT_BUFFER
        )
        
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        
        OutlinedTextField(
            value = Labels.ORDER_TYPE_MARKET,
            onValueChange = { },
            label = { Text(Labels.ORDER_TYPE_LABEL) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            enabled = false,
            readOnly = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun ExitRulesSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection(Labels.EXIT_RULES) {
        NumberField(
            value = config.targetPoints.toInt().toString(),
            label = Labels.TARGET_POINTS_LABEL,
            onValueChange = {
                val intValue = it.filter { c -> c.isDigit() }
                if (intValue.isNotEmpty()) {
                    intValue.toIntOrNull()?.let { value ->
                        if (value >= 0) {
                            onConfigChange(config.copy(targetPoints = value.toDouble()))
                        }
                    }
                } else if (it.isEmpty()) {
                    onConfigChange(config.copy(targetPoints = 0.0))
                }
            },
            keyboardType = KeyboardType.Number
        )
        
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        
        NumberField(
            value = config.stopLossPoints.toInt().toString(),
            label = Labels.STOP_LOSS_POINTS_LABEL,
            onValueChange = {
                val intValue = it.filter { c -> c.isDigit() }
                if (intValue.isNotEmpty()) {
                    intValue.toIntOrNull()?.let { value ->
                        if (value >= 0) {
                            onConfigChange(config.copy(stopLossPoints = value.toDouble()))
                        }
                    }
                } else if (it.isEmpty()) {
                    onConfigChange(config.copy(stopLossPoints = 0.0))
                }
            },
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
private fun PositionSizingSection(
    config: StrategyConfig,
    onConfigChange: (StrategyConfig) -> Unit
) {
    ConfigSection(Labels.POSITION_SIZING) {
        var lotSizeFocused by remember { mutableStateOf(false) }
        
        Box(modifier = Modifier.fillMaxWidth()) {
            NumberField(
                value = config.lotSize.toString(),
                label = Labels.LOT_SIZE_LABEL,
                onValueChange = { input ->
                    if (input.isEmpty()) {
                        onConfigChange(config.copy(lotSize = 0))
                    } else {
                        updateIfValid(input, { it.toIntOrNull() }, 0) { value ->
                            onConfigChange(config.copy(lotSize = value))
                        }
                    }
                },
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { lotSizeFocused = it.isFocused }
            )
            
            Text(
                text = getQuantityDisplay(config.lotSize),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (lotSizeFocused) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = PADDING_SMALL, bottom = PADDING_EXTRA_SMALL)
            )
        }
        
        Spacer(modifier = Modifier.height(PADDING_MEDIUM))
        
        NumberField(
            value = config.maxPositions.toString(),
            label = Labels.MAX_POSITION_LABEL,
            onValueChange = { input ->
                if (input.isEmpty()) {
                    onConfigChange(config.copy(maxPositions = 0))
                } else {
                    updateIfValid(input, { it.toIntOrNull() }, 0) { value ->
                        onConfigChange(config.copy(maxPositions = value))
                    }
                }
            }
        )
    }
}

private fun buildValidationMessage(
    targetIsZero: Boolean,
    stopLossIsZero: Boolean,
    lotSizeInvalid: Boolean,
    maxPositionsInvalid: Boolean
): String = com.trading.orb.ui.utils.buildValidationMessage(
    targetIsZero = targetIsZero,
    stopLossIsZero = stopLossIsZero,
    lotSizeInvalid = lotSizeInvalid,
    maxPositionsInvalid = maxPositionsInvalid
)

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

@Composable
private fun NumberFieldWithDialog(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    range: IntRange = MIN_BREAKOUT_BUFFER..MAX_BREAKOUT_BUFFER
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.toString(),
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { isDialogOpen = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.primary
        ),
        enabled = false,
        readOnly = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        ),
        trailingIcon = {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Select value",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { isDialogOpen = true }
            )
        }
    )

    if (isDialogOpen) {
        NumberPickerDialog(
            initialValue = value,
            range = range,
            onValueSelected = { selectedValue ->
                onValueChange(selectedValue)
                isDialogOpen = false
            },
            onDismiss = { isDialogOpen = false },
            title = label
        )
    }
}

@Preview
@Composable
fun StrategyConfigScreenPreview() {
    OrbTradingTheme {
        StrategyConfigScreenContent(
            uiState = StrategyConfigPreviewProvider.sampleStrategyConfigUiState(),
            onSaveConfig = { }
        )
    }
}
