package com.trading.orb.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.TEXT_SIZE_SMALL
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ShowValidationDialog(
    title: String = DialogMessages.VALIDATION_ERROR,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(DialogMessages.OK)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Time picker dialog (24-hour format)
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

                    TimeSpinner(
                        value = selectedMinute,
                        range = 0..59,
                        step = 5,
                        onValueChange = { selectedMinute = it },
                        label = "Min"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
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
 * AM/PM time picker dialog with time range constraints
 */
@Composable
fun TimePickerDialogAMPM(
    initialTime: LocalTime = LocalTime.now(),
    onTimeSelected: (LocalTime) -> Unit = {},
    onDismiss: () -> Unit = {},
    minTime: LocalTime? = null,
    maxTime: LocalTime? = null
) {
    var selectedHour by remember { mutableIntStateOf(
        when {
            initialTime.hour == 0 -> 12
            initialTime.hour > 12 -> initialTime.hour - 12
            else -> initialTime.hour
        }
    ) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }
    var isPM by remember { mutableStateOf(initialTime.hour >= 12) }

    fun get24HourTime(hour12: Int, min: Int, isPmVal: Boolean): LocalTime {
        val hour24 = when {
            isPmVal && hour12 != 12 -> hour12 + 12
            !isPmVal && hour12 == 12 -> 0
            else -> hour12
        }
        return LocalTime.of(hour24, min)
    }

    fun isTimeValid(hour12: Int, min: Int, isPmVal: Boolean): Boolean {
        val time = get24HourTime(hour12, min, isPmVal)
        val isAfterMin = minTime?.let { time >= it } ?: true
        val isBeforeMax = maxTime?.let { time <= it } ?: true
        return isAfterMin && isBeforeMax
    }

    fun getValidHours(): IntRange {
        val min = minTime?.hour ?: 0
        val max = maxTime?.hour ?: 23
        
        val minHour12 = when {
            min == 0 -> 12
            min > 12 -> min - 12
            else -> min
        }
        
        val maxHour12 = when {
            max == 0 -> 12
            max > 12 -> max - 12
            else -> max
        }
        
        return if (minHour12 <= maxHour12) minHour12..maxHour12 else 1..12
    }

    fun getValidMinutes(): IntRange {
        val validHours = getValidHours()
        
        val minMinutes = if (selectedHour == validHours.first && minTime != null) {
            minTime.minute
        } else {
            0
        }
        
        val maxMinutes = if (selectedHour == validHours.last && maxTime != null) {
            maxTime.minute
        } else {
            59
        }
        
        return minMinutes..maxMinutes
    }

    val validHours = getValidHours()
    val validMinutes = getValidMinutes()

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
                    TimeSpinner(
                        value = selectedHour,
                        range = validHours,
                        onValueChange = { selectedHour = it },
                        label = "Hour"
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )

                    TimeSpinner(
                        value = selectedMinute,
                        range = validMinutes,
                        step = 5,
                        onValueChange = { selectedMinute = it },
                        label = "Min"
                    )

                    Text(
                        text = if (isPM) "PM" else "AM",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isPM = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPM) Primary else SurfaceVariant
                        )
                    ) {
                        Text("AM", color = if (!isPM) Surface else TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { isPM = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPM) Primary else SurfaceVariant
                        )
                    ) {
                        Text("PM", color = if (isPM) Surface else TextPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d %s", selectedHour, selectedMinute, if (isPM) "PM" else "AM"),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                
                if (minTime != null && maxTime != null) {
                    Text(
                        text = "Range: ${minTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${maxTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(get24HourTime(selectedHour, selectedMinute, isPM))
                },
                enabled = isTimeValid(selectedHour, selectedMinute, isPM)
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
 * Number picker dialog for selecting values in a range (e.g., 1-10)
 */
@Composable
fun NumberPickerDialog(
    initialValue: Int = 5,
    range: IntRange = 1..10,
    onValueSelected: (Int) -> Unit = {},
    onDismiss: () -> Unit = {},
    title: String = "Select Number"
) {
    var selectedValue by remember { mutableIntStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedValue > range.first) {
                                selectedValue--
                            }
                        },
                        enabled = selectedValue > range.first
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Decrease",
                            tint = if (selectedValue > range.first) Primary else Primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(PADDING_EXTRA_LARGE))

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
                            .background(SurfaceVariant)
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedValue.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(PADDING_EXTRA_LARGE))

                    IconButton(
                        onClick = {
                            if (selectedValue < range.last) {
                                selectedValue++
                            }
                        },
                        enabled = selectedValue < range.last
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Increase",
                            tint = if (selectedValue < range.last) Primary else Primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Range: ${range.first} - ${range.last}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onValueSelected(selectedValue)
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
            onClick = { 
                val newValue = value + step
                if (newValue <= range.last) {
                    onValueChange(newValue)
                }
            },
            enabled = value < range.last
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = if (value < range.last) Primary else Primary.copy(alpha = 0.3f)
            )
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
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
            onClick = { 
                val newValue = value - step
                if (newValue >= range.first) {
                    onValueChange(newValue)
                }
            },
            enabled = value > range.first
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = if (value > range.first) Primary else Primary.copy(alpha = 0.3f)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = TEXT_SIZE_SMALL
        )
    }
}
