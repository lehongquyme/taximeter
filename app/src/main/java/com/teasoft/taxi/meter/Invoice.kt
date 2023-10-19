package com.teasoft.taxi.meter

data class Invoice(
    val duration: String,
    val distance: Long,
    val flagfall: Long,
    val total_fare: Long
)

