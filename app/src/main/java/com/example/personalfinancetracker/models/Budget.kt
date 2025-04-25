package com.example.personalfinancetracker.models

import java.io.Serializable

data class Budget(
    val amount: Double,
    val warningThreshold: Double = 0.8, // 80% of budget
    val month: Int,
    val year: Int
) : Serializable