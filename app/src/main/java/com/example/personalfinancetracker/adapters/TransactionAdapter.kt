package com.example.personalfinancetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.utils.PreferencesManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val preferencesManager: PreferencesManager,
    private val onItemClick: (Transaction) -> Unit = {}
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    init {
        updateCurrency()
    }

    fun updateCurrency() {
        val currencyCode = preferencesManager.getCurrency()
        numberFormat.currency = Currency.getInstance(currencyCode)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

            // Set click listener
            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}