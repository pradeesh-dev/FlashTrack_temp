package com.flashtrack.app.presentation.features.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashtrack.app.data.local.entity.TransactionType
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class AnalysisPeriod { WEEK, MONTH, YEAR, CUSTOM }

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val period: AnalysisPeriod = AnalysisPeriod.MONTH,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val transactionCount: Int = 0,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val budget: Budget? = null,
    val safeToSpend: Double = 0.0,
    val dailyExpenses: List<DailyAmount> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val paymentModeSpending: List<PaymentModeSpending> = emptyList(),
    val predictedExpense: Double = 0.0,
    val avgDailyExpense: Double = 0.0,
    val avgTxnExpense: Double = 0.0,
    val avgDailyIncome: Double = 0.0,
    val avgTxnIncome: Double = 0.0,
    val compareWithPrev: Boolean = false,
    val categoryTab: Int = 0,  // 0=Spending 1=Income
    val paymentTab: Int = 0    // 0=Spending 1=Income 2=Transfers
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val categoryRepo: CategoryRepository,
    private val paymentModeRepo: PaymentModeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisUiState())
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    init { loadData() }

    fun setPeriod(p: AnalysisPeriod) { _state.update { it.copy(period = p) }; loadData() }
    fun prevPeriod() { adjustPeriod(-1) }
    fun nextPeriod() { adjustPeriod(1) }
    fun toggleCompare() { _state.update { it.copy(compareWithPrev = !it.compareWithPrev) } }
    fun setCategoryTab(t: Int) { _state.update { it.copy(categoryTab = t) } }
    fun setPaymentTab(t: Int) { _state.update { it.copy(paymentTab = t) } }

    private fun adjustPeriod(dir: Int) {
        val s = _state.value
        when (s.period) {
            AnalysisPeriod.MONTH -> {
                val newMonth = s.month + dir
                if (newMonth < 1) _state.update { it.copy(month = 12, year = s.year - 1) }
                else if (newMonth > 12) _state.update { it.copy(month = 1, year = s.year + 1) }
                else _state.update { it.copy(month = newMonth) }
            }
            AnalysisPeriod.YEAR -> _state.update { it.copy(year = s.year + dir) }
            else -> {}
        }
        loadData()
    }

    // FIX 8: Track job so we can cancel before launching a new one
    private var loadJob: kotlinx.coroutines.Job? = null

    private fun loadData() {
        loadJob?.cancel()          // cancel any in-flight load
        loadJob = viewModelScope.launch {
            val s = _state.value
            val (startMs, endMs) = dateRange(s)
            val daysInPeriod = ((endMs - startMs) / 86_400_000.0).coerceAtLeast(1.0)
            val daysElapsed = ((System.currentTimeMillis() - startMs) / 86_400_000.0).coerceIn(1.0, daysInPeriod)
            val remainingDays = ((endMs - System.currentTimeMillis()) / 86_400_000.0).coerceAtLeast(0.0)

            combine(
                transactionRepo.getByDateRange(startMs, endMs),
                budgetRepo.getMonthlyBudget(s.year, s.month),
                paymentModeRepo.getAll()
            ) { transactions, budget, allPMs ->
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val incomes = transactions.filter { it.type == TransactionType.INCOME }
                val totalExp = expenses.sumOf { it.amount }
                val totalInc = incomes.sumOf { it.amount }

                // Daily breakdown
                val dailyMap = mutableMapOf<Int, Double>()
                expenses.forEach { t ->
                    val cal = Calendar.getInstance().also { c -> c.timeInMillis = t.date }
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    dailyMap[day] = (dailyMap[day] ?: 0.0) + t.amount
                }
                val dailyList = dailyMap.entries.sortedBy { it.key }
                    .map { DailyAmount(dayOfMonth = it.key, date = 0L, amount = it.value) }

                // Category breakdown
                val catMap = mutableMapOf<Long, Double>()
                expenses.forEach { catMap[it.category.id] = (catMap[it.category.id] ?: 0.0) + it.amount }
                val catSpending = catMap.entries
                    .map { (catId, amt) ->
                        val cat = expenses.first { it.category.id == catId }.category
                        CategorySpending(cat, amt, if (totalExp > 0) (amt / totalExp * 100).toFloat() else 0f)
                    }.sortedByDescending { it.amount }

                // Payment mode breakdown
                val pmMap = mutableMapOf<Long, Double>()
                expenses.forEach { pmMap[it.paymentMode.id] = (pmMap[it.paymentMode.id] ?: 0.0) + it.amount }
                val pmSpending = pmMap.entries
                    .map { (pmId, amt) ->
                        val pm = expenses.first { it.paymentMode.id == pmId }.paymentMode
                        PaymentModeSpending(pm, amt)
                    }.sortedByDescending { it.amount }

                // Predicted
                val predicted = if (daysElapsed > 0) (totalExp / daysElapsed) * daysInPeriod else 0.0
                val safe = if (budget != null && remainingDays > 0) budget.remaining / remainingDays else 0.0

                _state.update { cur -> cur.copy(
                    isLoading = false,
                    totalExpense = totalExp, totalIncome = totalInc,
                    transactionCount = transactions.size,
                    budget = budget, safeToSpend = safe,
                    dailyExpenses = dailyList,
                    categorySpending = catSpending,
                    paymentModeSpending = pmSpending,
                    predictedExpense = predicted,
                    avgDailyExpense = if (daysElapsed > 0) totalExp / daysElapsed else 0.0,
                    avgTxnExpense = if (expenses.isNotEmpty()) totalExp / expenses.size else 0.0,
                    avgDailyIncome = if (daysElapsed > 0) totalInc / daysElapsed else 0.0,
                    avgTxnIncome = if (incomes.isNotEmpty()) totalInc / incomes.size else 0.0
                ) }
            }.collect()
        }
    }

    private fun dateRange(s: AnalysisUiState): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (s.period) {
            AnalysisPeriod.MONTH -> {
                cal.set(s.year, s.month - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(s.year, s.month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                Pair(start, cal.timeInMillis)
            }
            AnalysisPeriod.YEAR -> {
                cal.set(s.year, 0, 1, 0, 0, 0); val start = cal.timeInMillis
                cal.set(s.year, 11, 31, 23, 59, 59)
                Pair(start, cal.timeInMillis)
            }
            AnalysisPeriod.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                Pair(start, start + 7 * 86_400_000L)
            }
            else -> Pair(0L, System.currentTimeMillis())
        }
    }
}
