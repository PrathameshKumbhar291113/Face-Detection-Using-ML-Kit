package com.facedetection


import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.facedetection.ui.theme.FacedetectionTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FacedetectionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
//                    MLKitFaceDetection()
                    CameraPreviewWithFaceDetection()
                }
            }
        }
    }
}

/*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MLKitFaceDetection() {
    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            Column {
                Text(text = "Camera Permission Denied.")
            }
        }) {
        FaceRecognitionScreenContent()
    }
}
@Composable
fun FaceRecognitionScreenContent() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var faceDetected by remember { mutableStateOf(false) }
    var blinkDetected by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isCameraEnabled by remember { mutableStateOf(true) }
    val outputDirectory = getOutputDirectory(context)
    val cameraExecutor = Executors.newSingleThreadExecutor()

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isCameraEnabled) {
                Column(
                    modifier = Modifier
                        .height(200.dp)
                        .align(Alignment.Center)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor = ContextCompat.getMainExecutor(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val cameraSelector = CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                                    .build()

                                val imageCapture = ImageCapture.Builder().build()

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setTargetResolution(Size(previewView.width, previewView.height))
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .apply {
                                        setAnalyzer(executor, FaceAnalyzer(ctx, imageCapture, outputDirectory, cameraExecutor) { detected, blink, uri ->
                                            faceDetected = detected
                                            blinkDetected = blink
                                            if (detected && blink) {
                                                capturedImageUri = uri
                                                isCameraEnabled = false
                                            }
                                        })
                                    }

                                cameraProvider.unbindAll()
                                try {
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture,
                                        imageAnalysis
                                    )
                                } catch (exc: Exception) {
                                    Log.e("FaceDetection", "Use case binding failed", exc)
                                }
                            }, executor)
                            previewView
                        }
                    )
                }

                Text(
                    text = when {
                        faceDetected && blinkDetected -> "Blink detected. Capturing image..."
                        faceDetected -> "Blink your eyes to capture the image."
                        else -> "Show your face."
                    },
                    fontSize = 30.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                capturedImageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
*/


/*@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceDetectionPreview() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    val blinkThreshold = 0.3f  // Define a threshold for eye open probability

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        @SuppressLint("UnsafeExperimentalUsageError")
                        val mediaImage = imageProxy.image
                        mediaImage?.let { image ->
                            val imageRotation = imageProxy.imageInfo.rotationDegrees
                            val inputImage = InputImage.fromMediaImage(image, imageRotation)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    for (face in faces) {
                                        val leftEyeOpenProbability = face.leftEyeOpenProbability
                                        val rightEyeOpenProbability = face.rightEyeOpenProbability

                                        if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                                            val isLeftEyeBlinking =
                                                leftEyeOpenProbability < blinkThreshold
                                            val isRightEyeBlinking =
                                                rightEyeOpenProbability < blinkThreshold

                                            if (isLeftEyeBlinking || isRightEyeBlinking) {
                                                // A blink is detected
                                                // Handle blink detection logic here (e.g., show a message or update UI)
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(context))
    }
}*/

//1.
//-------------------------------------------------------------------------------------------------------

// working function as blink is detecting in the below function DO NOT DELETE
/*@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithFaceDetection() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    // Face Detection Setup
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    LaunchedEffect(previewView) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Image Analysis Use Case
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        mediaImage?.let { image ->
                            val imageRotation = imageProxy.imageInfo.rotationDegrees
                            val inputImage = InputImage.fromMediaImage(image, imageRotation)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    var isBlinkDetected = false
                                    for (face in faces) {
                                        val leftEyeOpenProbability = face.leftEyeOpenProbability
                                        val rightEyeOpenProbability = face.rightEyeOpenProbability

                                        if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                                            val blinkThreshold = 0.3f // Define a threshold for eye open probability

                                            val isLeftEyeBlinking = leftEyeOpenProbability < blinkThreshold
                                            val isRightEyeBlinking = rightEyeOpenProbability < blinkThreshold

                                            if (isLeftEyeBlinking || isRightEyeBlinking) {
                                                isBlinkDetected = true
                                            }
                                        }
                                    }

                                    if (isBlinkDetected) {
                                        // Show toast if a blink is detected
                                        Toast.makeText(context, "User blinked an eye", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Bind use cases to lifecycle
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}*/

//-------------------------------------------------------------------------------------------------------

