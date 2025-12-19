package com.trading.orb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.orb.data.model.*
import com.trading.orb.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * Standard card container
 */
@Composable
fun OrbCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Section header
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Status badge
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * P&L display with color coding
 */
@Composable
fun PnLDisplay(
    pnl: Double,
    modifier: Modifier = Modifier,
    percentage: Double? = null,
    fontSize: Int = 24
) {
    val color = if (pnl >= 0) ProfitColor else LossColor
    val sign = if (pnl >= 0) "+" else ""

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "$sign₹${String.format("%.2f", pnl)}",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = fontSize.sp),
            color = color,
            fontWeight = FontWeight.Bold
        )
        if (percentage != null) {
            Text(
                text = "$sign${String.format("%.2f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

/**
 * Connection indicator
 */
@Composable
fun ConnectionIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when (status) {
                        ConnectionStatus.CONNECTED -> Success
                        ConnectionStatus.CONNECTING -> Warning
                        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> Error
                    }
                )
        )
        Text(
            text = when (status) {
                ConnectionStatus.CONNECTED -> "Connected"
                ConnectionStatus.CONNECTING -> "Connecting"
                ConnectionStatus.DISCONNECTED -> "Disconnected"
                ConnectionStatus.ERROR -> "Error"
            },
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

/**
 * Order side badge (Buy/Sell)
 */
@Composable
fun OrderSideBadge(
    side: OrderSide,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (side) {
        OrderSide.BUY -> Icons.Default.ArrowUpward to BuyColor
        OrderSide.SELL -> Icons.Default.ArrowDownward to SellColor
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = side.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Progress bar with label
 */
@Composable
fun LabeledProgressBar(
    label: String,
    current: Double,
    max: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "₹${String.format("%.0f", current)} / ₹${String.format("%.0f", max)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (current / max).toFloat().coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = SurfaceVariant
        )
    }
}

/**
 * Exit reason badge
 */
@Composable
fun ExitReasonBadge(
    reason: ExitReason,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (reason) {
        ExitReason.TARGET_HIT -> Triple(Icons.Default.CheckCircle, Success, "Target Hit")
        ExitReason.SL_HIT -> Triple(Icons.Default.Warning, Error, "SL Hit")
        ExitReason.TIME_EXIT -> Triple(Icons.Default.AccessTime, Primary, "Time Exit")
        ExitReason.MANUAL -> Triple(Icons.Default.TouchApp, Warning, "Manual")
        ExitReason.MANUAL_EXIT -> Triple(Icons.Default.TouchApp, Warning, "Manual Exit")
        ExitReason.EMERGENCY_EXIT -> Triple(Icons.Default.PowerSettingsNew, Error, "Emergency Stop")
        ExitReason.CIRCUIT_BREAKER -> Triple(Icons.Default.Block, Error, "Circuit Breaker")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary
            )
        }
    }
}

/**
 * Info row (label: value)
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Stat card (for dashboard)
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    OrbCard(modifier = modifier.height(100.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Risk threshold progress bar with automatic color coding
 * Color changes based on percentage thresholds:
 * - Green: < 50% (Safe)
 * - Yellow: 50-80% (Warning)
 * - Red: >= 80% (Critical)
 *
 * @param label Display label for the progress bar
 * @param current Current value
 * @param max Maximum value
 * @param warningThreshold Percentage at which to show warning (default 50)
 * @param criticalThreshold Percentage at which to show critical (default 80)
 * @param overrideColor Optional color to override automatic color coding
 */
@Composable
fun RiskThresholdProgressBar(
    label: String,
    current: Double,
    max: Double,
    warningThreshold: Double = 50.0,
    criticalThreshold: Double = 80.0,
    modifier: Modifier = Modifier,
    overrideColor: Color? = null
) {
    val percentage = if (max > 0) (current / max) * 100 else 0.0

    val color = overrideColor ?: when {
        percentage < warningThreshold -> Success
        percentage < criticalThreshold -> Warning
        else -> Error
    }

    LabeledProgressBar(
        label = label,
        current = current,
        max = max,
        color = color,
        modifier = modifier
    )
}

/**
 * Time formatter utilities
 */
object TimeFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a")

    fun formatTime(dateTime: java.time.LocalDateTime): String {
        return dateTime.format(timeFormatter)
    }

    fun formatDate(dateTime: java.time.LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }

    fun formatDateTime(dateTime: java.time.LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
}

/**
 * Common number input field with theme-aware colors
 */
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null) }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            { Icon(trailingIcon, contentDescription = null) }
        } else null,
        readOnly = readOnly
    )
}

/**
 * Currency input field (for rupees)
 */
@Composable
fun CurrencyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    NumberInputField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        leadingIcon = Icons.Default.CurrencyRupee
    )
}

/**
 * Percentage input field
 */
@Composable
fun PercentageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    NumberInputField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        trailingIcon = Icons.Default.Percent
    )
}

/**
 * Timer/duration input field
 */
@Composable
fun TimerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    NumberInputField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        trailingIcon = Icons.Default.Timer
    )
}

/**
 * Time selector field (read-only)
 */
@Composable
fun TimeField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onTimeChange: (String) -> Unit = { }
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.Schedule, contentDescription = null)
        }
    )
}

/**
 * Text search field
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        trailingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        }
    )
}
