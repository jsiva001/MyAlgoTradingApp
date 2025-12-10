package com.trading.orb.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Positions : Screen("positions")
    object History : Screen("history")
    object Strategy : Screen("strategy")
    object More : Screen("more")
    object Risk : Screen("risk")
    object Logs : Screen("logs")
}

