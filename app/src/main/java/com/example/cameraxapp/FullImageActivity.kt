package com.example.cameraxapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        // getting ImageView and image URI from intent

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val imageUri = intent.getStringExtra("image_uri")

        // load image into imageview using glide

        Glide.with(this)
            .load(imageUri)
            .into(imageView)

        // click listener for home button
        findViewById<Button>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
