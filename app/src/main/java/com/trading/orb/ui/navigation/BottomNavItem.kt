package com.trading.orb.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, Icons.Default.TrendingUp, "Dashboard"),
    BottomNavItem(Screen.Positions, Icons.Default.BarChart, "Positions"),
    BottomNavItem(Screen.History, Icons.Default.History, "History"),
    BottomNavItem(Screen.Strategy, Icons.Default.Settings, "Strategy"),
    BottomNavItem(Screen.More, Icons.Default.Menu, "More")
)

