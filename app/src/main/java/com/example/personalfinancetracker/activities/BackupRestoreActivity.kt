package com.example.personalfinancetracker.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.adapters.BackupAdapter
import com.example.personalfinancetracker.databinding.ActivityBackupRestoreBinding
import com.example.personalfinancetracker.models.BackupItem
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.utils.BackupHelper
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupRestoreBinding
    private lateinit var backupHelper: BackupHelper
    private lateinit var preferencesManager: PreferencesManager
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var backupAdapter: BackupAdapter
    private var selectedBackup: BackupItem? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Backup & Restore"

        backupHelper = BackupHelper(this)
        preferencesManager = PreferencesManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadBackups()
    }

    private fun setupRecyclerView() {
        backupAdapter = BackupAdapter { backup ->
            selectedBackup = backup
            binding.buttonRestore.isEnabled = true
        }
        
        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(this@BackupRestoreActivity)
            adapter = backupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonBackup.setOnClickListener {
            createBackup()
        }

        binding.buttonRestore.setOnClickListener {
            selectedBackup?.let { backup ->
                showRestoreConfirmationDialog(backup)
            }
        }
    }

    private fun createBackup() {
        try {
            lifecycleScope.launch {
                transactionViewModel.getAllTransactions().collectLatest { transactions ->
                    if (transactions.isEmpty()) {
                        Toast.makeText(this@BackupRestoreActivity, "No transactions to backup", Toast.LENGTH_SHORT).show()
                        return@collectLatest
                    }

                    // Create backup file with timestamp
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val backupFileName = "backup_$timestamp.json"
                    
                    // Create backup directory if it doesn't exist
                    val backupDir = File(filesDir, "backups")
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }

                    // Create backup file
                    val backupFile = File(backupDir, backupFileName)
                    
                    // Convert transactions to JSON and save to file
                    val backupData = gson.toJson(transactions)
                    backupFile.writeText(backupData)

                    Toast.makeText(this@BackupRestoreActivity, "Backup created successfully", Toast.LENGTH_SHORT).show()
                    loadBackups()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBackups() {
        try {
            val backupDir = File(filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
                return
            }

            val backups = backupDir.listFiles { file ->
                file.name.startsWith("backup_") && file.name.endsWith(".json")
            }?.map { file ->
                BackupItem(
                    fileName = file.name,
                    date = backupHelper.getBackupDate(file.name)
                )
            }?.sortedByDescending { it.date } ?: emptyList()

            backupAdapter.updateBackups(backups)
            
            if (backups.isEmpty()) {
                binding.textViewEmptyBackups.visibility = View.VISIBLE
                binding.recyclerViewBackups.visibility = View.GONE
            } else {
                binding.textViewEmptyBackups.visibility = View.GONE
                binding.recyclerViewBackups.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load backups: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestoreConfirmationDialog(backup: BackupItem) {
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore this backup? This will replace all current transactions.")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(backup)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restoreBackup(backup: BackupItem) {
        try {
            val backupFile = File(filesDir, "backups/${backup.fileName}")
            if (!backupFile.exists()) {
                Toast.makeText(this, "Backup file not found", Toast.LENGTH_SHORT).show()
                return
            }

            lifecycleScope.launch {
                try {
                    // Read backup data
                    val backupData = backupFile.readText()
                    val type = object : TypeToken<List<Transaction>>() {}.type
                    val transactions: List<Transaction> = gson.fromJson(backupData, type)

                    // Clear all existing transactions first
                    transactionViewModel.getAllTransactions().collectLatest { existingTransactions ->
                        existingTransactions.forEach { transaction ->
                            transactionViewModel.delete(transaction)
                        }

                        // Insert restored transactions
                        transactions.forEach { transaction ->
                            transactionViewModel.insert(transaction)
                        }

                        Toast.makeText(this@BackupRestoreActivity, "Backup restored successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@BackupRestoreActivity, "Failed to restore backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to restore backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}