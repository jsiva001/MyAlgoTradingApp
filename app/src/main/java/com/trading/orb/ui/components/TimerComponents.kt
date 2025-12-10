package com.trading.orb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.orb.data.model.TradingMode
import com.trading.orb.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ============ COUNTDOWN TIMER COMPONENT ============

@Composable
fun CountdownTimer(
    targetTime: LocalTime,
    modifier: Modifier = Modifier,
    onTimeUp: () -> Unit = {},
    onTickTime: (remainingSeconds: Long) -> Unit = {}
) {
    var remainingSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetTime) {
        while (true) {
            val now = LocalTime.now()
            val remaining = if (now < targetTime) {
                val target = java.time.LocalDateTime.of(java.time.LocalDate.now(), targetTime)
                val current = java.time.LocalDateTime.of(java.time.LocalDate.now(), now)
                (target.toLocalTime().toSecondOfDay() - current.toLocalTime().toSecondOfDay()).toLong()
            } else {
                0L
            }

            remainingSeconds = remaining
            onTickTime(remaining)

            if (remaining <= 0) {
                onTimeUp()
            }

            kotlinx.coroutines.delay(1000)
        }
    }

    TimerDisplay(
        seconds = remainingSeconds,
        modifier = modifier,
        isExpired = remainingSeconds <= 0
    )
}

/**
 * Timer display component
 */
@Composable
fun TimerDisplay(
    seconds: Long,
    modifier: Modifier = Modifier,
    isExpired: Boolean = false
) {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    val backgroundColor = if (isExpired) {
        Error.copy(alpha = 0.1f)
    } else if (seconds < 300) {
        Warning.copy(alpha = 0.1f)
    } else {
        Primary.copy(alpha = 0.1f)
    }

    val textColor = if (isExpired) {
        Error
    } else if (seconds < 300) {
        Warning
    } else {
        Primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, secs),
            style = MaterialTheme.typography.displaySmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 28.sp
        )
    }
}

/**
 * Simple timer with label and unit
 */
@Composable
fun SimpleTimer(
    seconds: Long,
    label: String,
    modifier: Modifier = Modifier,
    isExpired: Boolean = false,
    showMilliseconds: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        val displaySeconds = seconds / 60
        val displayMinutes = displaySeconds / 60
        val displayHours = displayMinutes / 60

        Text(
            text = if (showMilliseconds && seconds < 60) {
                String.format("%.2f s", seconds.toDouble() / 1000)
            } else if (displayHours > 0) {
                String.format("%dh %dm", displayHours, displayMinutes % 60)
            } else {
                String.format("%d:%02d", displayMinutes, (seconds / 60) % 60)
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (isExpired) Error else TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============ TRADING HOURS TIMER ============

@Composable
fun TradingHoursTimer(
    startTime: LocalTime = LocalTime.of(9, 15),
    endTime: LocalTime = LocalTime.of(15, 30),
    modifier: Modifier = Modifier,
    tradingMode: TradingMode = TradingMode.PAPER
) {
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    var isTradingActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalTime.now()
            isTradingActive = now in startTime..endTime

            val remaining = if (isTradingActive) {
                val target = java.time.LocalDateTime.of(java.time.LocalDate.now(), endTime)
                val current = java.time.LocalDateTime.of(java.time.LocalDate.now(), now)
                (target.toLocalTime().toSecondOfDay() - current.toLocalTime().toSecondOfDay()).toLong()
            } else if (now < startTime) {
                val target = java.time.LocalDateTime.of(java.time.LocalDate.now(), startTime)
                val current = java.time.LocalDateTime.of(java.time.LocalDate.now(), now)
                (target.toLocalTime().toSecondOfDay() - current.toLocalTime().toSecondOfDay()).toLong()
            } else {
                0L
            }

            remainingSeconds = remaining.coerceAtLeast(0)
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTradingActive) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isTradingActive) "MARKET OPEN" else "MARKET CLOSED",
            style = MaterialTheme.typography.labelSmall,
            color = if (isTradingActive) Success else Error,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isTradingActive) {
                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val secs = remainingSeconds % 60
                "Closes in ${String.format("%02d:%02d:%02d", hours, minutes, secs)}"
            } else {
                "Opens at ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============ TIME INPUT FIELD (for Strategy Config) ============

@Composable
fun TimeInputField(
    value: LocalTime,
    label: String,
    modifier: Modifier = Modifier,
    onTimeChange: (LocalTime) -> Unit = {},
    enabled: Boolean = true
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value.format(DateTimeFormatter.ofPattern("HH:mm")),
            onValueChange = {},
            label = { Text(label, fontSize = 12.sp) },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled) { isDialogOpen = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Select time",
                    tint = if (enabled) Primary else TextTertiary
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SurfaceVariant,
                disabledBorderColor = SurfaceVariant
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )

        if (isDialogOpen) {
            TimePickerDialog(
                initialTime = value,
                onTimeSelected = { time ->
                    onTimeChange(time)
                    isDialogOpen = false
                },
                onDismiss = { isDialogOpen = false }
            )
        }
    }
}

/**
 * Custom time picker dialog
 */
@Composable
fun TimePickerDialog(
    initialTime: LocalTime = LocalTime.now(),
    onTimeSelected: (LocalTime) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var selectedHour by remember { mutableIntStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Time", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    TimeSpinner(
                        value = selectedHour,
                        range = 0..23,
                        onValueChange = { selectedHour = it },
                        label = "Hour"
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )

                    // Minute picker
                    TimeSpinner(
                        value = selectedMinute,
                        range = 0..59,
                        step = 5,
                        onValueChange = { selectedMinute = it },
                        label = "Min"
                    )
                }

                // Display preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(selectedHour, selectedMinute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Surface
    )
}

/**
 * Time value spinner component
 */
@Composable
private fun TimeSpinner(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { onValueChange(((value + step - range.first) % (range.last - range.first + 1)) + range.first) }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = Primary
            )
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        IconButton(
            onClick = { onValueChange(((value - step - range.first) % (range.last - range.first + 1)) + range.first) }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = Primary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}
