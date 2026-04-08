package com.flashtrack.app.presentation.features.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashtrack.app.data.datastore.UserPreferencesRepository
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Accounts List ViewModel ──────────────────────────────────────────────────

data class AccountsUiState(
    val isLoading: Boolean = true,
    val showBalance: Boolean = true,
    val bankAccounts: List<Account> = emptyList(),
    val creditCards: List<Account> = emptyList(),
    val wallets: List<Account> = emptyList(),
    val cashAccounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalAvailableCredit: Double = 0.0,
    val creditView: Int = 0
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountsUiState())
    val state: StateFlow<AccountsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepo.getAll(),
                accountRepo.getTotalBalance(),
                accountRepo.getTotalAvailableCredit(),
                prefs.preferences
            ) { accounts, balance, credit, userPrefs ->
                AccountsUiState(
                    isLoading = false,
                    showBalance = userPrefs.showBalance,
                    bankAccounts = accounts.filter { it.type == AccountType.BANK },
                    creditCards = accounts.filter { it.type == AccountType.CREDIT_CARD },
                    wallets = accounts.filter { it.type == AccountType.WALLET },
                    cashAccounts = accounts.filter { it.type == AccountType.CASH },
                    totalBalance = balance,
                    totalAvailableCredit = credit,
                    creditView = _state.value.creditView
                )
            }.collect { _state.value = it }
        }
    }

    fun toggleShowBalance() {
        viewModelScope.launch { prefs.updateShowBalance(!_state.value.showBalance) }
    }

    fun setCreditView(v: Int) = _state.update { it.copy(creditView = v) }
}

// ─── Add Account ViewModel ────────────────────────────────────────────────────

data class AddAccountState(
    val tabIndex: Int = 0,
    val name: String = "",
    val balance: String = "0",
    val totalLimit: String = "0",
    val availableLimit: String = "0",
    val billingCycleDay: Int = 1,
    val paymentDueDay: Int = 15,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val paymentModeRepo: PaymentModeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddAccountState())
    val state: StateFlow<AddAccountState> = _state.asStateFlow()

    // FIX 4: Channel-based one-shot event — never gets stuck as "true"
    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun setTab(idx: Int) = _state.update { AddAccountState(tabIndex = idx) } // reset all fields on tab switch
    fun setName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun setBalance(v: String) = _state.update { it.copy(balance = v) }
    fun setTotalLimit(v: String) = _state.update { it.copy(totalLimit = v) }
    fun setAvailableLimit(v: String) = _state.update { it.copy(availableLimit = v) }
    fun setBillingCycleDay(v: Int) = _state.update { it.copy(billingCycleDay = v) }
    fun setPaymentDueDay(v: Int) = _state.update { it.copy(paymentDueDay = v) }

    // FIX 4: Reset state fully so ViewModel can be reused for a second account
    fun resetForNewAccount() {
        _state.value = AddAccountState()
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Account name is required") }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val type = when (s.tabIndex) {
                0 -> AccountType.BANK
                1 -> AccountType.WALLET
                else -> AccountType.CREDIT_CARD
            }
            val entity = AccountEntity(
                name = s.name.trim(),
                type = type,
                balance = s.balance.toDoubleOrNull() ?: 0.0,
                totalCreditLimit = if (type == AccountType.CREDIT_CARD) s.totalLimit.toDoubleOrNull() else null,
                availableCreditLimit = if (type == AccountType.CREDIT_CARD) s.availableLimit.toDoubleOrNull() else null,
                billingCycleStartDate = s.billingCycleDay,
                paymentDueDay = s.paymentDueDay
            )
            accountRepo.addAccount(entity)
            _state.update { it.copy(isLoading = false) }
            // FIX 4: Send one-shot event, then reset state for potential next use
            _navigationEvent.send(Unit)
            resetForNewAccount()
        }
    }
}

// ─── Account Detail ViewModel ─────────────────────────────────────────────────

data class AccountDetailState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val tabIndex: Int = 0,
    val daysUntilNextBill: Int = 0
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountDetailState())
    val state: StateFlow<AccountDetailState> = _state.asStateFlow()

    fun loadAccount(accountId: Long) {
        viewModelScope.launch {
            combine(
                transactionRepo.getByAccount(accountId),
                flowOf(accountRepo.getById(accountId))
            ) { txns, account ->
                val cal = java.util.Calendar.getInstance()
                val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                val billingDay = account?.billingCycleStartDate ?: 1
                val daysUntil = if (billingDay >= dayOfMonth)
                    billingDay - dayOfMonth
                else
                    daysInMonth - dayOfMonth + billingDay

                AccountDetailState(
                    isLoading = false,
                    account = account,
                    transactions = txns,
                    tabIndex = _state.value.tabIndex,
                    daysUntilNextBill = daysUntil
                )
            }.collect { _state.value = it }
        }
    }

    fun setTab(idx: Int) = _state.update { it.copy(tabIndex = idx) }

    fun filteredTransactions(): List<Transaction> {
        val txns = _state.value.transactions
        return when (_state.value.tabIndex) {
            1 -> txns.filter { it.type == TransactionType.INCOME }
            2 -> txns.filter { it.type == TransactionType.EXPENSE }
            3 -> txns.filter { it.type == TransactionType.TRANSFER }
            else -> txns
        }
    }
}
