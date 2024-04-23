package com.example.cameraxapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cameraxapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openCameraButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.viewImagesButton.setOnClickListener {
            startActivity(Intent(this, ImageGalleryActivity::class.java))
        }
    }
}