//2.
//-----------------------------------------------------------------
//below code is also working -- it includes the text showing the user state
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithFaceDetection() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    var isFaceDetected by remember { mutableStateOf(false) }
    var isBlinkDetected by remember { mutableStateOf(false) }
    var detectionText by remember { mutableStateOf("Show face") }

    // Face Detection Setup
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    LaunchedEffect(previewView) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Image Analysis Use Case
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        mediaImage?.let { image ->
                            val imageRotation = imageProxy.imageInfo.rotationDegrees
                            val inputImage = InputImage.fromMediaImage(image, imageRotation)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    if (faces.isNotEmpty()) {
                                        isFaceDetected = true
                                        var isBlinkDetectedLocal = false
                                        for (face in faces) {
                                            val leftEyeOpenProbability = face.leftEyeOpenProbability
                                            val rightEyeOpenProbability = face.rightEyeOpenProbability

                                            if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                                                val blinkThreshold = 0.3f
                                                val isLeftEyeBlinking = leftEyeOpenProbability < blinkThreshold
                                                val isRightEyeBlinking = rightEyeOpenProbability < blinkThreshold

                                                if (isLeftEyeBlinking || isRightEyeBlinking) {
                                                    isBlinkDetectedLocal = true
                                                }
                                            }
                                        }

                                        if (isBlinkDetectedLocal) {
                                            isBlinkDetected = true
                                            detectionText = "Eye blinked by user and saving image"
                                            Toast.makeText(context, "Eye blinked by user", Toast.LENGTH_SHORT).show()
                                        } else {
                                            detectionText = "Blink an eye to click image"
                                        }
                                    } else {
                                        isFaceDetected = false
                                        detectionText = "Show face"
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Bind use cases to lifecycle
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = detectionText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

//-----------------------------------------------------------------

//3.
//-------------------------------------------below code is only kept for the text view showing below camera screen-------------
// rather than that use do not use the code------not working(approach used --
// save image on blink of eye -- then display in image view

/*@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithFaceDetection() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    // State to control visibility
    var showImage by remember { mutableStateOf(false) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Show face") }

    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    // Coroutine scope to launch asynchronous tasks
    val scope = rememberCoroutineScope()

    LaunchedEffect(previewView) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        scope.launch {
                            val result = processImageProxy(imageProxy, faceDetector)
                            statusMessage = result.first
                            if (result.second) {
                                // Ensure the image is not closed before saving
                                val path = saveImageToLocalDirectory(imageProxy.image!!, context)
                                imagePath = path
                                showImage = true
                            }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showImage && imagePath != null) {
            Image(
                painter = rememberAsyncImagePainter(imagePath),
                contentDescription = "Saved Image",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.Center)
            )
            Text(
                text = statusMessage,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private suspend fun processImageProxy(
    imageProxy: ImageProxy,
    faceDetector: FaceDetector
): Pair<String, Boolean> {
    val mediaImage = imageProxy.image
    return if (mediaImage != null) {
        val imageRotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, imageRotation)
        try {
            val task = faceDetector.process(inputImage)
            val faces = task.await() // Use await() to get the result
            var isBlinkDetected = false
            for (face in faces) {
                val leftEyeOpenProbability = face.leftEyeOpenProbability
                val rightEyeOpenProbability = face.rightEyeOpenProbability

                if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                    val blinkThreshold = 0.3f // Define a threshold for eye open probability
                    val isLeftEyeBlinking = leftEyeOpenProbability < blinkThreshold
                    val isRightEyeBlinking = rightEyeOpenProbability < blinkThreshold

                    if (isLeftEyeBlinking || isRightEyeBlinking) {
                        isBlinkDetected = true
                    }
                }
            }

            if (isBlinkDetected) {
                Pair("Eye is blinked by user and saving image.", true)
            } else if (faces.isNotEmpty()) {
                Pair("Blink an eye to click image", false)
            } else {
                Pair("Show face", false)
            }
        } catch (e: Exception) {
            Log.e("FaceDetection", "Error processing image", e)
            Pair("Error processing image", false)
        } finally {
            imageProxy.close()
        }
    } else {
        Pair("No image available", false)
    }
}

private suspend fun saveImageToLocalDirectory(mediaImage: Image, context: Context): String {
    return withContext(Dispatchers.IO) {
        val planes = mediaImage.planes
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val imageFileName = "${UUID.randomUUID()}.jpg"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, imageFileName)

        FileOutputStream(imageFile).use { outputStream ->
            outputStream.write(bytes)
        }

        mediaImage.close()

        imageFile.absolutePath
    }
}
 */

//-----------------------------------------------------------------------



//4.
// ------------------------------------below code is working for blink of eye and showing it in image view ----------
//issue with the blink of eye is that if someone has the image with the eyes closed then it will take image


/*
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithFaceDetection() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    var isFaceDetected by remember { mutableStateOf(false) }
    var isBlinkDetected by remember { mutableStateOf(false) }
    var detectionText by remember { mutableStateOf("Show face") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showImage by remember { mutableStateOf(false) }

    // Face Detection Setup
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    LaunchedEffect(previewView) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Image Analysis Use Case
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        mediaImage?.let { image ->
                            val imageRotation = imageProxy.imageInfo.rotationDegrees
                            val inputImage = InputImage.fromMediaImage(image, imageRotation)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    if (faces.isNotEmpty()) {
                                        isFaceDetected = true
                                        var isBlinkDetectedLocal = false
                                        for (face in faces) {
                                            val leftEyeOpenProbability = face.leftEyeOpenProbability
                                            val rightEyeOpenProbability = face.rightEyeOpenProbability

                                            if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                                                val blinkThreshold = 0.3f
                                                val isLeftEyeBlinking = leftEyeOpenProbability < blinkThreshold
                                                val isRightEyeBlinking = rightEyeOpenProbability < blinkThreshold

                                                if (isLeftEyeBlinking || isRightEyeBlinking) {
                                                    isBlinkDetectedLocal = true
                                                    captureImage(imageProxy, context) { bitmap ->
                                                        capturedImage = bitmap
                                                        showImage = true
                                                    }
                                                }
                                            }
                                        }

                                        if (isBlinkDetectedLocal) {
                                            isBlinkDetected = true
                                            detectionText = "Eye blinked by user and saving image"
                                            Toast.makeText(context, "Eye blinked by user", Toast.LENGTH_SHORT).show()
                                        } else {
                                            detectionText = "Blink an eye to click image"
                                        }
                                    } else {
                                        isFaceDetected = false
                                        detectionText = "Show face"
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Bind use cases to lifecycle
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!showImage) {
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showImage && capturedImage != null) {
            Image(
                bitmap = capturedImage!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = detectionText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

// Function to capture and convert ImageProxy to Bitmap
@OptIn(ExperimentalGetImage::class)
private fun captureImage(imageProxy: ImageProxy, context: Context, onImageCaptured: (Bitmap) -> Unit) {
    val mediaImage = imageProxy.image
    mediaImage?.let { img ->
        // Convert Image to Bitmap
        val imageBitmap = mediaImage.toBitmap()

        // Get the rotation degrees from the ImageProxy
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // Rotate the bitmap to the correct orientation
        val rotatedBitmap = rotateBitmap(imageBitmap, rotationDegrees)

        // Notify the callback with the rotated bitmap
        onImageCaptured(rotatedBitmap)
    }
    // Close the imageProxy to prevent memory leaks
    imageProxy.close()
}

// Extension function to convert Image to Bitmap
private fun Image.toBitmap(): Bitmap {
    if (format != ImageFormat.YUV_420_888) {
        throw IllegalArgumentException("Unsupported image format: $format")
    }

    val planes = planes
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val width = width
    val height = height

    // Allocate memory for the YUV planes
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    // Create byte arrays to hold the YUV data
    val yData = ByteArray(ySize)
    val uData = ByteArray(uSize)
    val vData = ByteArray(vSize)

    // Copy data from ByteBuffer to byte arrays
    yBuffer.get(yData)
    uBuffer.get(uData)
    vBuffer.get(vData)

    // Create a Bitmap
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Convert YUV to RGB
    val rgbBytes = ByteArray(width * height * 4) // ARGB_8888 has 4 bytes per pixel
    yuvToRgb(yData, uData, vData, width, height, rgbBytes)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgbBytes))

    return bitmap
}

// Convert YUV to RGB
private fun yuvToRgb(yData: ByteArray, uData: ByteArray, vData: ByteArray, width: Int, height: Int, rgbBytes: ByteArray) {
    val ySize = width * height
    val uvSize = uData.size + vData.size

    // Conversion factors
    val yuvToRgbConversion = floatArrayOf(1.164f, 0.0f, 1.596f, 0.0f, -0.813f, -0.391f, 0.0f, 2.018f)
    val uvToRgbConversion = floatArrayOf(0.0f, 1.596f, -0.813f, 1.164f, -0.391f, 0.0f, 2.018f, 0.0f)

    var yIndex = 0
    var uvIndex = 0

    for (i in 0 until height) {
        for (j in 0 until width) {
            val y = (yData[yIndex].toInt() and 0xFF) - 16
            val u = (uData[uvIndex].toInt() and 0xFF) - 128
            val v = (vData[uvIndex].toInt() and 0xFF) - 128

            val r = (yuvToRgbConversion[0] * y + yuvToRgbConversion[2] * v).toInt().coerceIn(0, 255)
            val g = (yuvToRgbConversion[3] * y + yuvToRgbConversion[5] * u + yuvToRgbConversion[6] * v).toInt().coerceIn(0, 255)
            val b = (yuvToRgbConversion[7] * y + uvToRgbConversion[6] * u).toInt().coerceIn(0, 255)

            val rgbIndex = (i * width + j) * 4
            rgbBytes[rgbIndex] = (r).toByte()
            rgbBytes[rgbIndex + 1] = (g).toByte()
            rgbBytes[rgbIndex + 2] = (b).toByte()
            rgbBytes[rgbIndex + 3] = (-1).toByte() // Alpha channel (fully opaque)

            yIndex++
        }
    }
}

// Function to rotate Bitmap
private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}*/


//-----------------------------------------------------------------------
