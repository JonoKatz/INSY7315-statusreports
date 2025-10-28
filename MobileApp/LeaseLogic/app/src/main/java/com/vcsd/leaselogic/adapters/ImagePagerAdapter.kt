package com.vcsd.leaselogic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.R

class ImagePagerAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(context)
            .inflate(R.layout.item_image_pager, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Picasso.get()
            .load(imageUrls[position])
            .placeholder(R.drawable.ic_home)
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.imageView.setOnClickListener { onImageClick(position) }
    }

    override fun getItemCount() = imageUrls.size
}
