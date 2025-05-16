package com.example.personalfinancetracker.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.databinding.ActivityBackupRestoreBinding
import com.example.personalfinancetracker.utils.BackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupRestoreBinding
    private lateinit var backupManager: BackupManager
    private lateinit var backupAdapter: ArrayAdapter<String>
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Backup & Restore"

        backupManager = BackupManager(this)
        setupViews()
        loadBackups()
    }

    private fun setupViews() {
        binding.buttonCreateBackup.setOnClickListener {
            createBackup()
        }

        binding.buttonRestoreBackup.setOnClickListener {
            val selectedPosition = binding.listViewBackups.checkedItemPosition
            if (selectedPosition != ListView.INVALID_POSITION) {
                val selectedBackup = backupAdapter.getItem(selectedPosition)
                if (selectedBackup != null) {
                    // Extract the filename from the formatted string
                    val fileName = selectedBackup.substringAfterLast(" - ")
                    showRestoreConfirmationDialog(fileName)
                }
            } else {
                Toast.makeText(this, "Please select a backup to restore", Toast.LENGTH_SHORT).show()
            }
        }

        binding.listViewBackups.setOnItemClickListener { _, _, position, _ ->
            binding.buttonRestoreBackup.isEnabled = true
        }

        binding.listViewBackups.setOnItemLongClickListener { _, _, position, _ ->
            val backup = backupAdapter.getItem(position)
            if (backup != null) {
                // Extract the filename from the formatted string
                val fileName = backup.substringAfterLast(" - ")
                showDeleteConfirmationDialog(fileName)
            }
            true
        }
    }

    private fun loadBackups() {
        val backups = backupManager.getAvailableBackups()
        val formattedBackups = backups.map { fileName ->
            val date = backupManager.getBackupDate(fileName)
            "${dateFormat.format(date)} - $fileName"
        }

        backupAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_single_choice,
            formattedBackups
        )
        binding.listViewBackups.adapter = backupAdapter
        
        // Disable restore button if no backups are available
        binding.buttonRestoreBackup.isEnabled = backups.isNotEmpty()
    }

    private fun createBackup() {
        lifecycleScope.launch {
            try {
                val result = backupManager.createBackup()
                result.fold(
                    onSuccess = { fileName ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "Backup created successfully: $fileName",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadBackups()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "Failed to create backup: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@BackupRestoreActivity,
                    "Error creating backup: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showRestoreConfirmationDialog(backupFileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore this backup? This will replace all current data.")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(backupFileName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(backupFileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Backup")
            .setMessage("Are you sure you want to delete this backup?")
            .setPositiveButton("Delete") { _, _ ->
                if (backupManager.deleteBackup(backupFileName)) {
                    Toast.makeText(this, "Backup deleted successfully", Toast.LENGTH_SHORT).show()
                    loadBackups()
                } else {
                    Toast.makeText(this, "Failed to delete backup", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restoreBackup(backupFileName: String) {
        lifecycleScope.launch {
            try {
                val result = backupManager.restoreBackup(backupFileName)
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "Backup restored successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "Failed to restore backup: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@BackupRestoreActivity,
                    "Error restoring backup: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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