package com.facedetection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executor

class FaceAnalyzer(
    private val context: Context,
    private val imageCapture: ImageCapture,
    private val outputDirectory: File,
    private val executor: Executor,
    private val onFaceDetected: (Boolean, Uri?) -> Unit
) : ImageAnalysis.Analyzer {
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(highAccuracyOpts)
    private var previousLeftEyeOpen = true
    private var previousRightEyeOpen = true

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    var isLivenessDetected = false
                    for (face in faces) {
                        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: -1.0f
                        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: -1.0f

                        if (leftEyeOpenProbability > 0.5 && rightEyeOpenProbability > 0.5) {
                            isLivenessDetected = true
                        }

                        // Detect blink
                        val leftEyeClosed = leftEyeOpenProbability < 0.5
                        val rightEyeClosed = rightEyeOpenProbability < 0.5

                        if (previousLeftEyeOpen && previousRightEyeOpen && leftEyeClosed && rightEyeClosed) {
                            // Blink detected
                            capturePhoto { uri ->
                                onFaceDetected(true, uri)
                            }
                        }

                        previousLeftEyeOpen = !leftEyeClosed
                        previousRightEyeOpen = !rightEyeClosed
                    }
                    if (!isLivenessDetected) {
                        onFaceDetected(false, null)
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {
                    onFaceDetected(false, null)
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun capturePhoto(onImageCaptured: (Uri) -> Unit) {
        val photoFile = File(outputDirectory, "gdsalesuser.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, executor, object: ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Error capturing image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val compressedFile = compressImage(photoFile)
                val compressedUri = Uri.fromFile(compressedFile)

                onImageCaptured(compressedUri)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Image Captured on Blink", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun compressImage(imageFile: File): File {
        val bitmap = BitmapFactory.decodeFile(imageFile.path)
        val rotatedBitmap = rotateBitmapIfRequired(bitmap, imageFile)
        val compressedFile = File(imageFile.parent, "gdsalesuser_compressed.jpg")
        FileOutputStream(compressedFile).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out)
        }
        return compressedFile
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, imageFile: File): Bitmap {
        val exif: ExifInterface
        try {
            exif = ExifInterface(imageFile.path)
        } catch (e: IOException) {
            e.printStackTrace()
            return bitmap
        }

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

