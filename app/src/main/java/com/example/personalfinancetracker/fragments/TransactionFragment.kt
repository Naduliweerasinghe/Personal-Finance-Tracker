package com.example.personalfinancetracker.fragments

import android.content.Intent
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
import com.example.personalfinancetracker.activities.AddTransactionActivity
import com.example.personalfinancetracker.activities.EditTransactionActivity
import com.example.personalfinancetracker.adapters.TransactionAdapter
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransactionFragment : Fragment() {
    private lateinit var recyclerViewTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var textViewEmptyState: TextView
    private lateinit var toggleTransactionType: MaterialButtonToggleGroup
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var allTransactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel and PreferencesManager
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        preferencesManager = PreferencesManager(requireContext())

        // Initialize views
        recyclerViewTransactions = view.findViewById(R.id.recyclerViewTransactions)
        fabAddTransaction = view.findViewById(R.id.fabAddTransaction)
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState)
        toggleTransactionType = view.findViewById(R.id.toggleTransactionType)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup FAB click listener
        fabAddTransaction.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }

        // Setup transaction type toggle
        setupTransactionTypeToggle()

        // Collect transactions flow
        viewLifecycleOwner.lifecycleScope.launch {
            transactionViewModel.getAllTransactions().collectLatest { transactions ->
                allTransactions = transactions
                filterTransactions()
            }
        }

        // Observe currency changes
        viewLifecycleOwner.lifecycleScope.launch {
            transactionViewModel.currencyChanged.collectLatest {
                transactionAdapter.updateCurrency()
            }
        }
    }

    private fun setupTransactionTypeToggle() {
        toggleTransactionType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                filterTransactions()
            }
        }
    }

    private fun filterTransactions() {
        val filteredTransactions = when (toggleTransactionType.checkedButtonId) {
            R.id.buttonIncome -> allTransactions.filter { !it.isExpense }
            R.id.buttonExpense -> allTransactions.filter { it.isExpense }
            else -> allTransactions
        }
        updateUI(filteredTransactions)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            preferencesManager = preferencesManager,
            onItemClick = { transaction ->
                // Open EditTransactionActivity when a transaction is clicked
                val intent = Intent(requireContext(), EditTransactionActivity::class.java).apply {
                    putExtra("transaction_id", transaction.id)
                }
                startActivity(intent)
            }
        )
        recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun updateUI(transactions: List<Transaction>) {
        // Sort transactions by date (newest first)
        val sortedTransactions = transactions.sortedByDescending { it.date }
        
        // Update adapter with sorted transactions
        transactionAdapter.updateTransactions(sortedTransactions)
        
        // Show/hide empty state
        if (sortedTransactions.isEmpty()) {
            textViewEmptyState.visibility = View.VISIBLE
            recyclerViewTransactions.visibility = View.GONE
        } else {
            textViewEmptyState.visibility = View.GONE
            recyclerViewTransactions.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newInstance() = TransactionFragment()
    }
}