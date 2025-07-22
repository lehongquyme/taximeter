package com.teasoft.taxi.meter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taxi.R
import com.example.taxi.databinding.ActivityHomeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teasoft.taxi.meter.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity() {

    val homeViewModel: HomeViewModel by viewModels()

    //    -----------------------------------------------------------
    private val PERMISSIONS_REQUEST_CODE = 1001
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    var totalPrice = 0


    private var distance: Long = 0

    private lateinit var binding: ActivityHomeBinding
    private lateinit var handler: Handler
    private lateinit var language_usa: ImageButton
    private lateinit var language_vn: ImageButton
    private lateinit var locationManager: LocationManager
    private lateinit var locationCalculator: LocationCalculator
    val simpleDateFormat = SimpleDateFormat("HH:mm")
    var valueOpenFee: String = "0"
    val gson = Gson()
    var checkPriceListNull: Boolean = false

    var duration: Long = 0
    var firstTime: Long? = 0
    var currentTime: Long? = 0
    private fun getLanguage(): String {
        val sharedPreferences = getSharedPreferences("appTaximeter", Context.MODE_PRIVATE)
        return sharedPreferences.getString("language", "en") ?: "en"
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration().apply {
            setLocale(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun saveLanguage(language: String) {
        val sharedPreferences = getSharedPreferences("appTaximeter", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("language", language)
            apply()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = getLanguage()
        setLocale(language)
        this.referrer
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        requestPermission()
        val lang = binding.txtlanguage
        val sharedPreferences = getSharedPreferences("stateLanguage", Context.MODE_PRIVATE)
        var state = sharedPreferences.getBoolean("state", false)
        Log.d("ss", state.toString())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binding.txtlanguage.setText(language)

        binding.txtlanguage.setOnClickListener {
            if (lang.text == "en") {

                val language = "vi"
                saveLanguage(language)
                setLocale(language)
                recreate()

            } else {
                val language = "en"
                saveLanguage(language)
                setLocale(language)
                recreate()

            }
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationCalculator = LocationCalculator(locationManager)
        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val time: String = simpleDateFormat.format(Date())
                binding.txtClock.text = time
                handler.postDelayed(this, 1000)
            }
        })
        binding.btnStart.setOnClickListener {
            if (allPermissionsGranted()) {
                getDataFromSharePre()

                if (checkPriceListNull) {
                    if (checkStatus()) {
                        locationCalculator.start()

                        handler.post(object : Runnable {
                            override fun run() {
                                updateSpeed()
                                duration = currentTime!! - firstTime!!
                                currentTime = System.currentTimeMillis()
                                binding.txtDuration.text =
                                    homeViewModel.formatToDigitalClock(duration)

                                val time: String = simpleDateFormat.format(Date())
                                binding.txtClock.text = time
                                handler.postDelayed(this, 1000)
                            }
                        })
                        handler.post(object : Runnable {
                            override fun run() {

                                locationCalculator.setCheck(true)
                                distance = locationCalculator.getDistance()
                                binding.txtDistance.text = "$distance km"
                                totalPrice =
                                    homeViewModel.calculateThePrice(
                                        getDataFromSharePre(),
                                        distance.toInt(), valueOpenFee.toLong()
                                    ).toInt()
                                binding.txtTotalPrice.text = totalPrice.toString()
                                Toast.makeText(
                                    this@HomeActivity,
                                    totalPrice.toString(),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                                handler.postDelayed(this, 60000)
                            }
                        })
                        duration = 0
                        binding.txtDuration.text = duration.toString()
                        firstTime = System.currentTimeMillis()
                    } else {
                        locationCalculator.stop()
                        handler.removeCallbacksAndMessages(null)
                        homeViewModel.printBill(
                            supportFragmentManager, Invoice(
                                homeViewModel.formatToDigitalClock(duration),
                                distance, valueOpenFee.toLong(), totalPrice.toLong()
                            )
                        )
                    }

                }
            } else {
                showAlertDialog()
            }

        }

        binding.imgSetting.setOnClickListener {
            homeViewModel.displaySetPrice(supportFragmentManager)
        }
    }

    private fun requestPermission() {

//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                PERMISSIONS_REQUEST_CODE
//            )
//        }
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    private fun checkStatus(): Boolean {
        var start = getString(R.string.start_home).uppercase()
        var end = getString(R.string.end_home).uppercase()
        return if (binding.btnStart.text.toString().uppercase() == start) {
            binding.btnStart.text = end
            true
        } else {
            binding.btnStart.text = start


            false
        }
    }


    private fun updateSpeed() {
        var currentSpeed = locationCalculator.getSpeed()
        currentSpeed = (Math.round(currentSpeed * 10) / 10.0).toFloat()
        binding.txtSpeed.text = "$currentSpeed km/h"
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    private fun getDataFromSharePre(): List<Price> {
        val prefs = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (prefs.contains("priceList")) {
            checkPriceListNull = true
            val json = prefs.getString("priceList", "")
            if (prefs.contains("openFee")) {
                val open = prefs.getString("openFee", "0")
                valueOpenFee = open.toString()
            }
            return gson.fromJson(json, object : TypeToken<List<Price>>() {}.type)
        } else {
            return emptyList()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var allGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permissions[i]
                        )
                    ) {
                        // If user checked "never ask again" option
                        showAlertDialog()
                    } else {
                        // Permission denied but not "never ask again" option checked
                        requestPermission()
                    }
                    break
                }
            }
            if (allGranted) {
                // Do something with granted permissions
            }
        }
    }

    fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Permission is needed to use this feature")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { dialog, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}



