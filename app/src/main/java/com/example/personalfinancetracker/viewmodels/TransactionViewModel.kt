package com.example.personalfinancetracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinancetracker.database.TransactionEntity
import com.example.personalfinancetracker.database.TransactionDao
import com.example.personalfinancetracker.database.TransactionDatabase
import com.example.personalfinancetracker.database.CategoryBreakdown
import com.example.personalfinancetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao: TransactionDao = TransactionDatabase.getDatabase(application).transactionDao()
    
    private val _currencyChanged = MutableSharedFlow<Unit>()
    val currencyChanged: SharedFlow<Unit> = _currencyChanged
    
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions().map { entities ->
        entities.map { it.toTransaction() }
    }
    
    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    init {
        calculateTotalBalance()
    }

    fun notifyCurrencyChanged() {
        viewModelScope.launch {
            _currencyChanged.emit(Unit)
        }
    }

    fun getTransactionById(id: String) {
        viewModelScope.launch {
            try {
                val result = transactionDao.getTransactionById(id)
                if (result != null) {
                    _transaction.value = result.toTransaction()
                } else {
                    _error.value = "Transaction not found"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching transaction: ${e.message}"
            }
        }
    }

    fun insert(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.insert(transaction.toEntity())
                calculateTotalBalance()
            } catch (e: Exception) {
                _error.value = "Error inserting transaction: ${e.message}"
            }
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.delete(transaction.toEntity())
                calculateTotalBalance()
            } catch (e: Exception) {
                _error.value = "Error deleting transaction: ${e.message}"
            }
        }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.update(transaction.toEntity())
                calculateTotalBalance()
            } catch (e: Exception) {
                _error.value = "Error updating transaction: ${e.message}"
            }
        }
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    private fun calculateTotalBalance() {
        viewModelScope.launch {
            try {
                val transactions = getAllTransactions().first()
                val total = transactions.sumOf { transaction ->
                    if (transaction.isExpense) -transaction.amount else transaction.amount
                }
                _totalBalance.value = total
            } catch (e: Exception) {
                _error.value = "Error calculating total balance: ${e.message}"
            }
        }
    }

    fun getCategoryBreakdown(): Flow<Map<String, Double>> {
        return transactionDao.getCategoryBreakdown().map { breakdowns ->
            breakdowns.associate { it.category to it.total }
        }
    }

    private fun TransactionEntity.toTransaction(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            category = category,
            date = date,
            isExpense = isExpense,
            notes = notes
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            title = title,
            amount = amount,
            category = category,
            date = date,
            isExpense = isExpense,
            notes = notes
        )
    }
}

/*package com.example.personalfinancetracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinancetracker.database.Transaction
import com.example.personalfinancetracker.database.TransactionDao
import com.example.personalfinancetracker.database.TransactionDatabase
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao: TransactionDao = TransactionDatabase.getDatabase(application).transactionDao()
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    
    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction

    fun getTransactionById(id: Long) {
        viewModelScope.launch {
            val result = transactionDao.getTransactionById(id)
            _transaction.value = result
        }
    }

    fun insert(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insert(transaction) // Room will update if the ID exists
        }
    }
} */