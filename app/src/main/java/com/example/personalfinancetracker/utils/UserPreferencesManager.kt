package com.example.personalfinancetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.personalfinancetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    // User Authentication Methods
    fun saveUserCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun getUserPassword(): String? {
        return sharedPreferences.getString(KEY_PASSWORD, null)
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    // Transaction Methods
    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Settings Methods
    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
} 