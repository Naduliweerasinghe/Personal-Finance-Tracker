package com.example.personalfinancetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RecentTransactionAdapter : RecyclerView.Adapter<RecentTransactionAdapter.RecentTransactionViewHolder>() {

    private var transactions: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    inner class RecentTransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTransactionTitle)
        private val categoryTextView: TextView = itemView.findViewById(R.id.textViewTransactionCategory)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewTransactionDate)
        private val amountTextView: TextView = itemView.findViewById(R.id.textViewTransactionAmount)

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            categoryTextView.text = transaction.category
            dateTextView.text = dateFormat.format(transaction.date)
            amountTextView.text = numberFormat.format(transaction.amount)

            // Set amount color based on transaction type
            val colorRes = if (transaction.isExpense) R.color.colorExpense else R.color.colorIncome
            amountTextView.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentTransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return RecentTransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentTransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} 