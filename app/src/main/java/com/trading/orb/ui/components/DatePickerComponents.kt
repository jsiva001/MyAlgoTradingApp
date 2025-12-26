package com.trading.orb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.CORNER_RADIUS_MEDIUM
import com.trading.orb.ui.utils.TEXT_SIZE_SMALL
import com.trading.orb.ui.utils.PADDING_EXTRA_SMALL
import com.trading.orb.ui.utils.PADDING_SMALL
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// ============ DATE RANGE PICKER ============

@Composable
fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    onDateRangeSelected: (start: LocalDate, end: LocalDate) -> Unit = { _, _ -> }
) {
    var selectedStartDate by remember { mutableStateOf(startDate) }
    var selectedEndDate by remember { mutableStateOf(endDate) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start date field
            DateInputField(
                value = selectedStartDate,
                label = "Start Date",
                modifier = Modifier.weight(1f),
                onDateChange = { selectedStartDate = it }
            )

            // End date field
            DateInputField(
                value = selectedEndDate,
                label = "End Date",
                modifier = Modifier.weight(1f),
                onDateChange = { selectedEndDate = it }
            )
        }

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        Button(
            onClick = { 
                onDateRangeSelected(selectedStartDate, selectedEndDate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Apply Dates", fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Single date input field with picker
 */
@Composable
fun DateInputField(
    value: LocalDate,
    label: String,
    modifier: Modifier = Modifier,
    onDateChange: (LocalDate) -> Unit = {},
    enabled: Boolean = true
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            onValueChange = {},
            label = { Text(label, fontSize = TEXT_SIZE_SMALL) },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled) { isDialogOpen = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Select date",
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
            CalendarPickerDialog(
                initialDate = value,
                onDateSelected = { date ->
                    onDateChange(date)
                    isDialogOpen = false
                },
                onDismiss = { isDialogOpen = false }
            )
        }
    }
}

/**
 * Custom calendar picker dialog
 */
@Composable
fun CalendarPickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Date",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month/Year header
                MonthYearHeader(
                    yearMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calendar grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selected date display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected(selectedDate) }
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
 * Month/Year header with navigation
 */
@Composable
private fun MonthYearHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month",
                tint = Primary
            )
        }

        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = TextPrimary
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month",
                tint = Primary
            )
        }
    }
}

/**
 * Calendar grid showing days of month
 */
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()
    val daysInMonth = lastDay.dayOfMonth
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0 = Sunday, 6 = Saturday

    val dayList = mutableListOf<LocalDate?>()

    // Add empty cells for days before month starts
    repeat(firstDayOfWeek) { dayList.add(null) }

    // Add all days of the month
    for (day in 1..daysInMonth) {
        dayList.add(yearMonth.atDay(day))
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Day of week headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    fontSize = TEXT_SIZE_SMALL
                )
            }
        }

        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PADDING_EXTRA_SMALL),
            verticalArrangement = Arrangement.spacedBy(PADDING_EXTRA_SMALL)
        ) {
            items(dayList.size) { index ->
                val date = dayList[index]
                if (date != null) {
                    CalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) }
                    )
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

/**
 * Individual calendar day cell
 */
@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isToday = date == LocalDate.now()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> Primary
                    isToday -> Primary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isSelected -> Color.White
                isToday -> Primary
                else -> TextPrimary
            },
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Quick date range selector with preset options
 */
@Composable
fun QuickDateRangeSelector(
    onRangeSelected: (start: LocalDate, end: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateButton(
                label = "Today",
                startDate = today,
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = "7 Days",
                startDate = today.minusDays(7),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = "30 Days",
                startDate = today.minusDays(30),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateButton(
                label = "This Month",
                startDate = today.withDayOfMonth(1),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = "Last Month",
                startDate = today.minusMonths(1).withDayOfMonth(1),
                endDate = today.minusMonths(1).withDayOfMonth(
                    today.minusMonths(1).lengthOfMonth()
                ),
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = "All Time",
                startDate = LocalDate.of(2020, 1, 1),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Quick date range button
 */
@Composable
private fun QuickDateButton(
    label: String,
    startDate: LocalDate,
    endDate: LocalDate,
    onRangeSelected: (start: LocalDate, end: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onRangeSelected(startDate, endDate) },
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            fontSize = TEXT_SIZE_SMALL
        )
    }
}
