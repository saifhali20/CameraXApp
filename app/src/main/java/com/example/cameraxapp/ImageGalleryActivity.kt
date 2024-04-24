package com.example.cameraxapp

import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraxapp.databinding.ActivityImageGalleryBinding

class ImageGalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageGalleryBinding
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadImages()
    }

    private fun setupRecyclerView() {
        adapter = ImageAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

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
