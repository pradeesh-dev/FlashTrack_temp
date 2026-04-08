package com.flashtrack.app.data.local.database

import androidx.room.*
import com.flashtrack.app.data.local.dao.*
import com.flashtrack.app.data.local.entity.*

// ─── Type Converters ──────────────────────────────────────────────────────────

class Converters {
    @TypeConverter fun fromTransactionType(v: TransactionType) = v.name
    @TypeConverter fun toTransactionType(v: String) = TransactionType.valueOf(v)

    @TypeConverter fun fromAccountType(v: AccountType) = v.name
    @TypeConverter fun toAccountType(v: String) = AccountType.valueOf(v)

    @TypeConverter fun fromPaymentModeType(v: PaymentModeType) = v.name
    @TypeConverter fun toPaymentModeType(v: String) = PaymentModeType.valueOf(v)

    @TypeConverter fun fromBudgetPeriod(v: BudgetPeriod) = v.name
    @TypeConverter fun toBudgetPeriod(v: String) = BudgetPeriod.valueOf(v)

    @TypeConverter fun fromDebtType(v: DebtType) = v.name
    @TypeConverter fun toDebtType(v: String) = DebtType.valueOf(v)

    @TypeConverter fun fromDebtRecordType(v: DebtRecordType) = v.name
    @TypeConverter fun toDebtRecordType(v: String) = DebtRecordType.valueOf(v)

    @TypeConverter fun fromScheduleFrequency(v: ScheduleFrequency) = v.name
    @TypeConverter fun toScheduleFrequency(v: String) = ScheduleFrequency.valueOf(v)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        PaymentModeEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        BudgetEntity::class,
        DebtPersonEntity::class,
        DebtRecordEntity::class,
        ScheduledTransactionEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun paymentModeDao(): PaymentModeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtPersonDao(): DebtPersonDao
    abstract fun debtRecordDao(): DebtRecordDao
    abstract fun scheduledTransactionDao(): ScheduledTransactionDao
}

// ─── Seed Data ────────────────────────────────────────────────────────────────

object SeedData {

    val defaultCategories = listOf(
        // Expense
        CategoryEntity(name = "Food and Dining",   type = TransactionType.EXPENSE, iconName = "restaurant",      colorHex = "#E53935", sortOrder = 0, isDefault = true),
        CategoryEntity(name = "Tea & Snacks",       type = TransactionType.EXPENSE, iconName = "local_cafe",      colorHex = "#795548", sortOrder = 1, isDefault = true),
        CategoryEntity(name = "Transport",           type = TransactionType.EXPENSE, iconName = "directions_bus",  colorHex = "#1E88E5", sortOrder = 2, isDefault = true),
        CategoryEntity(name = "Bills and Utilities", type = TransactionType.EXPENSE, iconName = "receipt_long",   colorHex = "#039BE5", sortOrder = 3, isDefault = true),
        CategoryEntity(name = "Shopping",            type = TransactionType.EXPENSE, iconName = "shopping_bag",   colorHex = "#F4511E", sortOrder = 4, isDefault = true),
        CategoryEntity(name = "Entertainment",       type = TransactionType.EXPENSE, iconName = "movie",          colorHex = "#8E24AA", sortOrder = 5, isDefault = true),
        CategoryEntity(name = "Health & Medical",    type = TransactionType.EXPENSE, iconName = "local_hospital", colorHex = "#E91E63", sortOrder = 6, isDefault = true),
        CategoryEntity(name = "Travel",              type = TransactionType.EXPENSE, iconName = "flight",         colorHex = "#00ACC1", sortOrder = 7, isDefault = true),
        CategoryEntity(name = "Education",           type = TransactionType.EXPENSE, iconName = "school",         colorHex = "#FB8C00", sortOrder = 8, isDefault = true),
        CategoryEntity(name = "Groceries",           type = TransactionType.EXPENSE, iconName = "local_grocery_store", colorHex = "#43A047", sortOrder = 9, isDefault = true),
        CategoryEntity(name = "Personal Care",       type = TransactionType.EXPENSE, iconName = "face",           colorHex = "#D81B60", sortOrder = 10, isDefault = true),
        CategoryEntity(name = "Others",              type = TransactionType.EXPENSE, iconName = "more_horiz",     colorHex = "#757575", sortOrder = 99, isDefault = true),
        // Income
        CategoryEntity(name = "Salary",     type = TransactionType.INCOME, iconName = "account_balance_wallet", colorHex = "#43A047", sortOrder = 0, isDefault = true),
        CategoryEntity(name = "Freelance",  type = TransactionType.INCOME, iconName = "work",                   colorHex = "#00897B", sortOrder = 1, isDefault = true),
        CategoryEntity(name = "Investment", type = TransactionType.INCOME, iconName = "trending_up",            colorHex = "#FB8C00", sortOrder = 2, isDefault = true),
        CategoryEntity(name = "Business",   type = TransactionType.INCOME, iconName = "store",                  colorHex = "#1E88E5", sortOrder = 3, isDefault = true),
        CategoryEntity(name = "Gift",       type = TransactionType.INCOME, iconName = "card_giftcard",          colorHex = "#E91E63", sortOrder = 4, isDefault = true),
        CategoryEntity(name = "Interest",   type = TransactionType.INCOME, iconName = "savings",                colorHex = "#8E24AA", sortOrder = 5, isDefault = true),
        CategoryEntity(name = "Other Income",type = TransactionType.INCOME,iconName = "more_horiz",             colorHex = "#757575", sortOrder = 99, isDefault = true),
    )

    val defaultAccount = AccountEntity(
        name = "Cash",
        type = AccountType.CASH,
        balance = 0.0,
        colorHex = "#43A047",
        iconName = "payments",
        sortOrder = 99
    )

    val defaultPaymentMode = PaymentModeEntity(
        name = "Cash",
        type = PaymentModeType.CASH,
        iconName = "payments",
        isDefault = true,
        sortOrder = 99
    )

    val defaultTags = listOf(
        TagEntity(name = "bus"),
        TagEntity(name = "tea"),
        TagEntity(name = "breakfast"),
        TagEntity(name = "lunch"),
        TagEntity(name = "dinner"),
        TagEntity(name = "work"),
    )
}
