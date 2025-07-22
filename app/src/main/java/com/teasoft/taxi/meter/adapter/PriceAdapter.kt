package com.teasoft.taxi.meter.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.databinding.ItemSettingBinding
import com.teasoft.taxi.meter.Price

class PriceAdapter(context: Context) : RecyclerView.Adapter<PriceAdapter.ViewHolder>() {
    val context = context
    var text: String = ""
    var priceList = ArrayList<Price>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Price, position: Int) {
            with(binding) {
                edtEndNumber.isEnabled = position != priceList.size - 1
                txtStartNumber.text = item.startNumber.toString()
                edtEndNumber.setText(item.endNumber.toString())
                edtPrice.setText(item.price.toString())

                listener(edtPrice, position, binding, txtStartNumber)
                listener(edtEndNumber, position, binding, txtStartNumber)

            }
        }

    }

    fun changeData(): List<Price> {
        return priceList
    }

    private fun listener(
        edt: EditText,
        position: Int,
        binding: ItemSettingBinding,
        txtStart: TextView
    ) {


        edt.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK || event?.action == KeyEvent.ACTION_UP) {

                    if (edt.text.toString() != "" || edt.text.isNotBlank()) {
                        if (edt == binding.edtPrice) {
                            updatePrice(edt, position, txtStart)
                        } else {
                            updateEndNumber(edt, position, txtStart)

                        }

                    }
                    return true
                }
                return false
            }
        })

        edt.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                if (edt.text.toString() != "" || edt.text.isNotBlank()) {

                    if (edt == binding.edtPrice) {
                        updatePrice(edt, position, txtStart)
                    } else {
                        updateEndNumber(edt, position, txtStart)

                    }
                }
                true
            } else {
                false
            }
        }

    }

    private fun updateEndNumber(edt: EditText, position: Int, txtStart: TextView) {
        if (position == 0) {
            priceList[position].endNumber = edt.text.toString()
        } else {
            txtStart.text = priceList[position - 1].endNumber.toString()
            priceList[position].startNumber = txtStart.text.toString()
            if((edt.text.toString().toInt()) > (priceList[position - 1].endNumber).toInt()){
                priceList[position].endNumber = edt.text.toString()
            }

        }
    }

    private fun updatePrice(edt: EditText, position: Int, txtStart: TextView) {
        if (position == 0) {
            priceList[position].price = edt.text.toString()
        } else {
            txtStart.text = priceList[position - 1].endNumber.toString()
            priceList[position].startNumber = txtStart.text.toString()
            priceList[position].price = edt.text.toString()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return priceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(priceList[position], position)

    }

}