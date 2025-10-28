package com.vcsd.leaselogic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.R

class FullScreenImageAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    private val onImageTap: () -> Unit
) : RecyclerView.Adapter<FullScreenImageAdapter.FullScreenViewHolder>() {

    inner class FullScreenViewHolder(val photoView: PhotoView) :
        RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullScreenViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_fullscreen_photo, parent, false)
        val photoView = view.findViewById<PhotoView>(R.id.photoView)
        return FullScreenViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: FullScreenViewHolder, position: Int) {
        val url = imageUrls[position]

        Picasso.get()
            .load(url)
            .placeholder(R.drawable.ic_home)
            .fit()
            .centerInside()
            .into(holder.photoView)

        // Tap anywhere to close dialog
        holder.photoView.setOnPhotoTapListener { _, _, _ -> onImageTap() }
    }

    override fun getItemCount() = imageUrls.size
}
