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
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

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
        // bitmap to mat(rgb)
        var inputMat = Mat()
        Utils.bitmapToMat(inputImage, inputMat)
        // convert rgb to hsv
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_RGB2HSV)
        var outputMat = Mat()
        // only show the red area
        Core.inRange(inputMat, Scalar(0.0, 100.0, 100.0), Scalar(10.0, 255.0, 255.0), outputMat)
        Log.d(TAG, "row: ${outputMat.rows()}, col: ${outputMat.cols()}")
        Log.d(TAG, "all number: ${outputMat.rows() * outputMat.cols()}")
        // remove noises
        //Imgproc.blur(outputMat, outputMat, Size(10.0, 10.0))
        // convert output to binary
        Imgproc.threshold(outputMat, outputMat, 80.0, 255.0, Imgproc.THRESH_BINARY)

        if(voteTrafficColor(outputMat)) {
            text_view_result_main.text = "赤信号です"
        } else {
            text_view_result_main.text = "赤信号ではありません"
        }

        // convert mat to bitmap
        var outputImage = inputImage.copy(inputImage.config, true)
        Utils.matToBitmap(outputMat, outputImage)
        // show the result image
        result_image_view_main.setImageBitmap(outputImage)
    }

    private fun voteTrafficColor(image: Mat): Boolean {
        var labelImg = Mat()
        var stats = Mat()
        var centroids = Mat()
        val labelNum = Imgproc.connectedComponentsWithStats(image, labelImg, stats, centroids)

        var vote = 0
        var allArea = 0
        for(i in 0..labelNum) {
            val result = IntArray(labelNum)
            stats.get(i, Imgproc.CC_STAT_AREA, result)
            Log.d(TAG, "$i, ${result[0]}")
            if(i == 0) allArea = result[0]
            else vote += result[0]
        }
        return vote * 100 > allArea
    }

    companion object {
        private const val READ_REQUEST_CODE = 42
        private const val TAG = "Main"
        private const val hMin = 0.0
        private const val hMax = 8.0
        private const val sMin = 144.0
        private const val sMax = 194.0
        private const val vMin = 153.0
        private const val vMax = 252.0
    }
}