package com.example.personalfinancetracker.database

import androidx.room.*
import com.example.personalfinancetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE isExpense = 1 
        GROUP BY category
    """)
    fun getCategoryBreakdown(): Flow<List<CategoryBreakdown>>

    @Query("SELECT SUM(CASE WHEN isExpense = 1 THEN -amount ELSE amount END) FROM transactions")
    fun getTotalBalance(): Flow<Double>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
} 