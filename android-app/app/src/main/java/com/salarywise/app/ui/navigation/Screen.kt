package com.salarywise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Onboarding : Screen("onboarding", "Welcome")
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Budget : Screen("budget", "Budget", Icons.Filled.PieChart, Icons.Outlined.PieChartOutline)
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Goals : Screen("goals", "Goals", Icons.Filled.Savings, Icons.Outlined.Savings)

    // Secondary & Modal screens
    object Salary : Screen("salary", "Salary Management")
    object Recurring : Screen("recurring", "Recurring Bills")
    object FinancialFreedom : Screen("financial_freedom", "Financial Freedom")
    object MonthlyReport : Screen("monthly_report", "Financial Report")
    object HealthScore : Screen("health_score", "Financial Health Score")
    object Notifications : Screen("notifications", "Notifications")
    object Profile : Screen("profile", "Profile & Settings")
    object PinLock : Screen("pin_lock", "Security Lock")

    companion object {
        val bottomNavItems = listOf(Home, Expenses, Budget, Analytics, Goals)
    }
}
