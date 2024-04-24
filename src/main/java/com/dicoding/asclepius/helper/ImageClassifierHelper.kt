package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
    private val context: Context,
    private val classifierListener: ClassifierListener?
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setScoreThreshold(0.1f)
                .setMaxResults(3)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "cancer_classification.tflite",
                options
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image classifier: ${e.message}")
            classifierListener?.onError("Failed to set up the image classifier.")
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        val imageBitmap: Bitmap? = uriToBitmap(context, imageUri)
        if (imageBitmap == null) {
            classifierListener?.onError("Failed to load image.")
            return
        }

        val tensorImage = TensorImage.fromBitmap(imageBitmap)
        val results = imageClassifier?.classify(tensorImage)

        if (results == null) {
            classifierListener?.onError("Classification failed.")
        } else {
            classifierListener?.onResults(results)
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to Bitmap: ${e.message}")
            null
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}
