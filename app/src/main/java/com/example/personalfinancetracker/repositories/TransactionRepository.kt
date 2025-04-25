package com.example.personalfinancetracker.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.database.TransactionDao
import com.example.personalfinancetracker.database.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions().map { entities ->
        entities.map { it.toTransaction() }
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(TransactionEntity.fromTransaction(transaction))
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(TransactionEntity.fromTransaction(transaction))
    }
} 