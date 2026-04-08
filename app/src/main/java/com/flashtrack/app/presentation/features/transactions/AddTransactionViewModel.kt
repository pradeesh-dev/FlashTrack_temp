package com.flashtrack.app.presentation.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AddTransactionState(
    val isEditMode: Boolean = false,
    val editingTransactionId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val selectedCategory: Category? = null,
    val selectedPaymentMode: PaymentMode? = null,
    val selectedAccount: Account? = null,
    val toAccount: Account? = null,
    val note: String = "",
    val selectedTagIds: List<Long> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val allPaymentModes: List<PaymentMode> = emptyList(),
    val allAccounts: List<Account> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val showPaymentSheet: Boolean = false,
    val showCategorySheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val paymentModeRepo: PaymentModeRepository,
    private val accountRepo: AccountRepository,
    private val tagRepo: TagRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    // FIX 3: One-shot navigation channel
    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepo.getAll(),
                paymentModeRepo.getAll(),
                accountRepo.getAll(),
                tagRepo.getAll()
            ) { cats, pms, accs, tags ->
                val t = _state.value.type
                val defaultPm = pms.firstOrNull { it.isDefault } ?: pms.firstOrNull()
                val defaultCat = cats.filter { it.type == t }
                    .firstOrNull { it.name == "Others" } ?: cats.filter { it.type == t }.firstOrNull()
                _state.update { s ->
                    s.copy(
                        allCategories = cats,
                        allPaymentModes = pms,
                        allAccounts = accs,
                        allTags = tags,
                        selectedPaymentMode = s.selectedPaymentMode ?: defaultPm,
                        selectedCategory = s.selectedCategory ?: defaultCat,
                        selectedAccount = s.selectedAccount ?: accs.firstOrNull()
                    )
                }
            }.collect()
        }
    }

    // FIX 3: Load existing transaction for editing
    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val txn = transactionRepo.getById(transactionId) ?: return@launch
            _state.update { s ->
                s.copy(
                    isEditMode = true,
                    editingTransactionId = transactionId,
                    type = txn.type,
                    amount = txn.amount.toString(),
                    date = txn.date,
                    selectedCategory = txn.category,
                    selectedPaymentMode = txn.paymentMode,
                    selectedAccount = txn.account,
                    toAccount = txn.toAccount,
                    note = txn.note,
                    selectedTagIds = txn.tags.map { it.id }
                )
            }
        }
    }

    fun setType(type: TransactionType) {
        _state.update { s ->
            val defaultCat = s.allCategories.filter { it.type == type }
                .firstOrNull { it.name == "Others" }
                ?: s.allCategories.filter { it.type == type }.firstOrNull()
            // FIX 10: Only reset category when switching type, keep if same type
            val cat = if (s.type == type) s.selectedCategory else defaultCat
            s.copy(type = type, selectedCategory = cat)
        }
    }

    fun setAmount(v: String) = _state.update { it.copy(amount = v, error = null) }
    fun setDate(ms: Long) = _state.update { it.copy(date = ms) }
    fun setCategory(cat: Category) = _state.update { it.copy(selectedCategory = cat, showCategorySheet = false) }
    fun setPaymentMode(pm: PaymentMode) = _state.update { it.copy(selectedPaymentMode = pm, showPaymentSheet = false) }
    fun setNote(v: String) = _state.update { it.copy(note = v) }
    fun toggleTag(id: Long) = _state.update {
        val tags = if (it.selectedTagIds.contains(id)) it.selectedTagIds - id else it.selectedTagIds + id
        it.copy(selectedTagIds = tags)
    }
    fun showPaymentSheet() = _state.update { it.copy(showPaymentSheet = true) }
    fun hidePaymentSheet() = _state.update { it.copy(showPaymentSheet = false) }
    fun showCategorySheet() = _state.update { it.copy(showCategorySheet = true) }
    fun hideCategorySheet() = _state.update { it.copy(showCategorySheet = false) }

    fun setInitialType(type: String) {
        val t = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE }
        setType(t)
    }

    fun save() {
        val s = _state.value
        val amt = s.amount.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            _state.update { it.copy(error = "Enter a valid amount") }
            return
        }
        val cat = s.selectedCategory
        val pm = s.selectedPaymentMode
        val acc = s.selectedAccount
        if (cat == null || pm == null || acc == null) {
            _state.update { it.copy(error = "Please select category and payment mode") }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = s.editingTransactionId ?: 0L,
                amount = amt,
                type = s.type,
                categoryId = cat.id,
                paymentModeId = pm.id,
                accountId = acc.id,
                toAccountId = s.toAccount?.id,
                note = s.note,
                date = s.date,
                tagIds = s.selectedTagIds.joinToString(",")
            )
            if (s.isEditMode) {
                transactionRepo.updateTransaction(entity)
            } else {
                transactionRepo.addTransaction(entity)
            }
            _state.update { it.copy(isLoading = false) }
            _navigationEvent.send(Unit)
        }
    }
}
