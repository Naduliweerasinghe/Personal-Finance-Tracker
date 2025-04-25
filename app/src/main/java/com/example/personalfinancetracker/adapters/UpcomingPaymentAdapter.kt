package com.example.personalfinancetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.UpcomingPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class UpcomingPaymentAdapter : RecyclerView.Adapter<UpcomingPaymentAdapter.UpcomingPaymentViewHolder>() {

    private var payments: List<UpcomingPayment> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    inner class UpcomingPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewPaymentTitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.textViewPaymentAmount)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewPaymentDate)

        fun bind(payment: UpcomingPayment) {
            titleTextView.text = payment.title
            amountTextView.text = numberFormat.format(payment.amount)
            dateTextView.text = dateFormat.format(payment.dueDate)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingPaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_payment, parent, false)
        return UpcomingPaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpcomingPaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount(): Int = payments.size

    fun updatePayments(newPayments: List<UpcomingPayment>) {
        payments = newPayments
        notifyDataSetChanged()
    }
} 