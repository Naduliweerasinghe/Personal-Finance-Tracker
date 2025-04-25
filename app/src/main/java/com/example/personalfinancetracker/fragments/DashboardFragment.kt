package com.example.personalfinancetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.example.personalfinancetracker.utils.PreferencesManager
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
        
        // Initialize ViewModel and PreferencesManager
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
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
    }

    private fun setupUpcomingPaymentsRecyclerView() {
        upcomingPaymentAdapter = UpcomingPaymentAdapter()
        recyclerViewUpcomingPayments.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingPaymentAdapter
        }
        updateUpcomingPayments()
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

    private fun updateRecentTransactions(transactions: List<Transaction>) {
        val recentTransactions = transactions
            .sortedByDescending { it.date }
            .take(5) // Show only the 5 most recent transactions
        recentTransactionAdapter.updateTransactions(recentTransactions)
    }

    private fun updateFinancialData(transactions: List<Transaction>) {
        val totalBalance = transactions.sumOf { transaction ->
            if (transaction.isExpense) -transaction.amount else transaction.amount
        }
        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }

        // Get the budget amount from shared preferences
        val sharedPreferences = requireContext().getSharedPreferences("budget_prefs", 0)
        val budgetAmount = sharedPreferences.getFloat("budget_amount", 0f).toDouble()

        binding.textViewBalance.text = formatCurrency(totalBalance)
        binding.textViewIncome.text = formatCurrency(totalIncome)
        binding.textViewExpenses.text = formatCurrency(totalExpenses)
        binding.textViewBudget.text = formatCurrency(budgetAmount)
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