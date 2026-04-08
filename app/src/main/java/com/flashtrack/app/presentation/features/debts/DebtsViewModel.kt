package com.flashtrack.app.presentation.features.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Debts List ViewModel ─────────────────────────────────────────────────────

data class DebtsUiState(
    val isLoading: Boolean = true,
    val tabIndex: Int = 0,
    val allPersons: List<DebtPerson> = emptyList(),
    val totalPayable: Double = 0.0,
    val totalReceivable: Double = 0.0
) {
    val filteredPersons: List<DebtPerson> get() = when (tabIndex) {
        1 -> allPersons.filter { it.type == DebtType.LENDING }
        2 -> allPersons.filter { it.type == DebtType.BORROWING }
        else -> allPersons
    }
}

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepo: DebtRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DebtsUiState())
    val state: StateFlow<DebtsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            debtRepo.getUnsettledPersons().collect { persons ->
                val payable = persons.filter { it.type == DebtType.BORROWING }.sumOf { it.displayAmount }
                val receivable = persons.filter { it.type == DebtType.LENDING }.sumOf { it.displayAmount }
                _state.update {
                    it.copy(isLoading = false, allPersons = persons,
                        totalPayable = payable, totalReceivable = receivable)
                }
            }
        }
    }

    fun setTab(idx: Int) = _state.update { it.copy(tabIndex = idx) }
}

// ─── Debt Detail ViewModel ────────────────────────────────────────────────────

data class DebtDetailState(
    val isLoading: Boolean = true,
    val person: DebtPerson? = null,
    val records: List<DebtRecord> = emptyList(),
    val tabIndex: Int = 0
)

@HiltViewModel
class DebtDetailViewModel @Inject constructor(
    private val debtRepo: DebtRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DebtDetailState())
    val state: StateFlow<DebtDetailState> = _state.asStateFlow()

    fun loadPerson(personId: Long) {
        viewModelScope.launch {
            combine(
                flowOf(debtRepo.getPersonById(personId)),
                debtRepo.getRecordsByPerson(personId)
            ) { person, records ->
                DebtDetailState(
                    isLoading = false,
                    person = person,
                    records = records,
                    tabIndex = _state.value.tabIndex
                )
            }.collect { _state.value = it }
        }
    }

    fun setTab(idx: Int) = _state.update { it.copy(tabIndex = idx) }

    fun filteredRecords(): List<DebtRecord> = when (_state.value.tabIndex) {
        1 -> _state.value.records.filter { it.recordType == DebtRecordType.PAID }
        2 -> _state.value.records.filter { it.recordType == DebtRecordType.RECEIVED }
        else -> _state.value.records
    }

    fun addRecord(personId: Long, amount: Double, type: DebtRecordType, note: String) {
        viewModelScope.launch {
            debtRepo.addRecord(DebtRecordEntity(
                personId = personId, amount = amount,
                recordType = type, note = note
            ))
        }
    }

    fun deletePerson(person: DebtPerson, onDone: () -> Unit) {
        viewModelScope.launch {
            debtRepo.deletePerson(DebtPersonEntity(
                id = person.id,
                name = person.name,
                type = person.type,
                initialAmount = person.initialAmount,
                dueDate = person.dueDate,
                note = person.note,
                isSettled = person.isSettled
            ))
            onDone()
        }
    }
}

// ─── Add Debt ViewModel ───────────────────────────────────────────────────────

data class AddDebtState(
    val debtType: DebtType = DebtType.LENDING,
    val name: String = "",
    val dueDate: Long? = null,
    val note: String = "",
    val error: String? = null
)

@HiltViewModel
class AddDebtViewModel @Inject constructor(
    private val debtRepo: DebtRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddDebtState())
    val state: StateFlow<AddDebtState> = _state.asStateFlow()

    // FIX 2: Use Channel for one-shot navigation event instead of boolean flag
    // This prevents the "saved" state from re-triggering on recomposition
    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun setDebtType(type: DebtType) = _state.update { it.copy(debtType = type) }
    fun setName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun setNote(v: String) = _state.update { it.copy(note = v) }

    fun setInitialType(type: String) {
        val t = try { DebtType.valueOf(type) } catch (e: Exception) { DebtType.LENDING }
        _state.update { it.copy(debtType = t) }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Name is required") }
            return
        }
        viewModelScope.launch {
            debtRepo.addPerson(DebtPersonEntity(
                name = s.name.trim(),
                type = s.debtType,
                dueDate = s.dueDate,
                note = s.note
            ))
            // FIX 2: Send one-shot navigation event via Channel
            _navigationEvent.send(Unit)
        }
    }
}
