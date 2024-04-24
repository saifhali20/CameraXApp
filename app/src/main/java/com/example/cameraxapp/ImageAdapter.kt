package com.example.cameraxapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cameraxapp.databinding.ItemImageBinding

class ImageAdapter(private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    // list to hold image URI's
    private val images = mutableListOf<String>()

    class ImageViewHolder(binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // load image into imageview using glide
        Glide.with(context).load(images[position]).into(holder.imageView)
        // setting click listener to open the full image
        holder.imageView.setOnClickListener {
            val intent = Intent(context, FullImageActivity::class.java)
            intent.putExtra("image_uri", images[position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = images.size

    fun addItem(uri: String) {
        images.add(uri)
        notifyItemInserted(images.size - 1)
    }
}
