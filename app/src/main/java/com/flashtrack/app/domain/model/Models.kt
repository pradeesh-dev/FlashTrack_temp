package com.flashtrack.app.domain.model

import com.flashtrack.app.data.local.entity.*

// Rich domain models used across the presentation layer

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMode: PaymentMode,
    val account: Account,
    val toAccount: Account? = null,
    val note: String = "",
    val date: Long,
    val tags: List<Tag> = emptyList(),
    val attachmentPath: String? = null,
    val isScheduled: Boolean = false,
    val isCreditCardPayment: Boolean = false
)

data class Account(
    val id: Long = 0,
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
    val linkedPaymentModes: List<PaymentMode> = emptyList()
)

data class PaymentMode(
    val id: Long = 0,
    val name: String,
    val type: PaymentModeType,
    val linkedAccountId: Long? = null,
    val linkedAccount: Account? = null,
    val iconName: String = "payment",
    val isDefault: Boolean = false
)

data class Category(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val transactionCount: Int = 0
)

data class Budget(
    val id: Long = 0,
    val amount: Double,
    val period: BudgetPeriod,
    val year: Int,
    val month: Int? = null,
    val spent: Double = 0.0
) {
    val remaining: Double get() = (amount - spent).coerceAtLeast(0.0)
    val progress: Float get() = if (amount > 0) (spent / amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spent > amount
}

data class DebtPerson(
    val id: Long = 0,
    val name: String,
    val type: DebtType,
    val initialAmount: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalReceived: Double = 0.0,
    val dueDate: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false,
    val recordCount: Int = 0
) {
    val netAmount: Double get() = when (type) {
        DebtType.LENDING -> initialAmount - totalReceived
        DebtType.BORROWING -> initialAmount - totalPaid
    }

    val displayAmount: Double get() = netAmount.coerceAtLeast(0.0)
}

data class DebtRecord(
    val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val recordType: DebtRecordType,
    val note: String = "",
    val date: Long
)

data class ScheduledTransaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMode: PaymentMode,
    val account: Account,
    val note: String = "",
    val frequency: ScheduleFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val nextDueDate: Long,
    val lastExecutedDate: Long? = null,
    val isActive: Boolean = true
)

// Analysis models
data class CategorySpending(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val previousAmount: Double = 0.0
) {
    val percentChange: Double get() = if (previousAmount > 0)
        ((amount - previousAmount) / previousAmount) * 100 else 0.0
}

data class DailyAmount(
    val dayOfMonth: Int,
    val date: Long,
    val amount: Double
)

data class PaymentModeSpending(
    val paymentMode: PaymentMode,
    val amount: Double
)

data class AnalysisSummary(
    val totalExpense: Double,
    val totalIncome: Double,
    val transactionCount: Int,
    val budget: Budget?,
    val categoryBreakdown: List<CategorySpending>,
    val dailyExpenses: List<DailyAmount>,
    val paymentModeBreakdown: List<PaymentModeSpending>,
    val predictedExpense: Double = 0.0,
    val avgDailyExpense: Double = 0.0,
    val avgDailyIncome: Double = 0.0,
    val avgTxnExpense: Double = 0.0,
    val avgTxnIncome: Double = 0.0
) {
    val netBalance: Double get() = totalIncome - totalExpense
}
