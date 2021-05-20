package com.example.trafficcolordetection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OpenCVLoader.initDebug()

        button_choose_a_photo_main.setOnClickListener {
            selectPhoto()
        }
    }

    private fun selectPhoto() {
        Log.d(TAG, "choose a photo")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult called")
        if(resultCode != RESULT_OK) {
            return
        }

        when(requestCode) {
            READ_REQUEST_CODE -> {
                try {
                    data?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        val image = BitmapFactory.decodeStream(inputStream)
                        original_image_view_main.setImageBitmap(image)
                        Log.d(TAG, "original image sat")

                        analyzeTrafficColor(image)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "error happened", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun analyzeTrafficColor(inputImage: Bitmap) {
        var inputMat = Mat()
        Utils.bitmapToMat(inputImage, inputMat)
        var outputMat = Mat()
        Imgproc.cvtColor(inputMat, outputMat, Imgproc.COLOR_RGB2GRAY)
        var outputImage = inputImage.copy(inputImage.config, true)
        Utils.matToBitmap(outputMat, outputImage)
        result_image_view_main.setImageBitmap(outputImage)
    }

    companion object {
        private const val READ_REQUEST_CODE = 42
        private const val TAG = "Main"
    }
}