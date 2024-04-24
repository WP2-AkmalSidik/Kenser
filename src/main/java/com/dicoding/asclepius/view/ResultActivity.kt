package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding
import kotlin.math.roundToInt

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let { Uri.parse(it) }
        imageUri?.let { binding.resultImage.setImageURI(it) }

        val label = intent.getStringExtra(EXTRA_RESULT_LABEL)
        val scoreString = intent.getStringExtra(EXTRA_RESULT_SCORE)

        if (label != null && scoreString != null) {
            val score = scoreString.toFloat()
            val percentageScore = score * 100

            val roundedPercentageScore = percentageScore.roundToInt()

            binding.resultText.text = "Hasil Klasifikasi: $label\nSkor: $roundedPercentageScore%"
        } else {
            binding.resultText.text = "Hasil klasifikasi tidak tersedia."
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_LABEL = "extra_result_label"
        const val EXTRA_RESULT_SCORE = "extra_result_score"
    }
}