package com.flashtrack.app.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashtrack.app.data.datastore.UserPreferencesRepository
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "User",
    val greeting: String = "Good Morning,",
    val spending: Double = 0.0,
    val income: Double = 0.0,
    val availableBalance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val budget: Budget? = null,
    val safeToSpendPerDay: Double = 0.0,
    val scheduledTransactions: List<ScheduledTransaction> = emptyList(),
    val unsettledDebts: List<DebtPerson> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val budgetRepo: BudgetRepository,
    private val debtRepo: DebtRepository,
    private val scheduledRepo: ScheduledTransactionRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val now = Calendar.getInstance()
    private val startMs = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val endMs = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
            getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
    }.timeInMillis

    private fun greeting(): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when { h < 12 -> "Good Morning," ; h < 17 -> "Good Afternoon," ; else -> "Good Evening," }
    }

    val state: StateFlow<HomeUiState> = combine(
        prefs.preferences,
        transactionRepo.getTotalExpense(startMs, endMs),
        transactionRepo.getTotalIncome(startMs, endMs),
        accountRepo.getTotalBalance(),
        transactionRepo.getRecent(5),
        budgetRepo.getMonthlyBudget(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1),
        debtRepo.getUnsettledPersons()
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val userPrefs = arr[0] as com.flashtrack.app.data.datastore.UserPreferences
        val spending = arr[1] as Double
        val income = arr[2] as Double
        val balance = arr[3] as Double
        @Suppress("UNCHECKED_CAST")
        val recent = arr[4] as List<Transaction>
        val budget = arr[5] as Budget?
        @Suppress("UNCHECKED_CAST")
        val debts = arr[6] as List<DebtPerson>
        val remaining = (endMs - System.currentTimeMillis()).coerceAtLeast(0L)
        val remainingDays = remaining / 86_400_000.0
        val safe = if (budget != null && remainingDays > 0) budget.remaining / remainingDays else 0.0
        HomeUiState(
            isLoading = false, userName = userPrefs.userName, greeting = greeting(),
            spending = spending, income = income, availableBalance = balance,
            recentTransactions = recent, budget = budget,
            safeToSpendPerDay = safe, unsettledDebts = debts.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
