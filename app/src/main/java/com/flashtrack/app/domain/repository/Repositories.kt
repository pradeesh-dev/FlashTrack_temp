package com.flashtrack.app.domain.repository

import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getRecent(limit: Int = 5): Flow<List<Transaction>>
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>>
    fun getByAccount(accountId: Long): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun addTransaction(entity: TransactionEntity): Long
    suspend fun updateTransaction(entity: TransactionEntity)
    suspend fun deleteTransaction(entity: TransactionEntity)
    fun getTotalExpense(startMs: Long, endMs: Long): Flow<Double>
    fun getTotalIncome(startMs: Long, endMs: Long): Flow<Double>
    fun getCountByDateRange(startMs: Long, endMs: Long): Flow<Int>
}

interface AccountRepository {
    fun getAll(): Flow<List<Account>>
    fun getByType(type: AccountType): Flow<List<Account>>
    suspend fun getById(id: Long): Account?
    suspend fun addAccount(entity: AccountEntity): Long
    suspend fun updateAccount(entity: AccountEntity)
    suspend fun deleteAccount(entity: AccountEntity)
    fun getTotalBalance(): Flow<Double>
    fun getTotalAvailableCredit(): Flow<Double>
    suspend fun updateBalance(id: Long, balance: Double)
}

interface PaymentModeRepository {
    fun getAll(): Flow<List<PaymentMode>>
    fun getByAccount(accountId: Long): Flow<List<PaymentMode>>
    suspend fun getById(id: Long): PaymentMode?
    suspend fun getDefault(): PaymentMode?
    suspend fun addPaymentMode(entity: PaymentModeEntity): Long
    suspend fun updatePaymentMode(entity: PaymentModeEntity)
    suspend fun deletePaymentMode(entity: PaymentModeEntity)
}

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getByType(type: TransactionType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun addCategory(entity: CategoryEntity): Long
    suspend fun updateCategory(entity: CategoryEntity)
    suspend fun deleteCategory(entity: CategoryEntity)
}

interface TagRepository {
    fun getAll(): Flow<List<Tag>>
    fun search(query: String): Flow<List<Tag>>
    suspend fun addTag(entity: TagEntity): Long
    suspend fun deleteTag(entity: TagEntity)
}

interface BudgetRepository {
    fun getAll(): Flow<List<Budget>>
    fun getAllMonthly(): Flow<List<Budget>>
    fun getMonthlyBudget(year: Int, month: Int): Flow<Budget?>
    fun getYearlyBudget(year: Int): Flow<Budget?>
    suspend fun addBudget(entity: BudgetEntity): Long
    suspend fun updateBudget(entity: BudgetEntity)
    suspend fun deleteBudget(entity: BudgetEntity)
}

interface DebtRepository {
    fun getAllPersons(): Flow<List<DebtPerson>>
    fun getUnsettledPersons(): Flow<List<DebtPerson>>
    fun getPersonsByType(type: DebtType): Flow<List<DebtPerson>>
    suspend fun getPersonById(id: Long): DebtPerson?
    suspend fun addPerson(entity: DebtPersonEntity): Long
    suspend fun updatePerson(entity: DebtPersonEntity)
    suspend fun deletePerson(entity: DebtPersonEntity)
    fun getRecordsByPerson(personId: Long): Flow<List<DebtRecord>>
    suspend fun addRecord(entity: DebtRecordEntity): Long
    suspend fun deleteRecord(entity: DebtRecordEntity)
}

interface ScheduledTransactionRepository {
    fun getActive(): Flow<List<ScheduledTransaction>>
    fun getAll(): Flow<List<ScheduledTransaction>>
    suspend fun add(entity: ScheduledTransactionEntity): Long
    suspend fun update(entity: ScheduledTransactionEntity)
    suspend fun delete(entity: ScheduledTransactionEntity)
    suspend fun getDue(nowMs: Long): List<ScheduledTransaction>
}
