package com.example.personalfinancetracker.models

import android.os.Parcel
import android.os.Parcelable
import com.example.personalfinancetracker.R
import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var date: Date,
    var isExpense: Boolean,
    var notes: String = ""
) : Parcelable {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(amount >= 0) { "Amount cannot be negative" }
        require(category.isNotBlank()) { "Category cannot be blank" }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: UUID.randomUUID().toString(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeDouble(amount)
        parcel.writeString(category)
        parcel.writeLong(date.time)
        parcel.writeByte(if (isExpense) 1 else 0)
        parcel.writeString(notes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }

    // Utility functions
    fun getFormattedAmount(): String {
        return String.format("$%.2f", amount)
    }

    fun getFormattedDate(): String {
        return android.text.format.DateFormat.format("MMM dd, yyyy", date).toString()
    }

    fun getCategoryIcon(): Int {
        return when (category.lowercase()) {
            "food" -> R.drawable.ic_food
            "transport" -> R.drawable.ic_transport
            "entertainment" -> R.drawable.ic_entertainment
            "bills" -> R.drawable.ic_bills
            "shopping" -> R.drawable.ic_shopping
            else -> R.drawable.ic_food // Default icon
        }
    }
}