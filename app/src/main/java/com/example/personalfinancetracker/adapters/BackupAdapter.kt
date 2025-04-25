package com.example.personalfinancetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.BackupItem
import java.text.SimpleDateFormat
import java.util.*

class BackupAdapter(
    private val onItemClick: (BackupItem) -> Unit
) : RecyclerView.Adapter<BackupAdapter.BackupViewHolder>() {

    private var backups: List<BackupItem> = emptyList()
    private var selectedPosition = RecyclerView.NO_POSITION
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    inner class BackupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.textViewBackupFileName)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewBackupDate)

        fun bind(backup: BackupItem, isSelected: Boolean) {
            fileNameTextView.text = backup.fileName
            dateTextView.text = dateFormat.format(backup.date)

            // Update background based on selection
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (isSelected) R.color.colorPrimaryLight else android.R.color.transparent
                )
            )

            // Set click listener
            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onItemClick(backup)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_backup, parent, false)
        return BackupViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(backups[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = backups.size

    fun updateBackups(newBackups: List<BackupItem>) {
        backups = newBackups
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun getSelectedBackup(): BackupItem? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            backups[selectedPosition]
        } else {
            null
        }
    }
} 