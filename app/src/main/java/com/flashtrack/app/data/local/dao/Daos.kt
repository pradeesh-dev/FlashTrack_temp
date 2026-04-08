package com.flashtrack.app.data.local.dao

import androidx.room.*
import com.flashtrack.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ─── Transaction DAO ──────────────────────────────────────────────────────────

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date >= :startMs AND date <= :endMs ORDER BY date DESC")
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY date DESC")
    fun getByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE paymentModeId = :paymentModeId ORDER BY date DESC")
    fun getByPaymentMode(paymentModeId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date >= :startMs AND date <= :endMs")
    fun getTotalExpense(startMs: Long, endMs: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date >= :startMs AND date <= :endMs")
    fun getTotalIncome(startMs: Long, endMs: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' AND date >= :startMs AND date <= :endMs ORDER BY date DESC")
    fun getExpenses(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'INCOME' AND date >= :startMs AND date <= :endMs ORDER BY date DESC")
    fun getIncomes(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE date >= :startMs AND date <= :endMs")
    fun getCountByDateRange(startMs: Long, endMs: Long): Flow<Int>

    @Query("SELECT * FROM transactions WHERE isCreditCardPayment = 1 AND accountId = :accountId ORDER BY date DESC")
    fun getCreditCardPayments(accountId: Long): Flow<List<TransactionEntity>>
}

// ─── Account DAO ──────────────────────────────────────────────────────────────

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AccountEntity): Long

    @Update
    suspend fun update(entity: AccountEntity)

    @Delete
    suspend fun delete(entity: AccountEntity)

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE type = :type AND isActive = 1 ORDER BY sortOrder ASC")
    fun getByType(type: AccountType): Flow<List<AccountEntity>>

    @Query("SELECT SUM(balance) FROM accounts WHERE type IN ('BANK','WALLET','CASH') AND isActive = 1")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT SUM(availableCreditLimit) FROM accounts WHERE type = 'CREDIT_CARD' AND isActive = 1")
    fun getTotalAvailableCredit(): Flow<Double?>

    @Query("UPDATE accounts SET balance = :balance WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double)

    @Query("UPDATE accounts SET availableCreditLimit = :limit WHERE id = :id")
    suspend fun updateAvailableCredit(id: Long, limit: Double)
}

// ─── Payment Mode DAO ─────────────────────────────────────────────────────────

@Dao
interface PaymentModeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentModeEntity): Long

    @Update
    suspend fun update(entity: PaymentModeEntity)

    @Delete
    suspend fun delete(entity: PaymentModeEntity)

    @Query("SELECT * FROM payment_modes WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<PaymentModeEntity>>

    @Query("SELECT * FROM payment_modes WHERE id = :id")
    suspend fun getById(id: Long): PaymentModeEntity?

    @Query("SELECT * FROM payment_modes WHERE linkedAccountId = :accountId AND isActive = 1")
    fun getByAccount(accountId: Long): Flow<List<PaymentModeEntity>>

    @Query("SELECT * FROM payment_modes WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): PaymentModeEntity?

    @Query("SELECT * FROM payment_modes WHERE type = 'CREDIT_CARD' AND isActive = 1")
    fun getCreditCardModes(): Flow<List<PaymentModeEntity>>
}

// ─── Category DAO ─────────────────────────────────────────────────────────────

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CategoryEntity): Long

    @Update
    suspend fun update(entity: CategoryEntity)

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder ASC, id ASC")
    fun getByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = 'Others' AND type = 'EXPENSE' LIMIT 1")
    suspend fun getDefaultExpenseCategory(): CategoryEntity?
}

// ─── Tag DAO ──────────────────────────────────────────────────────────────────

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TagEntity): Long

    @Update
    suspend fun update(entity: TagEntity)

    @Delete
    suspend fun delete(entity: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<TagEntity>>
}

// ─── Budget DAO ───────────────────────────────────────────────────────────────

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BudgetEntity): Long

    @Update
    suspend fun update(entity: BudgetEntity)

    @Delete
    suspend fun delete(entity: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE period = 'MONTHLY' AND year = :year AND month = :month AND categoryId IS NULL LIMIT 1")
    fun getMonthlyBudget(year: Int, month: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE period = 'YEARLY' AND year = :year AND categoryId IS NULL LIMIT 1")
    fun getYearlyBudget(year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE period = 'MONTHLY' ORDER BY year DESC, month DESC")
    fun getAllMonthly(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?
}

// ─── Debt Person DAO ──────────────────────────────────────────────────────────

@Dao
interface DebtPersonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtPersonEntity): Long

    @Update
    suspend fun update(entity: DebtPersonEntity)

    @Delete
    suspend fun delete(entity: DebtPersonEntity)

    @Query("SELECT * FROM debt_persons WHERE isSettled = 0 ORDER BY name ASC")
    fun getUnsettled(): Flow<List<DebtPersonEntity>>

    @Query("SELECT * FROM debt_persons ORDER BY name ASC")
    fun getAll(): Flow<List<DebtPersonEntity>>

    @Query("SELECT * FROM debt_persons WHERE type = :type AND isSettled = 0 ORDER BY name ASC")
    fun getByType(type: DebtType): Flow<List<DebtPersonEntity>>

    @Query("SELECT * FROM debt_persons WHERE id = :id")
    suspend fun getById(id: Long): DebtPersonEntity?
}

// ─── Debt Record DAO ──────────────────────────────────────────────────────────

@Dao
interface DebtRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtRecordEntity): Long

    @Update
    suspend fun update(entity: DebtRecordEntity)

    @Delete
    suspend fun delete(entity: DebtRecordEntity)

    @Query("SELECT * FROM debt_records WHERE personId = :personId ORDER BY date DESC")
    fun getByPerson(personId: Long): Flow<List<DebtRecordEntity>>

    @Query("SELECT SUM(amount) FROM debt_records WHERE personId = :personId AND recordType = 'PAID'")
    fun getTotalPaid(personId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM debt_records WHERE personId = :personId AND recordType = 'RECEIVED'")
    fun getTotalReceived(personId: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM debt_records WHERE personId = :personId")
    fun getRecordCount(personId: Long): Flow<Int>
}

// ─── Scheduled Transaction DAO ────────────────────────────────────────────────

@Dao
interface ScheduledTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduledTransactionEntity): Long

    @Update
    suspend fun update(entity: ScheduledTransactionEntity)

    @Delete
    suspend fun delete(entity: ScheduledTransactionEntity)

    @Query("SELECT * FROM scheduled_transactions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActive(): Flow<List<ScheduledTransactionEntity>>

    @Query("SELECT * FROM scheduled_transactions ORDER BY nextDueDate ASC")
    fun getAll(): Flow<List<ScheduledTransactionEntity>>

    @Query("SELECT * FROM scheduled_transactions WHERE id = :id")
    suspend fun getById(id: Long): ScheduledTransactionEntity?

    @Query("SELECT * FROM scheduled_transactions WHERE nextDueDate <= :nowMs AND isActive = 1")
    suspend fun getDue(nowMs: Long): List<ScheduledTransactionEntity>
}
