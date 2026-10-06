package com.salarywise.app.ui.screens.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.SavingsGoalEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.FinancialCalculations
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class SavingsGoalViewItem(
    val goal: SavingsGoalEntity,
    val progressPercent: Double,
    val remainingAmount: Double,
    val estimatedMonthsLeft: Int
)

data class SavingsUiState(
    val user: UserEntity? = null,
    val currentSavings: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val monthlySavingsTarget: Double = 0.0,
    val totalTargetProgress: Double = 0.0,
    val goalItems: List<SavingsGoalViewItem> = emptyList(),
    val isLoading: Boolean = true
)

class SavingsViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadSavingsData()
    }

    private fun loadSavingsData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                repository.getAllGoalsFlow(user.id).collect { goals ->
                    val totalSaved = goals.sumOf { it.currentAmount }
                    val totalTarget = goals.sumOf { it.targetAmount }
                    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) * 100.0 else 0.0

                    val items = goals.map { g ->
                        val remaining = (g.targetAmount - g.currentAmount).coerceAtLeast(0.0)
                        val prog = FinancialCalculations.calculateGoalProgress(g.currentAmount, g.targetAmount)
                        val monthsLeft = if (g.monthlyContribution > 0) Math.ceil(remaining / g.monthlyContribution).toInt() else 0
                        SavingsGoalViewItem(
                            goal = g,
                            progressPercent = prog,
                            remainingAmount = remaining,
                            estimatedMonthsLeft = monthsLeft
                        )
                    }

                    _uiState.update {
                        it.copy(
                            user = user,
                            currentSavings = totalSaved.coerceAtLeast(user.currentSavings),
                            monthlySavings = user.monthlySavingsTarget,
                            savingsRate = if (user.monthlyInHandSalary > 0) (user.monthlySavingsTarget / user.monthlyInHandSalary) * 100.0 else 0.0,
                            monthlySavingsTarget = user.monthlySavingsTarget,
                            totalTargetProgress = overallProgress,
                            goalItems = items,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun addGoal(
        title: String,
        targetAmount: Double,
        initialAmount: Double,
        monthlyContribution: Double,
        category: String,
        isEmergencyFund: Boolean = false
    ) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val goal = SavingsGoalEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = title,
                targetAmount = targetAmount,
                currentAmount = initialAmount,
                monthlyContribution = monthlyContribution,
                category = category,
                isEmergencyFund = isEmergencyFund
            )
            repository.saveGoal(goal)
        }
    }

    fun contributeToGoal(goalId: String, amount: Double, note: String) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            repository.addGoalDeposit(goalId, user.id, amount, note)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }
}
