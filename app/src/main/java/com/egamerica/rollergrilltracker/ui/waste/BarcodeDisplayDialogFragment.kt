package com.egamerica.rollergrilltracker.ui.waste

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.egamerica.rollergrilltracker.R
import java.lang.IllegalArgumentException

class BarcodeDisplayDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_BARCODE = "barcode"
        private const val ARG_PRODUCT_NAME = "product_name"
        
        fun newInstance(barcode: String, productName: String): BarcodeDisplayDialogFragment {
            val fragment = BarcodeDisplayDialogFragment()
            val args = Bundle()
            args.putString(ARG_BARCODE, barcode)
            args.putString(ARG_PRODUCT_NAME, productName)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_barcode_display, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val barcode = arguments?.getString(ARG_BARCODE) ?: ""
        val productName = arguments?.getString(ARG_PRODUCT_NAME) ?: ""
        
        val productNameTextView = view.findViewById<TextView>(R.id.text_product_name)
        val barcodeTextView = view.findViewById<TextView>(R.id.text_barcode)
        val barcodeImageView = view.findViewById<ImageView>(R.id.image_barcode)
        val closeButton = view.findViewById<Button>(R.id.button_close)
        
        productNameTextView.text = productName
        barcodeTextView.text = barcode
        
        // Generate and display the barcode
        try {
            val bitmap = generateBarcode(barcode)
            barcodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            barcodeImageView.setImageDrawable(null)
            barcodeTextView.text = getString(R.string.error_barcode_not_found)
        }
        
        closeButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun generateBarcode(barcodeData: String): Bitmap {
        val width = 500
        val height = 200
        
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                barcodeData,
                BarcodeFormat.CODE_128,
                width,
                height
            )
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            return bitmap
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not generate barcode")
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}