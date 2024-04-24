package com.example.cameraxapp

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraxapp.databinding.ActivityImageGalleryBinding

class ImageGalleryActivity : AppCompatActivity() {
    // view binding instance
    private lateinit var binding: ActivityImageGalleryBinding
    // adapter for recyclerview
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize viewbinding
        binding = ActivityImageGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setting up recycler view

        setupRecyclerView()
        // loading images
        loadImages()

        // home button click listener
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // function to set up recylcerview

    private fun setupRecyclerView() {
        adapter = ImageAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // function to load images from database
    private fun loadImages() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "images", arrayOf("_id", "image_uri"), null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            val uri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
            adapter.addItem(uri)
        }
        cursor.close()
        db.close()
    }
}
