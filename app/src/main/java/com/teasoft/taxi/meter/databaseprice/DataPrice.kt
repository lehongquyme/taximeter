package com.teasoft.taxi.meter.databaseprice

import android.widget.EditText
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataPrice(
    @PrimaryKey(autoGenerate = true)
val id: Int,
    val usernameunitprice: String?,
    val price_per_kilomet: String,
    val incurred: String,
    val open_fee: String,
    val edt_name_other_fees : String,
    val edit_price_other: String,
    val  edt_vat : String,
)


