package com.example.personalfinancetracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.activities.BackupRestoreActivity
import com.example.personalfinancetracker.activities.BudgetSettingsActivity
import com.example.personalfinancetracker.activities.LoginActivity
import com.example.personalfinancetracker.databinding.FragmentSettingsBinding
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.services.NotificationService
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationService: NotificationService
    private lateinit var spinnerCurrency: Spinner
    private lateinit var switchNotifications: Switch
    private lateinit var buttonSaveSettings: Button
    private lateinit var buttonBudgetSettings: Button
    private lateinit var buttonBackupRestore: Button
    private lateinit var buttonLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferencesManager = PreferencesManager(requireContext())
        notificationService = NotificationService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        initViews()
        loadCurrentSettings()
        setupObservers()
        setupClickListeners()
    }

    private fun initViews() {
        spinnerCurrency = binding.spinnerCurrency
        switchNotifications = binding.switchNotifications
        buttonSaveSettings = binding.buttonSaveSettings
        buttonBudgetSettings = binding.buttonBudgetSettings
        buttonBackupRestore = binding.buttonBackupRestore
        buttonLogout = binding.buttonLogout

        // Setup currency spinner with valid ISO codes
        val currencies = arrayOf(
            "USD - US Dollar",
            "EUR - Euro",
            "GBP - British Pound",
            "JPY - Japanese Yen",
            "LKR - Sri Lankan Rupee"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
    }

    private fun loadCurrentSettings() {
        // Set current currency
        val currentCurrency = preferencesManager.getCurrency()
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "LKR")
        val currencyIndex = currencies.indexOfFirst { it == currentCurrency }
        if (currencyIndex >= 0) {
            spinnerCurrency.setSelection(currencyIndex)
        }

        // Set notification preference
        switchNotifications.isChecked = preferencesManager.getNotificationsEnabled()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllTransactions().collectLatest { transactions: List<Transaction> ->
                val totalBalance = transactions.sumOf { transaction ->
                    if (transaction.isExpense) -transaction.amount else transaction.amount
                }
                binding.textViewTotalBalance.text = formatCurrency(totalBalance)
            }
        }
    }

    private fun setupClickListeners() {
        buttonSaveSettings.setOnClickListener {
            saveSettings()
        }

        buttonBudgetSettings.setOnClickListener {
            // Simply navigate to BudgetSettingsActivity without any SharedPreferences
            try {
                startActivity(Intent(requireContext(), BudgetSettingsActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open budget settings", Toast.LENGTH_SHORT).show()
            }
        }

        buttonBackupRestore.setOnClickListener {
            startActivity(Intent(requireContext(), BackupRestoreActivity::class.java))
        }

        buttonLogout.setOnClickListener {
            logout()
        }

        // Handle notification toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle(isChecked)
        }

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = parent?.getItemAtPosition(position).toString()
                val currencyCode = selectedCurrency.split(" - ")[0]
                preferencesManager.setCurrency(currencyCode)
                // Refresh the UI with new currency
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.getAllTransactions().collectLatest { transactions ->
                        val totalBalance = transactions.sumOf { transaction ->
                            if (transaction.isExpense) -transaction.amount else transaction.amount
                        }
                        binding.textViewTotalBalance.text = formatCurrency(totalBalance)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun handleNotificationToggle(isEnabled: Boolean) {
        // Save the notification preference
        preferencesManager.setNotificationsEnabled(isEnabled)
        
        if (isEnabled) {
            // Show a confirmation notification when enabled
            notificationService.showBudgetWarningNotification(
                "Notifications Enabled",
                "You will now receive notifications for budget warnings and upcoming payments."
            )
            Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            // Cancel all notifications when disabled
            notificationService.cancelAllNotifications()
            Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        val selectedCurrency = spinnerCurrency.selectedItem.toString()
        val currencyCode = selectedCurrency.split(" - ")[0]
        if (currencyCode.length == 3) {
            preferencesManager.setCurrency(currencyCode)
        } else {
            preferencesManager.setCurrency("USD") // Default to USD if invalid
        }

        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        // Clear the user session
        preferencesManager.logout()

        // Navigate to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun formatCurrency(amount: Double): String {
        val currencyCode = preferencesManager.getCurrency()
        return try {
            val currency = java.util.Currency.getInstance(currencyCode)
            val symbol = currency.symbol
            String.format("%s%.2f", symbol, amount)
        } catch (e: IllegalArgumentException) {
            // Fallback to default formatting if currency code is invalid
            val currencySymbol = when (currencyCode) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                "LKR" -> "Rs."
                else -> currencyCode
            }
            String.format("%s%.2f", currencySymbol, amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}