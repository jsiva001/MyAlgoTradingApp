package com.trading.orb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.ConnectionStatus
import com.trading.orb.data.model.TradingMode
import com.trading.orb.ui.theme.Error
import com.trading.orb.ui.theme.OnPrimary
import com.trading.orb.ui.theme.Primary
import com.trading.orb.ui.theme.TextPrimary

@Composable
fun ModeBanner(tradingMode: TradingMode) {
    val (text, color) = when (tradingMode) {
        TradingMode.PAPER -> "📄 PAPER TRADING MODE" to Primary
        TradingMode.LIVE -> "🔴 LIVE TRADING MODE" to Error
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = OnPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    tradingMode: TradingMode?,
    connectionStatus: ConnectionStatus,
    canNavigateBack: Boolean,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                if (tradingMode != null) {
                    val (modeText, containerColor, contentColor) = when (tradingMode) {
                        TradingMode.PAPER -> Triple("PAPER", Primary, OnPrimary)
                        TradingMode.LIVE -> Triple("LIVE", Error, OnPrimary)
                    }
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = containerColor,
                    ) {
                        Text(
                            text = modeText,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        actions = {
            ConnectionIndicator(
                status = connectionStatus,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = com.trading.orb.ui.theme.Surface,
            titleContentColor = TextPrimary
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        modifier = modifier
    )
}
