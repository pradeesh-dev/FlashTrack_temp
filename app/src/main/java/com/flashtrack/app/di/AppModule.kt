package com.flashtrack.app.di

import android.content.Context
import androidx.room.Room
import com.flashtrack.app.data.local.dao.*
import com.flashtrack.app.data.local.database.AppDatabase
import com.flashtrack.app.data.local.database.SeedData
import com.flashtrack.app.data.repository.*
import com.flashtrack.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "flashtrack_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideAccountDao(db: AppDatabase) = db.accountDao()
    @Provides fun providePaymentModeDao(db: AppDatabase) = db.paymentModeDao()
    @Provides fun provideCategoryDao(db: AppDatabase) = db.categoryDao()
    @Provides fun provideTagDao(db: AppDatabase) = db.tagDao()
    @Provides fun provideBudgetDao(db: AppDatabase) = db.budgetDao()
    @Provides fun provideDebtPersonDao(db: AppDatabase) = db.debtPersonDao()
    @Provides fun provideDebtRecordDao(db: AppDatabase) = db.debtRecordDao()
    @Provides fun provideScheduledDao(db: AppDatabase) = db.scheduledTransactionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindTransactionRepo(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindAccountRepo(impl: AccountRepositoryImpl): AccountRepository

    @Binds @Singleton
    abstract fun bindPaymentModeRepo(impl: PaymentModeRepositoryImpl): PaymentModeRepository

    @Binds @Singleton
    abstract fun bindCategoryRepo(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindTagRepo(impl: TagRepositoryImpl): TagRepository

    @Binds @Singleton
    abstract fun bindBudgetRepo(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds @Singleton
    abstract fun bindDebtRepo(impl: DebtRepositoryImpl): DebtRepository

    @Binds @Singleton
    abstract fun bindScheduledRepo(impl: ScheduledTransactionRepositoryImpl): ScheduledTransactionRepository
}

// ─── Database Seeder ─────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object SeederModule {

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        db: AppDatabase,
        prefs: com.flashtrack.app.data.datastore.UserPreferencesRepository
    ): DatabaseSeeder = DatabaseSeeder(db, prefs)
}

class DatabaseSeeder(
    private val db: AppDatabase,
    private val prefs: com.flashtrack.app.data.datastore.UserPreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfNeeded() {
        scope.launch {
            // FIX 14: Use first() to read prefs ONCE — not collect() which leaks forever
            val userPrefs = prefs.preferences.first()
            if (!userPrefs.isDatabaseSeeded) {
                seed()
                prefs.markDatabaseSeeded()
            }
        }
    }

    private suspend fun seed() {
        // Categories
        SeedData.defaultCategories.forEach { db.categoryDao().insert(it) }
        // Default cash account
        val cashAccountId = db.accountDao().insert(SeedData.defaultAccount)
        // Default cash payment mode linked to cash account
        db.paymentModeDao().insert(
            SeedData.defaultPaymentMode.copy(linkedAccountId = cashAccountId)
        )
        // Tags
        SeedData.defaultTags.forEach { db.tagDao().insert(it) }
    }
}
