package com.trading.orb.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.orb.data.model.TradingMode
import com.trading.orb.ui.theme.*

// ============ DIALOG TEMPLATES ============

/**
 * Base dialog template with mode-aware styling
 */
@Composable
fun OrbDialog(
    isVisible: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    tradingMode: TradingMode = TradingMode.PAPER,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                DialogHeader(
                    title = title,
                    tradingMode = tradingMode
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content
                )
            },
            confirmButton = {},
            containerColor = Surface,
            modifier = modifier.fillMaxWidth(0.9f)
        )
    }
}

/**
 * Dialog header with mode-aware color
 */
@Composable
private fun DialogHeader(
    title: String,
    tradingMode: TradingMode
) {
    val modeColor = if (tradingMode == TradingMode.PAPER) {
        PaperPrimary
    } else {
        LivePrimary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = modeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = modeColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Mode: ${tradingMode.name}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

// ============ CONFIRMATION DIALOG ============

@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    tradingMode: TradingMode = TradingMode.PAPER,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDangerous: Boolean = false,
    onConfirm: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDangerous) Error else Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = confirmText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = cancelText,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Surface
        )
    }
}

// ============ INFO DIALOG ============

@Composable
fun InfoDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    tradingMode: TradingMode = TradingMode.PAPER,
    icon: ImageVector = Icons.Default.Info,
    buttonText: String = "OK",
    onDismiss: () -> Unit = {}
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Surface
        )
    }
}

// ============ ERROR DIALOG WITH RETRY ============

@Composable
fun ErrorDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    tradingMode: TradingMode = TradingMode.PAPER,
    errorCode: String? = null,
    showRetry: Boolean = true,
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorCode != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error Code: $errorCode",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (showRetry) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                if (showRetry) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            containerColor = Surface
        )
    }
}

// ============ LOADING DIALOG ============

@Composable
fun LoadingDialog(
    isVisible: Boolean,
    message: String = "Loading...",
    tradingMode: TradingMode = TradingMode.PAPER
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = {},
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = if (tradingMode == TradingMode.PAPER) {
                            PaperPrimary
                        } else {
                            LivePrimary
                        },
                        strokeWidth = 4.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            containerColor = Surface
        )
    }
}

// ============ SUCCESS DIALOG WITH ANIMATION ============

@Composable
fun SuccessDialog(
    isVisible: Boolean,
    title: String = "Success",
    message: String,
    tradingMode: TradingMode = TradingMode.PAPER,
    autoCloseMs: Long? = 2000,
    onDismiss: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        AlertDialog(
            onDismissRequest = { onDismiss(); onClose() },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Success,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = Success.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (autoCloseMs != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto closing...",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDismiss(); onClose() },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Surface
        )
    }

    // Auto-close timer
    if (isVisible && autoCloseMs != null) {
        LaunchedEffect(isVisible) {
            kotlinx.coroutines.delay(autoCloseMs)
            onClose()
        }
    }
}
