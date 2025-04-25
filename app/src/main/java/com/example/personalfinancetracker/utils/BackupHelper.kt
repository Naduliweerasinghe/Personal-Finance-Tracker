package com.example.personalfinancetracker.utils

import android.content.Context
import com.example.personalfinancetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupHelper(private val context: Context) {

    private val gson = Gson()
    private val backupDir = File(context.filesDir, "backups")
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }

    fun backupTransactions(transactions: List<Transaction>): Boolean {
        return try {
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")
            backupFile.writeText(transactions.toJson())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAvailableBackups(): List<String> {
        return backupDir.listFiles { file -> 
            file.isFile && file.name.startsWith("backup_") && file.name.endsWith(".json")
        }?.map { it.name } ?: emptyList()
    }

    fun getBackupDate(fileName: String): Date {
        // Extract timestamp from filename (backup_yyyyMMdd_HHmmss.json)
        val timestamp = fileName.substring(7, fileName.length - 5) // Remove "backup_" and ".json"
        return dateFormat.parse(timestamp) ?: Date()
    }

    fun restoreTransactions(fileName: String): List<Transaction>? {
        return try {
            val backupFile = File(backupDir, fileName)
            if (!backupFile.exists()) return null
            
            val json = backupFile.readText()
            json.fromJson<List<Transaction>>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun List<Transaction>.toJson(): String {
        return gson.toJson(this)
    }

    private inline fun <reified T> String.fromJson(): T {
        return gson.fromJson(this, T::class.java)
    }

    fun formatBackupFileName(fileName: String): String {
        // Remove prefix and extension to get the date part
        val datePart = fileName
            .removePrefix("backup_")
            .removeSuffix(".json")

        // Parse the date
        val inputFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(datePart)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            fileName
        }
    }
}