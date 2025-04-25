package com.example.personalfinancetracker.database

import androidx.room.ColumnInfo

data class CategoryBreakdown(
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "total")
    val total: Double
) 