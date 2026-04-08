package com.flashtrack.app.data.repository

import com.flashtrack.app.data.local.dao.*
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

// ─── Mappers ──────────────────────────────────────────────────────────────────

fun CategoryEntity.toDomain() = Category(
    id = id, name = name, type = type,
    iconName = iconName, colorHex = colorHex,
    sortOrder = sortOrder, isDefault = isDefault
)

fun AccountEntity.toDomain(paymentModes: List<PaymentMode> = emptyList()) = Account(
    id = id, name = name, type = type, balance = balance,
    totalCreditLimit = totalCreditLimit,
    availableCreditLimit = availableCreditLimit,
    billingCycleStartDate = billingCycleStartDate,
    paymentDueDate = paymentDueDate,
    colorHex = colorHex, iconName = iconName, isActive = isActive,
    linkedPaymentModes = paymentModes
)

fun PaymentModeEntity.toDomain(account: Account? = null) = PaymentMode(
    id = id, name = name, type = type,
    linkedAccountId = linkedAccountId,
    linkedAccount = account,
    iconName = iconName, isDefault = isDefault
)

fun TagEntity.toDomain(count: Int = 0) = Tag(id = id, name = name, transactionCount = count)

fun DebtRecordEntity.toDomain() = DebtRecord(
    id = id, personId = personId, amount = amount,
    recordType = recordType, note = note, date = date
)


// ─── Transaction Repository ───────────────────────────────────────────────────

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val txnDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val paymentModeDao: PaymentModeDao,
    private val accountDao: AccountDao,
    private val tagDao: TagDao
) : TransactionRepository {

    private suspend fun enrich(entity: TransactionEntity): Transaction {
        val cat = categoryDao.getById(entity.categoryId)?.toDomain()
            ?: Category(name = "Others", type = TransactionType.EXPENSE, iconName = "more_horiz", colorHex = "#757575")
        val pm = paymentModeDao.getById(entity.paymentModeId)?.toDomain()
            ?: PaymentMode(name = "Cash", type = PaymentModeType.CASH, iconName = "payments")
        val acc = accountDao.getById(entity.accountId)?.toDomain()
            ?: Account(name = "Cash", type = AccountType.CASH)
        val toAcc = entity.toAccountId?.let { accountDao.getById(it)?.toDomain() }
        val tags = entity.tagIds.split(",").filter { it.isNotBlank() }
            .mapNotNull { id -> tagDao.getById(id.toLongOrNull() ?: 0)?.toDomain() }
        return Transaction(
            id = entity.id, amount = entity.amount, type = entity.type,
            category = cat, paymentMode = pm, account = acc, toAccount = toAcc,
            note = entity.note, date = entity.date, tags = tags,
            attachmentPath = entity.attachmentPath,
            isScheduled = entity.isScheduled,
            isCreditCardPayment = entity.isCreditCardPayment
        )
    }

    override fun getAll(): Flow<List<Transaction>> =
        txnDao.getAll().map { list -> list.map { enrich(it) } }

    override fun getRecent(limit: Int): Flow<List<Transaction>> =
        txnDao.getRecent(limit).map { list -> list.map { enrich(it) } }

    override fun getByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>> =
        txnDao.getByDateRange(startMs, endMs).map { list -> list.map { enrich(it) } }

    override fun getByAccount(accountId: Long): Flow<List<Transaction>> =
        txnDao.getByAccount(accountId).map { list -> list.map { enrich(it) } }

    override suspend fun getById(id: Long): Transaction? =
        txnDao.getById(id)?.let { enrich(it) }

    override suspend fun addTransaction(entity: TransactionEntity): Long =
        txnDao.insert(entity)

    override suspend fun updateTransaction(entity: TransactionEntity) =
        txnDao.update(entity)

    override suspend fun deleteTransaction(entity: TransactionEntity) =
        txnDao.delete(entity)

    override fun getTotalExpense(startMs: Long, endMs: Long): Flow<Double> =
        txnDao.getTotalExpense(startMs, endMs).map { it ?: 0.0 }

    override fun getTotalIncome(startMs: Long, endMs: Long): Flow<Double> =
        txnDao.getTotalIncome(startMs, endMs).map { it ?: 0.0 }

    override fun getCountByDateRange(startMs: Long, endMs: Long): Flow<Int> =
        txnDao.getCountByDateRange(startMs, endMs)
}

