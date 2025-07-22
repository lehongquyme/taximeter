package com.teasoft.taxi.meter.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taxi.R
import com.example.taxi.databinding.DialogPriceSettingBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teasoft.taxi.meter.Price
import com.teasoft.taxi.meter.adapter.PriceAdapter

class PriceSettingDialog : DialogFragment() {
    private var _binding: DialogPriceSettingBinding? = null
    private val binding get() = _binding!!
    lateinit var priceList: ArrayList<Price>
    lateinit var priceAdapter: PriceAdapter
    lateinit var valueOpenFee: String
    lateinit var Fee: EditText
    var checkEmpty: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPriceSettingBinding.inflate(inflater, container, false)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        priceAdapter = PriceAdapter(requireContext())
        priceList = ArrayList()
        priceList.add(Price("0", "", ""))
        priceList.add(Price("0", getString(R.string.end_dialog), ""))

        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
            it.window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

        }
        with(binding) {

            Fee = binding.edtOpenFee

            rcvSetting.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            priceAdapter.priceList = priceList
            rcvSetting.adapter = priceAdapter

            switchOpenFee.setOnCheckedChangeListener { _, isChecked ->
                edtOpenFee.visibility = if (isChecked) View.VISIBLE else View.GONE
                if (!isChecked) {
                    Fee.setText("0")
                    valueOpenFee = "0"
                }

            }

            btnDone.setOnClickListener {
                if (Fee != null && edtOpenFee.text.toString() != "") {

                    valueOpenFee = Fee!!.text.toString()
                    Log.d("aa", valueOpenFee)
                } else {
                    valueOpenFee = "0"
                    Log.d("aa", valueOpenFee)

                }
                Log.e("onViewCreated: ", valueOpenFee)
                priceList = priceAdapter.changeData() as ArrayList<Price>
                priceList.forEach {
                    if (it.endNumber == "" || it.price == "" || it.startNumber == "") {
                        Toast.makeText(requireContext(), "edt empty", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                val gson = Gson()
                val editor =
                    requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
                val json = gson.toJson(priceList)
                editor.putString("priceList", json)
                editor.putString("openFee", valueOpenFee)

                editor.apply()
                dismiss()
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            btnAdd.setOnClickListener {
                val id = priceList.size - 2
                priceList.forEach {
                    if (it.endNumber == "" || it.price == "" || it.startNumber == "") {
                        return@setOnClickListener
                    }
                }
                if (priceList.size == 1) {
                    priceList.add(priceList.size - 1, Price("0", "", ""))
                } else {
                    priceList.add(priceList.size - 1, Price(priceList[id].endNumber, "", ""))
                    priceList.last().price = ""
                }

                priceAdapter.priceList = priceList

            }
            btnRemove.setOnClickListener {
                if (priceList.size > 1) {
                    priceList.removeAt(priceList.size - 2)
                    priceList[0].startNumber = "0"
                    priceAdapter.priceList = priceList

                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    }

    override fun onResume() {
        super.onResume()
        getDataFromSharePre()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun getDataFromSharePre() {
        val gson = Gson()
        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (prefs.contains("openFee")) {
            val open = prefs.getString("openFee", "0")
            binding.edtOpenFee.setText(open.toString())
            if (open.toString().toInt() > 0) {
                binding.switchOpenFee.isChecked = true
            }
        }

        if (prefs.contains("priceList")) {
            val json = prefs.getString("priceList", "")
            val list = gson.fromJson<List<Price>>(json, object : TypeToken<List<Price>>() {}.type)
            priceList = list as ArrayList<Price>
            priceList.last().endNumber = getString(R.string.end_dialog)
            priceAdapter.priceList = priceList
        }
    }
}