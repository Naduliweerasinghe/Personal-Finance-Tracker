package com.example.personalfinancetracker.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class UpcomingPayment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val dueDate: Date,
    val category: String,
    val isRecurring: Boolean = false,
    val recurringPeriod: String = "", // "monthly", "weekly", etc.
    val notes: String = ""
) : Parcelable