// ─── Account Repository ───────────────────────────────────────────────────────

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val paymentModeDao: PaymentModeDao
) : AccountRepository {

    override fun getAll(): Flow<List<Account>> =
        accountDao.getAll().map { accounts ->
            accounts.map { acc ->
                val pms = paymentModeDao.getByAccount(acc.id).first().map { it.toDomain(acc.toDomain()) }
                acc.toDomain(pms)
            }
        }

    override fun getByType(type: AccountType): Flow<List<Account>> =
        accountDao.getByType(type).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Account? = accountDao.getById(id)?.toDomain()

    override suspend fun addAccount(entity: AccountEntity): Long = accountDao.insert(entity)

    override suspend fun updateAccount(entity: AccountEntity) = accountDao.update(entity)

    override suspend fun deleteAccount(entity: AccountEntity) = accountDao.delete(entity)

    override fun getTotalBalance(): Flow<Double> = accountDao.getTotalBalance().map { it ?: 0.0 }

    override fun getTotalAvailableCredit(): Flow<Double> =
        accountDao.getTotalAvailableCredit().map { it ?: 0.0 }

    override suspend fun updateBalance(id: Long, balance: Double) =
        accountDao.updateBalance(id, balance)
}

// ─── Payment Mode Repository ──────────────────────────────────────────────────

@Singleton
class PaymentModeRepositoryImpl @Inject constructor(
    private val paymentModeDao: PaymentModeDao,
    private val accountDao: AccountDao
) : PaymentModeRepository {

    override fun getAll(): Flow<List<PaymentMode>> =
        paymentModeDao.getAll().map { list ->
            list.map { pm ->
                val acc = pm.linkedAccountId?.let { accountDao.getById(it)?.toDomain() }
                pm.toDomain(acc)
            }
        }

    override fun getByAccount(accountId: Long): Flow<List<PaymentMode>> =
        paymentModeDao.getByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): PaymentMode? = paymentModeDao.getById(id)?.toDomain()

    override suspend fun getDefault(): PaymentMode? = paymentModeDao.getDefault()?.toDomain()

    override suspend fun addPaymentMode(entity: PaymentModeEntity): Long =
        paymentModeDao.insert(entity)

    override suspend fun updatePaymentMode(entity: PaymentModeEntity) =
        paymentModeDao.update(entity)

    override suspend fun deletePaymentMode(entity: PaymentModeEntity) =
        paymentModeDao.delete(entity)
}

// ─── Category Repository ──────────────────────────────────────────────────────

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        categoryDao.getAll().map { it.map { e -> e.toDomain() } }

    override fun getByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getByType(type).map { it.map { e -> e.toDomain() } }

    override suspend fun getById(id: Long): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun addCategory(entity: CategoryEntity): Long = categoryDao.insert(entity)

    override suspend fun updateCategory(entity: CategoryEntity) = categoryDao.update(entity)

    override suspend fun deleteCategory(entity: CategoryEntity) = categoryDao.delete(entity)
}

// ─── Tag Repository ───────────────────────────────────────────────────────────

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAll(): Flow<List<Tag>> =
        tagDao.getAll().map { it.map { e -> e.toDomain() } }

    override fun search(query: String): Flow<List<Tag>> =
        tagDao.search(query).map { it.map { e -> e.toDomain() } }

    override suspend fun addTag(entity: TagEntity): Long = tagDao.insert(entity)

    override suspend fun deleteTag(entity: TagEntity) = tagDao.delete(entity)
}

// ─── Budget Repository ────────────────────────────────────────────────────────

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val txnDao: TransactionDao
) : BudgetRepository {

    private fun BudgetEntity.toDomain(spent: Double = 0.0) = Budget(
        id = id, amount = amount, period = period,
        year = year, month = month, spent = spent
    )

    private suspend fun getSpent(budget: BudgetEntity): Double {
        val (startMs, endMs) = budget.dateRange()
        return txnDao.getTotalExpense(startMs, endMs).first() ?: 0.0
    }

    override fun getAll(): Flow<List<Budget>> = budgetDao.getAll().map { list ->
        list.map { b -> b.toDomain(getSpent(b)) }
    }

    override fun getAllMonthly(): Flow<List<Budget>> = budgetDao.getAllMonthly().map { list ->
        list.map { b -> b.toDomain(getSpent(b)) }
    }

    override fun getMonthlyBudget(year: Int, month: Int): Flow<Budget?> =
        budgetDao.getMonthlyBudget(year, month).map { it?.let { b -> b.toDomain(getSpent(b)) } }

    override fun getYearlyBudget(year: Int): Flow<Budget?> =
        budgetDao.getYearlyBudget(year).map { it?.let { b -> b.toDomain(getSpent(b)) } }

    override suspend fun addBudget(entity: BudgetEntity): Long = budgetDao.insert(entity)

    override suspend fun updateBudget(entity: BudgetEntity) = budgetDao.update(entity)

    override suspend fun deleteBudget(entity: BudgetEntity) = budgetDao.delete(entity)
}

