package com.example.personalfinancetracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personalfinancetracker.models.Transaction
import java.util.Date

@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val isExpense: Boolean,
    val notes: String = ""
) {
    fun toTransaction(): Transaction = Transaction(
        id = id,
        title = title,
        amount = amount,
        category = category,
        date = date,
        isExpense = isExpense,
        notes = notes
    )

    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            title = transaction.title,
            amount = transaction.amount,
            category = transaction.category,
            date = transaction.date,
            isExpense = transaction.isExpense,
            notes = transaction.notes
        )
    }
} 