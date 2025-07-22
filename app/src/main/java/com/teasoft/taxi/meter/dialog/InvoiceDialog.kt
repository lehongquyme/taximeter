package com.teasoft.taxi.meter.dialog

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.taxi.R
import com.example.taxi.databinding.DialogInvoiceBinding
import com.teasoft.taxi.meter.Invoice
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

class InvoiceDialog(var invoice: Invoice) : DialogFragment() {
    private var bitmapp: Bitmap? = null
    private val REQUEST_CODE = 1
    lateinit var binding: DialogInvoiceBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.fullScreenAlert)
        binding = DialogInvoiceBinding.inflate(layoutInflater)
        builder?.setView(binding.root)
        val dialog = builder.create()
        setData()
        binding.btnBackHome.setOnClickListener {
            dismiss()
        }
        binding.imgQr.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
        binding.imgZoomQR.setOnClickListener {
            if (bitmapp != null) {
                zoomImage()
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE)
            }
        }

        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            // Use the bitmap as needed
            changeBitmapToString(bitmap)
            binding.imgZoomQR.setImageBitmap(bitmap)
            bitmapp = bitmap
        }
    }

    private fun setData() {
        binding.txtDurationDialog.text = invoice.duration
        binding.txtDistanceDialog.text = invoice.distance.toString() + " km"
        binding.txtFlagfallDialog.text = NumberFormat.getInstance().format(invoice.flagfall)+ getString(R.string.unit_money)
        binding.txtTotalFareDialog.text = NumberFormat.getInstance().format(invoice.total_fare)+ getString(R.string.unit_money)
        setImageQR()
    }

    private fun zoomImage() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_image)
        dialog.window?.setBackgroundDrawableResource(R.drawable.background)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val img =
            dialog.findViewById(R.id.img_QrDialog) as com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
        val btn = dialog.findViewById(R.id.btn_Dismiss) as Button
        img.setImage(ImageSource.bitmap(bitmapp!!))
        dialog.show()
        btn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun changeBitmapToString(bitmap: Bitmap) {
        val bitmapUri: Uri = getImageUri(requireContext(), bitmap)
        val bitmapUriString: String = bitmapUri.toString()
        val editor = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)?.edit()
        editor?.putString("bitmap_uri", bitmapUriString)
        editor?.apply()
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun setImageQR() {
        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        if(sharedPreferences?.contains("bitmap_uri")!!){
            val bitmapUriString = sharedPreferences?.getString("bitmap_uri", null)
            if (bitmapUriString != null) {
                val bitmapUri =
                    Uri.parse(bitmapUriString)
                val inputStream = requireActivity()?.contentResolver?.openInputStream(bitmapUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imgZoomQR.setImageBitmap(bitmap)
                bitmapp = bitmap
            }
        }
    }
}