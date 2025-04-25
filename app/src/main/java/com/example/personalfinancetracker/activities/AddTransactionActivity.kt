package com.example.personalfinancetracker.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.databinding.ActivityAddTransactionBinding
import com.example.personalfinancetracker.utils.DateUtils
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var selectedDate = Calendar.getInstance()
    private var isExpense = true

    private val expenseCategories = arrayOf(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Bills & Utilities",
        "Entertainment",
        "Health & Medical",
        "Education",
        "Other Expenses"
    )

    private val incomeCategories = arrayOf(
        "Salary",
        "Business",
        "Investments",
        "Freelance",
        "Rental",
        "Other Income"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupTransactionTypeToggle()
        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton()
        setupAddUpcomingPaymentButton()
    }

    private fun setupTransactionTypeToggle() {
        binding.toggleTransactionType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isExpense = checkedId == R.id.buttonExpense
                updateCategoryDropdown()
                
                // Update amount field color based on transaction type
                binding.textInputLayoutAmount.setStartIconTintList(
                    getColorStateList(if (isExpense) R.color.colorExpense else R.color.colorIncome)
                )
            }
        }
    }

    private fun setupCategoryDropdown() {
        updateCategoryDropdown()
        
        (binding.spinnerCategory as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
            val categories = if (isExpense) expenseCategories else incomeCategories
            binding.textInputLayoutCategory.editText?.setText(categories[position])
        }
    }

    private fun updateCategoryDropdown() {
        val categories = if (isExpense) expenseCategories else incomeCategories
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_category,
            categories
        )
        (binding.spinnerCategory as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.textInputLayoutDate.setEndIconOnClickListener {
            showDatePicker()
        }
        
        binding.textViewDate.setOnClickListener {
            showDatePicker()
        }

        // Set current date as default
        updateDateDisplay()
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        binding.textViewDate.setText(DateUtils.formatDate(selectedDate.time))
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        binding.textInputLayoutTitle.error = null
        binding.textInputLayoutAmount.error = null
        binding.textInputLayoutCategory.error = null

        if (binding.editTextTitle.text.toString().trim().isEmpty()) {
            binding.textInputLayoutTitle.error = "Please enter a title"
            isValid = false
        }

        if (binding.editTextAmount.text.toString().trim().isEmpty()) {
            binding.textInputLayoutAmount.error = "Please enter an amount"
            isValid = false
        }

        if ((binding.spinnerCategory as? AutoCompleteTextView)?.text.toString().trim().isEmpty()) {
            binding.textInputLayoutCategory.error = "Please select a category"
            isValid = false
        }

        return isValid
    }

    private fun saveTransaction() {
        val transaction = Transaction(
            id = UUID.randomUUID().toString(), // Generate a unique ID for each new transaction
            title = binding.editTextTitle.text.toString().trim(),
            amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0,
            category = (binding.spinnerCategory as? AutoCompleteTextView)?.text.toString(),
            date = selectedDate.time,
            isExpense = isExpense,
            notes = binding.editTextNotes.text.toString().trim()
        )

        // Save transaction using ViewModel
        transactionViewModel.insert(transaction)
        
        Snackbar.make(
            binding.root,
            "Transaction saved successfully",
            Snackbar.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun setupAddUpcomingPaymentButton() {
        binding.buttonAddUpcomingPayment.setOnClickListener {
            val intent = Intent(this, AddUpcomingPaymentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}