package com.flashtrack.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── Enums ───────────────────────────────────────────────────────────────────

enum class TransactionType { EXPENSE, INCOME, TRANSFER }
enum class AccountType { BANK, CREDIT_CARD, WALLET, CASH }
enum class PaymentModeType { UPI, DEBIT_CARD, CREDIT_CARD, WALLET, CASH, NET_BANKING, OTHER }
enum class BudgetPeriod { MONTHLY, YEARLY }
enum class DebtType { LENDING, BORROWING }
enum class DebtRecordType { PAID, RECEIVED, ADJUSTMENT }
enum class ScheduleFrequency { DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY }

// ─── Transaction ─────────────────────────────────────────────────────────────

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val paymentModeId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val tagIds: String = "",
    val attachmentPath: String? = null,
    val isScheduled: Boolean = false,
    val scheduledTxnId: Long? = null,
    val isCreditCardPayment: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Account ─────────────────────────────────────────────────────────────────

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0,
    val totalCreditLimit: Double? = null,
    val availableCreditLimit: Double? = null,
    val billingCycleStartDate: Int = 1,
    val paymentDueDate: Int = 15,
    val colorHex: String = "#4CAF50",
    val iconName: String = "account_balance",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Payment Mode ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "payment_modes",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("linkedAccountId")]
)
data class PaymentModeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PaymentModeType,
    val linkedAccountId: Long? = null,
    val iconName: String = "payment",
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

// ─── Category ─────────────────────────────────────────────────────────────────

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isCustom: Boolean = false
)

// ─── Tag ─────────────────────────────────────────────────────────────────────

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Budget ───────────────────────────────────────────────────────────────────

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val period: BudgetPeriod,
    val year: Int,
    val month: Int? = null,
    val categoryId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Debt Person ──────────────────────────────────────────────────────────────

@Entity(tableName = "debt_persons")
data class DebtPersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: DebtType,
    val initialAmount: Double = 0.0,
    val dueDate: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Debt Record ──────────────────────────────────────────────────────────────

@Entity(
    tableName = "debt_records",
    foreignKeys = [
        ForeignKey(
            entity = DebtPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class DebtRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val recordType: DebtRecordType,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)

// ─── Scheduled Transaction ────────────────────────────────────────────────────

@Entity(tableName = "scheduled_transactions")
data class ScheduledTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val paymentModeId: Long,
    val accountId: Long,
    val note: String = "",
    val frequency: ScheduleFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val nextDueDate: Long,
    val lastExecutedDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
