package com.teasoft.taxi.meter.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.teasoft.taxi.meter.Invoice
import com.teasoft.taxi.meter.Price
import com.example.taxi.R
import com.teasoft.taxi.meter.dialog.InvoiceDialog
import com.teasoft.taxi.meter.dialog.PriceSettingDialog
import java.util.concurrent.TimeUnit

class HomeViewModel() : ViewModel() {
    fun printBill(supportFragmentManager: FragmentManager, invoice: Invoice) {
        var invoiceDialog = InvoiceDialog(invoice)
        invoiceDialog.show(supportFragmentManager, "fullScreenDialog")

    }

    fun displaySetPrice(supportFragmentManager: FragmentManager) {
        var invoiceDialog = PriceSettingDialog()
        invoiceDialog.show(supportFragmentManager, "fullScreenDialog")
    }

    @SuppressLint("DefaultLocale")
    fun formatToDigitalClock(miliSeconds: Long): String {

        val hours = (TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt())
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt()

        return when {
            hours > 0 -> String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(miliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(miliSeconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        miliSeconds
                    )
                ), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(miliSeconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        miliSeconds
                    )
                )
            )

            minutes > 0 -> String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(miliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(miliSeconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        miliSeconds
                    )
                ), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(miliSeconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        miliSeconds
                    )
                )
            )

            seconds > 0 -> String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(miliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(miliSeconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        miliSeconds
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(miliSeconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        miliSeconds
                    )
                )
            )

            else -> {
                "00:00"
            }
        }

    }

    fun calculateThePrice(priceList: List<Price>, totalDistance: Int, flagfall: Long): Long {
        var distance = 0
        var totalPrice: Long = 0

        when (priceList.first().endNumber) {
            "end" -> totalPrice = totalDistance * priceList.first().price.toLong()
            else -> {
                if (totalDistance <= priceList.first().endNumber.toInt()) {
                    totalPrice = totalDistance * priceList.first().price.toLong()
                }else{
                    priceList.forEach {
                        if(it.endNumber.toDoubleOrNull() != null ){
                            if (totalDistance >= it.endNumber.toInt()) {
                                distance = it.endNumber.toInt() - it.startNumber.toInt()
                                totalPrice += distance.toLong() * it.price.toLong()
                            } else {
                                if(totalDistance >= it.startNumber.toInt()){
                                    totalPrice += (totalDistance - it.startNumber.toInt()) * it.price.toLong()
                                }
                            }
                            Log.e("calculateThePrice: ", totalPrice.toString())

                        }else{
                            if(totalDistance > it.startNumber.toInt()){
                                totalPrice += (totalDistance - it.startNumber.toLong()) * it.price.toInt()
                                Log.e("calculateThePrice: ", totalPrice.toString())

                            }
                        }
                    }

                }
            }
        }

        return totalPrice + flagfall
    }


}