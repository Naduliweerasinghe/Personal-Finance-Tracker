package com.example.personalfinancetracker.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.utils.DateUtils
import java.text.NumberFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

class BudgetSettingsActivity : AppCompatActivity() {

    private lateinit var textViewMonthYear: TextView
    private lateinit var editTextBudgetAmount: EditText
    private lateinit var seekBarWarningThreshold: SeekBar
    private lateinit var textViewWarningThreshold: TextView
    private lateinit var buttonSaveBudget: Button
    private var warningThreshold: Double = 0.8 // Default 80%

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_settings)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Budget Settings"

        initViews()
        setupListeners()
    }

    private fun initViews() {
        textViewMonthYear = findViewById(R.id.textViewMonthYear)
        editTextBudgetAmount = findViewById(R.id.editTextBudgetAmount)
        seekBarWarningThreshold = findViewById(R.id.seekBarWarningThreshold)
        textViewWarningThreshold = findViewById(R.id.textViewWarningThreshold)
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget)

        // Set current month and year
        val currentDate = Date()
        textViewMonthYear.text = DateUtils.formatMonthYear(currentDate)

        // Set default values
        seekBarWarningThreshold.progress = 80
        updateWarningThresholdText()
    }

    private fun setupListeners() {
        seekBarWarningThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningThreshold = progress / 100.0
                updateWarningThresholdText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonSaveBudget.setOnClickListener {
            if (validateInputs()) {
                saveBudget()
            }
        }
    }

    private fun updateWarningThresholdText() {
        val percentage = (warningThreshold * 100).toInt()
        textViewWarningThreshold.text = "Warning at $percentage% of budget"
    }

    private fun validateInputs(): Boolean {
        if (editTextBudgetAmount.text.toString().trim().isEmpty()) {
            editTextBudgetAmount.error = "Budget amount is required"
            return false
        }

        try {
            val amount = editTextBudgetAmount.text.toString().toDouble()
            if (amount <= 0) {
                editTextBudgetAmount.error = "Budget must be greater than zero"
                return false
            }
        } catch (e: NumberFormatException) {
            editTextBudgetAmount.error = "Invalid amount"
            return false
        }

        return true
    }

    private fun saveBudget() {
        try {
            val amount = editTextBudgetAmount.text.toString().toDouble()
            
            // Save to SharedPreferences
            val sharedPreferences = getSharedPreferences("budget_prefs", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putFloat("budget_amount", amount.toFloat())
                apply()
            }

            val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            numberFormat.currency = Currency.getInstance("USD")

            Toast.makeText(
                this,
                "Budget set to ${numberFormat.format(amount)}",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving budget", Toast.LENGTH_SHORT).show()
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