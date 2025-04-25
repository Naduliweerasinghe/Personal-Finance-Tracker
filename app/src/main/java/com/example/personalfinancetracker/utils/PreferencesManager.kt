package com.example.personalfinancetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.personalfinancetracker.models.Budget
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.models.UpcomingPayment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Calendar
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "personal_finance_preferences",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_UPCOMING_PAYMENTS = "upcoming_payments"
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit {
            putString(KEY_USER_NAME, name)
        }
    }

    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit {
            putString(KEY_USER_EMAIL, email)
        }
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_USER_LOGGED_IN, loggedIn)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit {
            putString(KEY_TRANSACTIONS, json)
        }
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun setCurrency(currency: String) {
        sharedPreferences.edit {
            putString(KEY_CURRENCY, currency)
        }
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun setBudget(budget: Budget) {
        val json = gson.toJson(budget)
        sharedPreferences.edit {
            putString(KEY_BUDGET, json)
        }
    }

    fun getBudget(): Budget? {
        val json = sharedPreferences.getString(KEY_BUDGET, null) ?: return null
        return gson.fromJson(json, Budget::class.java)
    }

    fun getCurrentMonthBudget(): Budget? {
        val budget = getBudget() ?: return null
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return if (budget.month == currentMonth && budget.year == currentYear) {
            budget
        } else {
            null
        }
    }

    fun isFirstLaunch(): Boolean {
        val isFirst = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirst) {
            sharedPreferences.edit {
                putBoolean(KEY_FIRST_LAUNCH, false)
            }
        }
        return isFirst
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }
    }

    fun getNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun isNotificationEnabled(): Boolean {
        return getNotificationsEnabled()
    }

    fun saveUpcomingPayments(payments: List<UpcomingPayment>) {
        val json = gson.toJson(payments)
        sharedPreferences.edit {
            putString(KEY_UPCOMING_PAYMENTS, json)
        }
    }

    fun getUpcomingPayments(): List<UpcomingPayment> {
        val json = sharedPreferences.getString(KEY_UPCOMING_PAYMENTS, null)
        return if (json != null) {
            val type = object : TypeToken<List<UpcomingPayment>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun logout() {
        setUserLoggedIn(false)
        sharedPreferences.edit {
            clear()
        }
    }
}