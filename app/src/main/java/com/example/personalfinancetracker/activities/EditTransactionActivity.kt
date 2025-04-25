package com.example.personalfinancetracker.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.databinding.ActivityEditTransactionBinding
import com.example.personalfinancetracker.utils.DateUtils
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTransactionBinding
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var selectedDate = Calendar.getInstance()
    private var isExpense = true
    private var transactionId: String = "0"

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

    private lateinit var preferencesManager: PreferencesManager

    companion object {
        const val EXTRA_TRANSACTION = "extra_transaction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Edit Transaction"

        preferencesManager = PreferencesManager(this)

        // Get transaction ID from intent
        transactionId = intent.getStringExtra("transaction_id") ?: "0"
        if (transactionId == "0") {
            finish()
            return
        }

        setupTransactionTypeToggle()
        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton()
        setupDeleteButton()
        loadTransaction()
    }

    private fun loadTransaction() {
        transactionViewModel.getTransactionById(transactionId)
        transactionViewModel.transaction.observe(this, Observer { transaction ->
            transaction?.let { trans ->
                binding.editTextTitle.setText(trans.title)
                binding.editTextAmount.setText(trans.amount.toString())
                isExpense = trans.isExpense
                selectedDate.time = trans.date
                binding.textViewDate.text = DateUtils.formatDate(trans.date)
                binding.editTextNotes.setText(trans.notes)
                
                // Update UI based on transaction type
                updateTransactionTypeUI()
                
                // Set category after updating the dropdown
                val categories = if (isExpense) expenseCategories else incomeCategories
                val categoryIndex = categories.indexOf(trans.category)
                if (categoryIndex >= 0) {
                    binding.spinnerCategory.setText(categories[categoryIndex], false)
                }
            }
        })
    }

    private fun updateTransaction() {
        val title = binding.editTextTitle.text.toString().trim()
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        val category = binding.spinnerCategory.text.toString()
        val notes = binding.editTextNotes.text.toString().trim()

        if (title.isEmpty() || amount <= 0 || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = transactionId,
            title = title,
            amount = amount,
            category = category,
            date = selectedDate.time,
            isExpense = isExpense,
            notes = notes
        )

        transactionViewModel.update(transaction)
        Snackbar.make(binding.root, "Transaction updated successfully", Snackbar.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteTransaction() {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                transactionViewModel.getTransactionById(transactionId)
                transactionViewModel.transaction.observe(this, Observer { transaction ->
                    transaction?.let { trans ->
                        transactionViewModel.delete(trans)
                        Snackbar.make(binding.root, "Transaction deleted successfully", Snackbar.LENGTH_SHORT).show()
                        finish()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        
        binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            val categories = if (isExpense) expenseCategories else incomeCategories
            binding.spinnerCategory.setText(categories[position], false)
        }
    }

    private fun updateCategoryDropdown() {
        val categories = if (isExpense) expenseCategories else incomeCategories
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_category,
            categories
        )
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.textViewDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate.time = calendar.time
                binding.textViewDate.text = DateUtils.formatDate(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun setupSaveButton() {
        binding.buttonUpdate.setOnClickListener {
            if (validateInputs()) {
                updateTransaction()
            }
        }
    }

    private fun setupDeleteButton() {
        binding.buttonDelete.setOnClickListener {
            deleteTransaction()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.editTextTitle.text.toString().trim().isEmpty()) {
            binding.textInputLayoutTitle.error = "Title is required"
            isValid = false
        } else {
            binding.textInputLayoutTitle.error = null
        }

        val amountText = binding.editTextAmount.text.toString().trim()
        if (amountText.isEmpty()) {
            binding.textInputLayoutAmount.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    binding.textInputLayoutAmount.error = "Amount must be greater than zero"
                    isValid = false
                } else {
                    binding.textInputLayoutAmount.error = null
                }
            } catch (e: NumberFormatException) {
                binding.textInputLayoutAmount.error = "Invalid amount"
                isValid = false
            }
        }

        if (binding.spinnerCategory.text.toString().isEmpty()) {
            binding.textInputLayoutCategory.error = "Category is required"
            isValid = false
        } else {
            binding.textInputLayoutCategory.error = null
        }

        return isValid
    }

    private fun updateTransactionTypeUI() {
        binding.toggleTransactionType.check(if (isExpense) R.id.buttonExpense else R.id.buttonIncome)
        updateCategoryDropdown()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_transaction, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete -> {
                deleteTransaction()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}