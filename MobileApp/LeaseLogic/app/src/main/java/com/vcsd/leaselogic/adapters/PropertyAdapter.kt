package com.vcsd.leaselogic.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.databinding.ItemPropertyBinding
import com.vcsd.leaselogic.landlord.PropertyDetailsActivity
import com.vcsd.leaselogic.models.Property

class PropertyAdapter(
    private val context: Context,
    private val list: List<Property>
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    inner class PropertyViewHolder(val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = list[position]
        holder.binding.txtPropertyName.text = property.name
        holder.binding.txtPropertyPrice.text = "R${property.price}"
        holder.binding.txtPropertyLocation.text = property.location

        // Clear old images
        holder.binding.layoutImages.removeAllViews()

        // 🔹 Add multiple images horizontally
        val inflater = LayoutInflater.from(context)
        val imageSize = 180 // pixels
        val margin = 8

        property.imageUrls?.forEach { url ->
            val imageView = ImageView(context)
            val params = ViewGroup.MarginLayoutParams(imageSize, imageSize)
            params.setMargins(margin, 0, margin, 0)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Picasso.get().load(url).into(imageView)
            holder.binding.layoutImages.addView(imageView)
        }

        // Open details on click
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PropertyDetailsActivity::class.java)
            intent.putExtra("propertyId", property.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size
}
