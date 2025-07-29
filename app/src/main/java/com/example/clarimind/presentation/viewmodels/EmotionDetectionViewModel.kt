package com.example.clarimind.presentation.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import com.example.clarimind.ml.EmotionDetectionModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EmotionDetectionViewModel : ViewModel() {

    // Define emotion labels corresponding to FER-2013 dataset
    private val emotionLabels = arrayOf(
        "Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"
    )

    fun processEmotionDetection(
        bitmap: Bitmap,
        context: Context,
        onEmotionDetected: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val emotion = withContext(Dispatchers.IO) {
                    detectEmotion(bitmap, context)
                }
                onEmotionDetected(emotion)
            } catch (e: Exception) {
                println("Error in emotion detection: ${e.message}")
                e.printStackTrace()
                // Handle error and return a default emotion
                onEmotionDetected("Neutral")
            }
        }
    }

    private suspend fun detectEmotion(bitmap: Bitmap, context: Context): String {
        return withContext(Dispatchers.IO) {
            // Initialize the MobileNetV2 model
            val model = EmotionDetectionModel.newInstance(context)

            try {
                // Debug: Log original bitmap size
                println("Original bitmap size: ${bitmap.width}x${bitmap.height}")

                // Save original image for debugging
                saveImageToGallery(context, bitmap, "original_capture")

                // First, detect faces in the image
                val faceBitmap = detectAndCropFace(bitmap)

                if (faceBitmap == null) {
                    println("No face detected in the image")
                    return@withContext "Neutral"
                }

                // Debug: Log face bitmap size
                println("Face bitmap size: ${faceBitmap.width}x${faceBitmap.height}")

                // Save cropped face for debugging
                saveImageToGallery(context, faceBitmap, "cropped_face")

                // Check if cropped face is already 48x48, if not resize minimally
                val modelInputBitmap = if (faceBitmap.width == 48 && faceBitmap.height == 48) {
                    faceBitmap // Use cropped directly if it's already the right size
                } else {
                    Bitmap.createScaledBitmap(faceBitmap, 48, 48, true) // Only resize if needed
                }

                // Save what we're actually sending to model for debugging
                saveImageToGallery(context, modelInputBitmap, "model_input")

                // Convert bitmap to ByteBuffer (RGB format)
                val byteBuffer = bitmapToByteBuffer(modelInputBitmap)

                // Create input tensor for MobileNetV2 (RGB: 48x48x3)
                val inputFeature0 = TensorBuffer.createFixedSize(
                    intArrayOf(1, 48, 48, 3), // 3 channels for RGB
                    DataType.FLOAT32
                )
                inputFeature0.loadBuffer(byteBuffer)

                // Run model inference
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // Process the output to get the predicted emotion
                val predictedEmotion = processModelOutput(outputFeature0)

                predictedEmotion
            } catch (e: Exception) {
                println("Error in emotion detection: ${e.message}")
                e.printStackTrace()
                "Neutral"
            } finally {
                // Always release model resources
                model.close()
            }
        }
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap, prefix: String) {
        try {
            // Create a unique filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "${prefix}_${timestamp}.jpg"

            // Save to app's external files directory (no permissions needed)
            val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ClariMind_Debug")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            val file = File(appDir, filename)

            FileOutputStream(file).use { out ->
                // Use higher quality for debugging (95% instead of 90%)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            println("Image saved: ${file.absolutePath}")

        } catch (e: IOException) {
            println("Error saving image: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun detectAndCropFace(bitmap: Bitmap): Bitmap? {
        return suspendCoroutine { continuation ->
            // Configure face detection options for better accuracy
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // Changed to ACCURATE
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.15f) // Slightly larger minimum face size for better quality
                // Tracking is disabled by default, no need to call enableTracking()
                .build()

            val detector = FaceDetection.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Get the first (largest) face
                        val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                        face?.let {
                            // Use smaller expansion to preserve more detail
                            val expandedRect = expandBoundingBoxOptimized(it.boundingBox, bitmap.width, bitmap.height)

                            // Crop the face from the original bitmap with better quality
                            val faceBitmap = cropFaceOptimized(bitmap, expandedRect)

                            println("Face detected at: ${expandedRect.left}, ${expandedRect.top}, ${expandedRect.width()}x${expandedRect.height()}")
                            continuation.resume(faceBitmap)
                        } ?: run {
                            println("Face object is null")
                            continuation.resume(null)
                        }
                    } else {
                        println("No faces detected")
                        continuation.resume(null)
                    }

                    // Clean up detector
                    detector.close()
                }
                .addOnFailureListener { e ->
                    println("Face detection failed: ${e.message}")
                    e.printStackTrace()
                    detector.close()
                    continuation.resume(null)
                }
        }
    }

    private fun expandBoundingBoxOptimized(originalRect: Rect, imageWidth: Int, imageHeight: Int): Rect {
        // Reduced expansion factor to preserve more detail (10% instead of 20%)
        val expandFactor = 0.1f
        val expandX = (originalRect.width() * expandFactor).toInt()
        val expandY = (originalRect.height() * expandFactor).toInt()

        val left = maxOf(0, originalRect.left - expandX)
        val top = maxOf(0, originalRect.top - expandY)
        val right = minOf(imageWidth, originalRect.right + expandX)
        val bottom = minOf(imageHeight, originalRect.bottom + expandY)

        return Rect(left, top, right, bottom)
    }

    private fun cropFaceOptimized(bitmap: Bitmap, rect: Rect): Bitmap {
        // Ensure we have valid dimensions
        val width = rect.width()
        val height = rect.height()

        if (width <= 0 || height <= 0) {
            return bitmap
        }

        // Create bitmap with ARGB_8888 config for better quality
        return Bitmap.createBitmap(
            bitmap,
            rect.left,
            rect.top,
            width,
            height,
            null,
            true // Use filtering for better quality
        )
    }

    private fun preprocessBitmapMinimal(bitmap: Bitmap): Bitmap {
        // Only resize to 48x48 with high quality filtering - no enhancement
        return Bitmap.createScaledBitmap(bitmap, 48, 48, true)
    }

    private fun enhanceContrastOptimized(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Apply gentler contrast enhancement to preserve details
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Reduced contrast factor (1.1 instead of 1.2) to avoid over-enhancement
            val enhancedR = kotlin.math.min(255, kotlin.math.max(0, ((r - 128) * 1.1 + 128).toInt()))
            val enhancedG = kotlin.math.min(255, kotlin.math.max(0, ((g - 128) * 1.1 + 128).toInt()))
            val enhancedB = kotlin.math.min(255, kotlin.math.max(0, ((b - 128) * 1.1 + 128).toInt()))

            pixels[i] = (0xFF shl 24) or (enhancedR shl 16) or (enhancedG shl 8) or enhancedB
        }

        enhancedBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return enhancedBitmap
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // MobileNetV2 expects RGB input: 48x48x3 = 6912 elements
        val byteBuffer = ByteBuffer.allocateDirect(4 * 48 * 48 * 3) // 4 bytes per float, 3 channels
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(48 * 48)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            // Extract RGB values and normalize to [0, 1] range
            // MobileNetV2 typically expects input normalized to [0, 1]
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            // Store RGB values in the ByteBuffer
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    private fun processModelOutput(outputBuffer: TensorBuffer): String {
        val probabilities = outputBuffer.floatArray

        // Debug: Print probabilities to see what the model is outputting
        println("Model probabilities: ${probabilities.contentToString()}")

        // Find the index with the highest probability
        var maxIndex = 0
        var maxProbability = probabilities[0]

        for (i in probabilities.indices) {
            if (probabilities[i] > maxProbability) {
                maxProbability = probabilities[i]
                maxIndex = i
            }
        }

        // Get the predicted emotion
        val predictedEmotion = if (maxIndex < emotionLabels.size) {
            emotionLabels[maxIndex]
        } else {
            "Neutral"
        }

        println("Predicted emotion: $predictedEmotion with confidence: $maxProbability")

        // Return emotion only if confidence is above threshold
        return if (maxProbability > 0.3f) { // Adjust threshold as needed
            predictedEmotion
        } else {
            "Neutral" // Low confidence, return neutral
        }
    }
}