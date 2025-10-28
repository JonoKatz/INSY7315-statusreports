package com.vcsd.leaselogic.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcsd.leaselogic.databinding.ItemImagePreviewBinding

class ImagePreviewAdapter(
    private val imageUris: MutableList<Uri>,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.PreviewViewHolder>() {

    inner class PreviewViewHolder(val binding: ItemImagePreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        val uri = imageUris[position]
        holder.binding.imgPreviewThumb.setImageURI(uri)

        // Delete when X is tapped
        holder.binding.btnRemoveImage.setOnClickListener {
            onRemove(uri)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, imageUris.size)
        }
    }

    override fun getItemCount() = imageUris.size
}
