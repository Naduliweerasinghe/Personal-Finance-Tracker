package com.example.personalfinancetracker.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.databinding.ActivityAddUpcomingPaymentBinding
import com.example.personalfinancetracker.models.UpcomingPayment
import com.example.personalfinancetracker.utils.PreferencesManager
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class AddUpcomingPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUpcomingPaymentBinding
    private lateinit var preferencesManager: PreferencesManager
    private var selectedDate = Calendar.getInstance()

    private val categories = arrayOf(
        "Bills & Utilities",
        "Rent/Mortgage",
        "Insurance",
        "Subscriptions",
        "Loan Payment",
        "Credit Card",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpcomingPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Upcoming Payment"

        preferencesManager = PreferencesManager(this)

        setupCategoryDropdown()
        setupDatePicker()
        setupRecurringOptions()
        setupSaveButton()
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_category,
            categories
        )
        binding.menuCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.textInputLayoutDueDate.setEndIconOnClickListener {
            showDatePicker()
        }
        
        binding.editTextDueDate.setOnClickListener {
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
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.editTextDueDate.setText(dateFormat.format(selectedDate.time))
    }

    private fun setupRecurringOptions() {
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.textInputLayoutRecurringPeriod.isEnabled = isChecked
            if (!isChecked) {
                binding.menuRecurringPeriod.setText("")
            }
        }

        val recurringPeriods = arrayOf("Daily", "Weekly", "Monthly", "Yearly")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown_category, recurringPeriods)
        binding.menuRecurringPeriod.setAdapter(adapter)
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveUpcomingPayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        binding.textInputLayoutTitle.error = null
        binding.textInputLayoutAmount.error = null
        binding.textInputLayoutCategory.error = null
        binding.textInputLayoutDueDate.error = null

        if (binding.editTextTitle.text.toString().trim().isEmpty()) {
            binding.textInputLayoutTitle.error = "Please enter a title"
            isValid = false
        }

        if (binding.editTextAmount.text.toString().trim().isEmpty()) {
            binding.textInputLayoutAmount.error = "Please enter an amount"
            isValid = false
        }

        if (binding.menuCategory.text.toString().trim().isEmpty()) {
            binding.textInputLayoutCategory.error = "Please select a category"
            isValid = false
        }

        if (binding.editTextDueDate.text.toString().trim().isEmpty()) {
            binding.textInputLayoutDueDate.error = "Please select a due date"
            isValid = false
        }

        if (binding.switchRecurring.isChecked && binding.menuRecurringPeriod.text.toString().trim().isEmpty()) {
            binding.textInputLayoutRecurringPeriod.error = "Please select a recurring period"
            isValid = false
        }

        return isValid
    }

    private fun saveUpcomingPayment() {
        val upcomingPayment = UpcomingPayment(
            title = binding.editTextTitle.text.toString().trim(),
            amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0,
            category = binding.menuCategory.text.toString(),
            dueDate = selectedDate.time,
            isRecurring = binding.switchRecurring.isChecked,
            recurringPeriod = if (binding.switchRecurring.isChecked) {
                binding.menuRecurringPeriod.text.toString()
            } else "",
            notes = binding.editTextNotes.text.toString().trim()
        )

        // Save to SharedPreferences
        val upcomingPayments = preferencesManager.getUpcomingPayments().toMutableList()
        upcomingPayments.add(upcomingPayment)
        preferencesManager.saveUpcomingPayments(upcomingPayments)

        Snackbar.make(
            binding.root,
            "Upcoming payment saved successfully",
            Snackbar.LENGTH_SHORT
        ).show()

        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 