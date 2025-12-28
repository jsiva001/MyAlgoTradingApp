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
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.utils.DatePickerDefaults
import com.trading.orb.ui.utils.DatePickerLabels
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
            horizontalArrangement = Arrangement.spacedBy(DATE_PICKER_SPACING)
        ) {
            // Start date field
            DateInputField(
                value = selectedStartDate,
                label = DatePickerLabels.START_DATE,
                modifier = Modifier.weight(1f),
                onDateChange = { selectedStartDate = it }
            )

            // End date field
            DateInputField(
                value = selectedEndDate,
                label = DatePickerLabels.END_DATE,
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
                .height(DATE_PICKER_BUTTON_HEIGHT),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(CORNER_RADIUS_MEDIUM)
        ) {
            Text(DatePickerLabels.APPLY_DATES, fontWeight = FontWeight.SemiBold)
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
            value = value.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DISPLAY)),
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
                    contentDescription = DatePickerLabels.SELECT_DATE_ICON,
                    tint = if (enabled) Primary else TextTertiary
                )
            },
            shape = RoundedCornerShape(CORNER_RADIUS_MEDIUM),
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
                DatePickerLabels.SELECT_DATE,
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

                Spacer(modifier = Modifier.height(CALENDAR_HEADER_SPACING))

                // Calendar grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(DATE_PICKER_DIALOG_BOTTOM_SPACING))

                // Selected date display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CORNER_RADIUS_MEDIUM))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(PADDING_EXTRA_SMALL),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_SELECTED)),
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
                Text(DatePickerLabels.CONFIRM)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(DatePickerLabels.CANCEL)
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
                contentDescription = DatePickerLabels.PREVIOUS_MONTH,
                tint = Primary
            )
        }

        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern(DATE_FORMAT_MONTH_YEAR)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = TextPrimary
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = DatePickerLabels.NEXT_MONTH,
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
    val firstDayOfWeek = firstDay.dayOfWeek.value % FIRST_DAY_OF_WEEK_ADJUSTMENT

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
                .padding(bottom = CALENDAR_GRID_PADDING_BOTTOM),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                DatePickerDefaults.DAYS_HEADER_SUN,
                DatePickerDefaults.DAYS_HEADER_MON,
                DatePickerDefaults.DAYS_HEADER_TUE,
                DatePickerDefaults.DAYS_HEADER_WED,
                DatePickerDefaults.DAYS_HEADER_THU,
                DatePickerDefaults.DAYS_HEADER_FRI,
                DatePickerDefaults.DAYS_HEADER_SAT
            ).forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(CALENDAR_DAY_PADDING),
                    fontSize = TEXT_SIZE_SMALL
                )
            }
        }

        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(CALENDAR_GRID_FIXED_COLUMNS),
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
                    Spacer(modifier = Modifier.size(CALENDAR_DAY_CIRCLE_SIZE))
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
            text = DatePickerLabels.QUICK_SELECT,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PADDING_EXTRA_SMALL)
        ) {
            QuickDateButton(
                label = DatePickerLabels.TODAY,
                startDate = today,
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = DatePickerLabels.SEVEN_DAYS,
                startDate = today.minusDays(7),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = DatePickerLabels.THIRTY_DAYS,
                startDate = today.minusDays(30),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PADDING_EXTRA_SMALL)
        ) {
            QuickDateButton(
                label = DatePickerLabels.THIS_MONTH,
                startDate = today.withDayOfMonth(1),
                endDate = today,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = DatePickerLabels.LAST_MONTH,
                startDate = today.minusMonths(1).withDayOfMonth(1),
                endDate = today.minusMonths(1).withDayOfMonth(
                    today.minusMonths(1).lengthOfMonth()
                ),
                onRangeSelected = onRangeSelected,
                modifier = Modifier.weight(1f)
            )

            QuickDateButton(
                label = DatePickerLabels.ALL_TIME,
                startDate = LocalDate.of(
                    DatePickerDefaults.ALL_TIME_START_YEAR,
                    DatePickerDefaults.ALL_TIME_START_MONTH,
                    DatePickerDefaults.ALL_TIME_START_DAY
                ),
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
        modifier = modifier.height(DATE_PICKER_QUICK_BUTTON_HEIGHT),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceVariant
        ),
        shape = RoundedCornerShape(CORNER_RADIUS_MEDIUM)
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
