package com.example.personalfinancetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.adapters.RecentTransactionAdapter
import com.example.personalfinancetracker.adapters.UpcomingPaymentAdapter
import com.example.personalfinancetracker.databinding.FragmentDashboardBinding
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.models.UpcomingPayment
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.BudgetViewModel
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var textViewWelcome: TextView
    private lateinit var textViewBalance: TextView
    private lateinit var textViewIncome: TextView
    private lateinit var textViewExpenses: TextView
    private lateinit var textViewBudget: TextView
    private lateinit var recyclerViewUpcomingPayments: RecyclerView
    private lateinit var recyclerViewRecentTransactions: RecyclerView
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var upcomingPaymentAdapter: UpcomingPaymentAdapter
    private lateinit var recentTransactionAdapter: RecentTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModels and PreferencesManager
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        budgetViewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        preferencesManager = PreferencesManager(requireContext())
        
        // Initialize views
        textViewWelcome = view.findViewById(R.id.textViewWelcome)
        textViewBalance = view.findViewById(R.id.textViewBalance)
        textViewIncome = view.findViewById(R.id.textViewIncome)
        textViewExpenses = view.findViewById(R.id.textViewExpenses)
        textViewBudget = view.findViewById(R.id.textViewBudget)
        recyclerViewUpcomingPayments = view.findViewById(R.id.recyclerViewUpcomingPayments)
        recyclerViewRecentTransactions = view.findViewById(R.id.recyclerViewRecentTransactions)

        // Set welcome message
        textViewWelcome.text = "Welcome"

        // Setup RecyclerViews
        setupUpcomingPaymentsRecyclerView()
        setupRecentTransactionsRecyclerView()

        // Observe transactions and update UI
        viewLifecycleOwner.lifecycleScope.launch {
            transactionViewModel.getAllTransactions().collectLatest { transactions ->
                updateFinancialData(transactions)
                updateRecentTransactions(transactions)
            }
        }

        // Observe budget and update UI
        viewLifecycleOwner.lifecycleScope.launch {
            budgetViewModel.getCurrentBudget().collectLatest { budget ->
                budget?.let {
                    binding.textViewBudget.text = formatCurrency(it.amount)
                } ?: run {
                    binding.textViewBudget.text = formatCurrency(0.0)
                }
            }
        }
    }

    private fun setupUpcomingPaymentsRecyclerView() {
        upcomingPaymentAdapter = UpcomingPaymentAdapter(
            onPaymentDue = { payment ->
                // Convert upcoming payment to transaction when it's due
                convertUpcomingPaymentToTransaction(payment)
            },
            onPaymentClick = { payment ->
                // Show delete confirmation dialog when payment is clicked
                showDeleteConfirmationDialog(payment)
            }
        )
        recyclerViewUpcomingPayments.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingPaymentAdapter
        }
        updateUpcomingPayments()
    }

    private fun showDeleteConfirmationDialog(payment: UpcomingPayment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Upcoming Payment")
            .setMessage("Are you sure you want to delete this upcoming payment?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the payment
                preferencesManager.deleteUpcomingPayment(payment.id)
                updateUpcomingPayments()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecentTransactionsRecyclerView() {
        recentTransactionAdapter = RecentTransactionAdapter()
        recyclerViewRecentTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentTransactionAdapter
        }
    }

    private fun updateUpcomingPayments() {
        val upcomingPayments = preferencesManager.getUpcomingPayments() ?: emptyList()
        upcomingPaymentAdapter.updatePayments(upcomingPayments)
    }

    private fun convertUpcomingPaymentToTransaction(payment: UpcomingPayment) {
        val transaction = Transaction(
            title = payment.title,
            amount = payment.amount,
            category = payment.category,
            date = payment.dueDate,
            isExpense = true,
            notes = if (payment.isRecurring) "Recurring payment: ${payment.recurringPeriod}" else ""
        )
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Insert the transaction into Room Database
                transactionViewModel.insert(transaction)
                
                // If it's a recurring payment, create the next payment
                if (payment.isRecurring) {
                    val nextPayment = createNextRecurringPayment(payment)
                    preferencesManager.addUpcomingPayment(nextPayment)
                }
                
                // Remove the current upcoming payment
                preferencesManager.deleteUpcomingPayment(payment.id)
                updateUpcomingPayments()
            } catch (e: Exception) {
                // Handle any errors that might occur during the conversion
                e.printStackTrace()
            }
        }
    }

    private fun createNextRecurringPayment(currentPayment: UpcomingPayment): UpcomingPayment {
        val calendar = Calendar.getInstance().apply {
            time = currentPayment.dueDate
            when (currentPayment.recurringPeriod.lowercase()) {
                "weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                "monthly" -> add(Calendar.MONTH, 1)
                "yearly" -> add(Calendar.YEAR, 1)
                else -> add(Calendar.MONTH, 1) // Default to monthly
            }
        }

        return currentPayment.copy(
            id = UUID.randomUUID().toString(),
            dueDate = calendar.time
        )
    }

    private fun updateRecentTransactions(transactions: List<Transaction>) {
        val recentTransactions = transactions
            .sortedByDescending { it.date }
            .take(5) // Show only the 5 most recent transactions
        recentTransactionAdapter.updateTransactions(recentTransactions)
    }

    private fun updateFinancialData(transactions: List<Transaction>) {
        val totalBalance = calculateTotalBalance(transactions)
        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val totalExpenses = calculateTotalExpenses(transactions)

        binding.textViewBalance.text = formatCurrency(totalBalance)
        binding.textViewIncome.text = formatCurrency(totalIncome)
        binding.textViewExpenses.text = formatCurrency(totalExpenses)
    }

    private fun calculateTotalBalance(transactions: List<Transaction>): Double {
        val currentBalance = transactions.sumOf { transaction ->
            if (transaction.isExpense) -transaction.amount else transaction.amount
        }
        
        // Add upcoming payments to the balance calculation
        val upcomingPayments = preferencesManager.getUpcomingPayments() ?: emptyList()
        val upcomingExpenses = upcomingPayments.sumOf { it.amount }
        
        return currentBalance - upcomingExpenses
    }

    private fun calculateTotalExpenses(transactions: List<Transaction>): Double {
        val currentExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }
        
        // Add upcoming payments to the expenses
        val upcomingPayments = preferencesManager.getUpcomingPayments() ?: emptyList()
        val upcomingExpenses = upcomingPayments.sumOf { it.amount }
        
        return currentExpenses + upcomingExpenses
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val currencyCode = preferencesManager.getCurrency() ?: "USD"
            val currency = Currency.getInstance(currencyCode)
            val symbol = currency.symbol
            String.format("%s%.2f", symbol, amount)
        } catch (e: IllegalArgumentException) {
            String.format("$%.2f", amount) // Default to USD format if there's an error
        }
    }

    override fun onResume() {
        super.onResume()
        updateUpcomingPayments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}