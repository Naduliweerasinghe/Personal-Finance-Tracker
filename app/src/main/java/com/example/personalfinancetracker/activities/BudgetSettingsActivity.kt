package com.example.personalfinancetracker.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.databinding.ActivityBudgetSettingsBinding
import com.example.personalfinancetracker.viewmodels.BudgetViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetSettingsBinding
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Budget Settings"

        setupViews()
        loadCurrentBudget()
    }

    private fun setupViews() {
        binding.buttonSaveBudget.setOnClickListener {
            val amountText = binding.editTextBudgetAmount.text.toString()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    lifecycleScope.launch {
                        val currentBudget = budgetViewModel.getCurrentBudget().first()
                        if (currentBudget != null) {
                            budgetViewModel.updateBudget(amount)
                        } else {
                            budgetViewModel.setBudget(amount)
                        }
                        Toast.makeText(this@BudgetSettingsActivity, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCurrentBudget() {
        lifecycleScope.launch {
            val currentBudget = budgetViewModel.getCurrentBudget().first()
            currentBudget?.let {
                binding.editTextBudgetAmount.setText(it.amount.toString())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}