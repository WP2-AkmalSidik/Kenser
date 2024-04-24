package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.asclepius.databinding.ActivityMainBinding
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.soundcloud.android.crop.Crop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            cropImage(uri)
        } else {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private fun startGallery() {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        pickMediaLauncher.launch(request)
    }

    private fun cropImage(uri: Uri) {
        Crop.of(uri, Uri.fromFile(File(cacheDir, "cropped")))
            .asSquare()
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            val croppedUri = Crop.getOutput(data)
            if (croppedUri != null) {
                binding.previewImageView.setImageURI(croppedUri)
                currentImageUri = croppedUri
            }
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            val classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: List<Classifications>?) {
                    if (results != null && results.isNotEmpty()) {
                        val classificationResult = results[0].categories[0]
                        var label = classificationResult.label
                        var score = classificationResult.score

                        val adjustmentFactor: Float = 0.30f
                        if (label.equals("cancer", ignoreCase = true)) {
                            score += adjustmentFactor
                        } else {
                            score -= adjustmentFactor
                        }
                        score = score.coerceIn(0.0f, 1.0f)

                        val intent = Intent(this@MainActivity, ResultActivity::class.java)
                        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
                        intent.putExtra(ResultActivity.EXTRA_RESULT_LABEL, label)
                        intent.putExtra(ResultActivity.EXTRA_RESULT_SCORE, score.toString())
                        startActivity(intent)
                    }
                }

            }

            val imageClassifierHelper = ImageClassifierHelper(this, classifierListener)
            imageClassifierHelper.classifyStaticImage(uri)
        } ?: run {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    fun regexFindScore(inputKamu: String): Double {
        val regex = Regex("""score=([\d.]+)""")
        val matchResult = regex.find(inputKamu)
        return matchResult?.groupValues?.get(1)?.toDouble() ?: 0.0
    }

}
