package com.example.personalfinancetracker.models

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var icon: String,
    var color: Int,
    var isExpense: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: UUID.randomUUID().toString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(icon)
        parcel.writeInt(color)
        parcel.writeByte(if (isExpense) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}