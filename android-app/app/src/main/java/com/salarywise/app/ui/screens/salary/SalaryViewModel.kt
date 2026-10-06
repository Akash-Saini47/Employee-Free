package com.salarywise.app.ui.screens.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.SalaryRecordEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class SalaryUiState(
    val user: UserEntity? = null,
    val salaries: List<SalaryRecordEntity> = emptyList(),
    val growthHistory: List<SalaryRecordEntity> = emptyList(),
    val latestSalary: Double = 0.0,
    val totalGrowthPercent: Double = 0.0,
    val averageDeductions: Double = 0.0,
    val isLoading: Boolean = true
)

class SalaryViewModel(private val repository: SalaryWiseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SalaryUiState())
    val uiState: StateFlow<SalaryUiState> = _uiState.asStateFlow()

    init {
        loadSalaries()
    }

    private fun loadSalaries() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                repository.getAllSalariesFlow(user.id).collect { list ->
                    val sortedAsc = list.sortedBy { it.monthYear }
                    val growthPercent = if (sortedAsc.size >= 2) {
                        val first = sortedAsc.first().inHandSalary
                        val last = sortedAsc.last().inHandSalary
                        if (first > 0) ((last - first) / first) * 100.0 else 0.0
                    } else 0.0

                    val avgDeductions = if (list.isNotEmpty()) list.map { it.deductions }.average() else 0.0

                    _uiState.update {
                        it.copy(
                            user = user,
                            salaries = list,
                            growthHistory = sortedAsc,
                            latestSalary = list.firstOrNull()?.inHandSalary ?: user.monthlyInHandSalary,
                            totalGrowthPercent = growthPercent,
                            averageDeductions = avgDeductions,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun saveSalary(
        monthYear: String,
        grossSalary: Double,
        deductions: Double,
        paymentDate: Long,
        notes: String
    ) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val inHand = (grossSalary - deductions).coerceAtLeast(0.0)
            val record = SalaryRecordEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                monthYear = monthYear,
                grossSalary = grossSalary,
                deductions = deductions,
                inHandSalary = inHand,
                paymentDate = paymentDate,
                notes = notes
            )
            repository.saveSalary(record)
        }
    }

    fun deleteSalary(id: String) {
        viewModelScope.launch {
            repository.deleteSalary(id)
        }
    }
}