private fun BudgetEntity.dateRange(): Pair<Long, Long> {
    val cal = java.util.Calendar.getInstance()
    return if (period == BudgetPeriod.MONTHLY && month != null) {
        cal.set(year, month - 1, 1, 0, 0, 0); val start = cal.timeInMillis
        cal.set(year, month - 1, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59)
        Pair(start, cal.timeInMillis)
    } else {
        cal.set(year, 0, 1, 0, 0, 0); val start = cal.timeInMillis
        cal.set(year, 11, 31, 23, 59, 59)
        Pair(start, cal.timeInMillis)
    }
}

// ─── Debt Repository ──────────────────────────────────────────────────────────

@Singleton
class DebtRepositoryImpl @Inject constructor(
    private val personDao: DebtPersonDao,
    private val recordDao: DebtRecordDao
) : DebtRepository {

    private suspend fun DebtPersonEntity.toDomain(): DebtPerson {
        val paid = recordDao.getTotalPaid(id).first() ?: 0.0
        val received = recordDao.getTotalReceived(id).first() ?: 0.0
        val count = recordDao.getRecordCount(id).first()
        return DebtPerson(
            id = id, name = name, type = type,
            initialAmount = initialAmount,
            totalPaid = paid, totalReceived = received,
            dueDate = dueDate, note = note,
            isSettled = isSettled, recordCount = count
        )
    }

    override fun getAllPersons(): Flow<List<DebtPerson>> =
        personDao.getAll().map { it.map { e -> e.toDomain() } }

    override fun getUnsettledPersons(): Flow<List<DebtPerson>> =
        personDao.getUnsettled().map { it.map { e -> e.toDomain() } }

    override fun getPersonsByType(type: DebtType): Flow<List<DebtPerson>> =
        personDao.getByType(type).map { it.map { e -> e.toDomain() } }

    override suspend fun getPersonById(id: Long): DebtPerson? =
        personDao.getById(id)?.toDomain()

    override suspend fun addPerson(entity: DebtPersonEntity): Long = personDao.insert(entity)

    override suspend fun updatePerson(entity: DebtPersonEntity) = personDao.update(entity)

    override suspend fun deletePerson(entity: DebtPersonEntity) = personDao.delete(entity)

    override fun getRecordsByPerson(personId: Long): Flow<List<DebtRecord>> =
        recordDao.getByPerson(personId).map { it.map { e -> e.toDomain() } }

    override suspend fun addRecord(entity: DebtRecordEntity): Long = recordDao.insert(entity)

    override suspend fun deleteRecord(entity: DebtRecordEntity) = recordDao.delete(entity)
}

// ─── Scheduled Repository ────────────────────────────────────────────────────

@Singleton
class ScheduledTransactionRepositoryImpl @Inject constructor(
    private val dao: ScheduledTransactionDao,
    private val categoryDao: CategoryDao,
    private val paymentModeDao: PaymentModeDao,
    private val accountDao: AccountDao
) : ScheduledTransactionRepository {

    private suspend fun enrich(e: ScheduledTransactionEntity): ScheduledTransaction {
        val cat = categoryDao.getById(e.categoryId)?.toDomain()
            ?: Category(name = "Others", type = TransactionType.EXPENSE, iconName = "more_horiz", colorHex = "#757575")
        val pm = paymentModeDao.getById(e.paymentModeId)?.toDomain()
            ?: PaymentMode(name = "Cash", type = PaymentModeType.CASH, iconName = "payments")
        val acc = accountDao.getById(e.accountId)?.toDomain()
            ?: Account(name = "Cash", type = AccountType.CASH)
        return ScheduledTransaction(
            id = e.id, amount = e.amount, type = e.type,
            category = cat, paymentMode = pm, account = acc,
            note = e.note, frequency = e.frequency,
            startDate = e.startDate, endDate = e.endDate,
            nextDueDate = e.nextDueDate,
            lastExecutedDate = e.lastExecutedDate,
            isActive = e.isActive
        )
    }

    override fun getActive(): Flow<List<ScheduledTransaction>> =
        dao.getActive().map { it.map { e -> enrich(e) } }

    override fun getAll(): Flow<List<ScheduledTransaction>> =
        dao.getAll().map { it.map { e -> enrich(e) } }

    override suspend fun add(entity: ScheduledTransactionEntity): Long = dao.insert(entity)

    override suspend fun update(entity: ScheduledTransactionEntity) = dao.update(entity)

    override suspend fun delete(entity: ScheduledTransactionEntity) = dao.delete(entity)

    override suspend fun getDue(nowMs: Long): List<ScheduledTransaction> =
        dao.getDue(nowMs).map { enrich(it) }
}
