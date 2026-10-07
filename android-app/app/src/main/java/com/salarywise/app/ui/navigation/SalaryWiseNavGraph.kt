package com.salarywise.app.ui.navigation

import android.database.sqlite.SQLiteException
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.ui.screens.analytics.AnalyticsScreen
import com.salarywise.app.ui.screens.analytics.AnalyticsViewModel
import com.salarywise.app.ui.screens.budget.BudgetScreen
import com.salarywise.app.ui.screens.budget.BudgetViewModel
import com.salarywise.app.ui.screens.expense.ExpenseScreen
import com.salarywise.app.ui.screens.expense.ExpenseViewModel
import com.salarywise.app.ui.screens.financialfreedom.FinancialFreedomScreen
import com.salarywise.app.ui.screens.financialfreedom.FinancialFreedomViewModel
import com.salarywise.app.ui.screens.healthscore.FinancialHealthScoreScreen
import com.salarywise.app.ui.screens.healthscore.FinancialHealthScoreViewModel
import com.salarywise.app.ui.screens.home.HomeScreen
import com.salarywise.app.ui.screens.home.HomeViewModel
import com.salarywise.app.ui.screens.notifications.NotificationCenterScreen
import com.salarywise.app.ui.screens.onboarding.OnboardingScreen
import com.salarywise.app.ui.screens.profile.ProfileScreen
import com.salarywise.app.ui.screens.profile.ProfileViewModel
import com.salarywise.app.ui.screens.recurring.RecurringExpenseScreen
import com.salarywise.app.ui.screens.recurring.RecurringViewModel
import com.salarywise.app.ui.screens.salary.SalaryScreen
import com.salarywise.app.ui.screens.salary.SalaryViewModel
import com.salarywise.app.ui.screens.savings.SavingsScreen
import com.salarywise.app.ui.screens.savings.SavingsViewModel
import com.salarywise.app.ui.screens.security.PinLockScreen
import com.salarywise.app.ui.screens.report.MonthlyReportScreen
import com.salarywise.app.ui.screens.report.MonthlyReportViewModel
import kotlinx.coroutines.launch

@Composable
fun SalaryWiseNavGraph(
    navController: NavHostController,
    repository: SalaryWiseRepository,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var onboardingError by remember { mutableStateOf<String?>(null) }
    var isCreatingProfile by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(currentUser?.pinHash == null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = currentRoute in listOf(
        Screen.Home.route,
        Screen.Expenses.route,
        Screen.Budget.route,
        Screen.Analytics.route,
        Screen.Goals.route
    )

    if (currentUser?.pinHash != null && !isUnlocked) {
        PinLockScreen(
            expectedPinHash = currentUser.pinHash,
            onUnlocked = { isUnlocked = true }
        )
        return
    }

    val startDestination = if (currentUser == null || !currentUser.isOnboardingCompleted) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                SalaryWiseBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    isCreating = isCreatingProfile,
                    errorMessage = onboardingError,
                    onComplete = { name, salary, date, savings, essential, target, efTarget ->
                        if (isCreatingProfile) return@OnboardingScreen
                        coroutineScope.launch {
                            isCreatingProfile = true
                            onboardingError = null
                            try {
                                repository.completeOnboarding(
                                    name, salary, date, savings, essential, target, efTarget
                                )
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                onboardingError = when (e) {
                                    is SQLiteException ->
                                        "We couldn't save your profile to the local database. Please try again."
                                    is IllegalArgumentException ->
                                        "Some profile values are invalid. Please check the numbers and try again."
                                    else ->
                                        "Profile creation failed. Your entered data was not saved. Please try again."
                                }
                            } finally {
                                isCreatingProfile = false
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeVm = remember { HomeViewModel(repository) }
                HomeScreen(
                    viewModel = homeVm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Expenses.route) {
                val expenseVm = remember { ExpenseViewModel(repository) }
                ExpenseScreen(
                    viewModel = expenseVm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Budget.route) {
                val budgetVm = remember { BudgetViewModel(repository) }
                BudgetScreen(
                    viewModel = budgetVm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Analytics.route) {
                val analyticsVm = remember { AnalyticsViewModel(repository) }
                AnalyticsScreen(
                    viewModel = analyticsVm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Goals.route) {
                val savingsVm = remember { SavingsViewModel(repository) }
                SavingsScreen(
                    viewModel = savingsVm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Salary.route) {
                val salaryVm = remember { SalaryViewModel(repository) }
                SalaryScreen(
                    viewModel = salaryVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Recurring.route) {
                val recurringVm = remember { RecurringViewModel(repository) }
                RecurringExpenseScreen(
                    viewModel = recurringVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.FinancialFreedom.route) {
                val ffVm = remember { FinancialFreedomViewModel(repository) }
                FinancialFreedomScreen(
                    viewModel = ffVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MonthlyReport.route) {
                val reportVm = remember { MonthlyReportViewModel(repository) }
                MonthlyReportScreen(
                    viewModel = reportVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.HealthScore.route) {
                val healthVm = remember { FinancialHealthScoreViewModel(repository) }
                FinancialHealthScoreScreen(
                    viewModel = healthVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationCenterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val profileVm = remember { ProfileViewModel(repository) }
                ProfileScreen(
                    viewModel = profileVm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPinLock = { navController.navigate(Screen.PinLock.route) },
                    onAccountDeleted = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
