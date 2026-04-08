package com.flashtrack.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flashtrack_prefs")

data class UserPreferences(
    val userName: String = "User",
    val userEmail: String = "",
    val userPhotoPath: String = "",
    val showBalance: Boolean = true,
    val defaultPaymentModeId: Long = -1L,
    val defaultCategoryId: Long = -1L,
    val isOnboardingDone: Boolean = false,
    val lastBackupTime: Long = 0L,
    val currencySymbol: String = "₹",
    val dateFormat: String = "dd MMM yyyy",
    val isDatabaseSeeded: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO_PATH = stringPreferencesKey("user_photo_path")
        val SHOW_BALANCE = booleanPreferencesKey("show_balance")
        val DEFAULT_PAYMENT_MODE_ID = longPreferencesKey("default_payment_mode_id")
        val DEFAULT_CATEGORY_ID = longPreferencesKey("default_category_id")
        val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val IS_DATABASE_SEEDED = booleanPreferencesKey("is_database_seeded")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserPreferences(
                userName = prefs[Keys.USER_NAME] ?: "User",
                userEmail = prefs[Keys.USER_EMAIL] ?: "",
                userPhotoPath = prefs[Keys.USER_PHOTO_PATH] ?: "",
                showBalance = prefs[Keys.SHOW_BALANCE] ?: true,
                defaultPaymentModeId = prefs[Keys.DEFAULT_PAYMENT_MODE_ID] ?: -1L,
                defaultCategoryId = prefs[Keys.DEFAULT_CATEGORY_ID] ?: -1L,
                isOnboardingDone = prefs[Keys.IS_ONBOARDING_DONE] ?: false,
                lastBackupTime = prefs[Keys.LAST_BACKUP_TIME] ?: 0L,
                currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "₹",
                dateFormat = prefs[Keys.DATE_FORMAT] ?: "dd MMM yyyy",
                isDatabaseSeeded = prefs[Keys.IS_DATABASE_SEEDED] ?: false
            )
        }

    suspend fun updateUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun updateUserEmail(email: String) {
        dataStore.edit { it[Keys.USER_EMAIL] = email }
    }

    suspend fun updateUserPhotoPath(path: String) {
        dataStore.edit { it[Keys.USER_PHOTO_PATH] = path }
    }

    suspend fun updateShowBalance(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_BALANCE] = show }
    }

    suspend fun updateDefaultPaymentMode(id: Long) {
        dataStore.edit { it[Keys.DEFAULT_PAYMENT_MODE_ID] = id }
    }

    suspend fun markDatabaseSeeded() {
        dataStore.edit { it[Keys.IS_DATABASE_SEEDED] = true }
    }

    suspend fun markOnboardingDone() {
        dataStore.edit { it[Keys.IS_ONBOARDING_DONE] = true }
    }

    suspend fun updateLastBackupTime(time: Long) {
        dataStore.edit { it[Keys.LAST_BACKUP_TIME] = time }
    }
}
