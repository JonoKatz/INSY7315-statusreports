package com.vcsd.leaselogic.landlord

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.databinding.ActivityManagePropertiesBinding
import com.vcsd.leaselogic.databinding.ItemPropertyBinding
import com.vcsd.leaselogic.models.Property
import com.vcsd.leaselogic.R



class ManagePropertiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagePropertiesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val allProperties = mutableListOf<Property>()
    private val filteredList = mutableListOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePropertiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.recyclerProperties.layoutManager = LinearLayoutManager(this)
        binding.btnBack.setOnClickListener { finish() }

        setupSortDropdown()
        setupSearchBar()

        binding.btnAddProperty.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        loadProperties()
    }

    /** 🔹 Setup sort dropdown */
    private fun setupSortDropdown() {
        val options = listOf("Newest First", "Oldest First", "Lowest Price", "Highest Price")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        binding.dropdownSort.setAdapter(adapter)

        binding.dropdownSort.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> filteredList.sortByDescending { it.dateCreated }
                1 -> filteredList.sortBy { it.dateCreated }
                2 -> filteredList.sortBy { it.price }
                3 -> filteredList.sortByDescending { it.price }
            }
            binding.recyclerProperties.adapter?.notifyDataSetChanged()
        }
    }

    /** 🔹 Setup search filtering */
    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProperties(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /** 🔹 Load properties */
    private fun loadProperties() {
        val landlordId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        db.collection("properties")
            .whereEqualTo("landlordId", landlordId)
            .get()
            .addOnSuccessListener { snapshot ->
                allProperties.clear()
                for (doc in snapshot) {
                    val property = doc.toObject(Property::class.java)
                    property.id = doc.id
                    allProperties.add(property)
                }

                filteredList.clear()
                filteredList.addAll(allProperties)

                binding.progressBar.visibility = View.GONE
                if (allProperties.isEmpty()) {
                    binding.txtNoProperties.visibility = View.VISIBLE
                } else {
                    binding.txtNoProperties.visibility = View.GONE
                    binding.recyclerProperties.adapter = PropertyAdapter()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.txtNoProperties.visibility = View.VISIBLE
                binding.txtNoProperties.text = "Error loading properties"
            }
    }

    /** 🔹 Filter by name/location/price */
    private fun filterProperties(query: String) {
        filteredList.clear()
        val lower = query.lowercase()

        filteredList.addAll(
            allProperties.filter {
                it.name.lowercase().contains(lower)
                        || it.location.lowercase().contains(lower)
                        || it.price.toString().contains(lower)
            }
        )

        if (filteredList.isEmpty()) {
            binding.txtNoProperties.visibility = View.VISIBLE
            binding.recyclerProperties.visibility = View.GONE
        } else {
            binding.txtNoProperties.visibility = View.GONE
            binding.recyclerProperties.visibility = View.VISIBLE
            binding.recyclerProperties.adapter = PropertyAdapter()
        }
    }

    /** 🔹 Adapter with image preview modal */
    inner class PropertyAdapter :
        RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

        inner class PropertyViewHolder(val itemBinding: ItemPropertyBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
            val b = ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PropertyViewHolder(b)
        }

        override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
            val property = filteredList[position]
            holder.itemBinding.txtPropertyName.text = property.name
            holder.itemBinding.txtPropertyLocation.text = property.location
            holder.itemBinding.txtPropertyPrice.text = "R${property.price}"

            val firstImage = property.imageUrls?.firstOrNull()
            if (firstImage != null) {
                Picasso.get().load(firstImage).into(holder.itemBinding.imgProperty)
                holder.itemBinding.imgProperty.setOnClickListener {
                    showImageDialog(property.imageUrls ?: emptyList())
                }
            }

            holder.itemBinding.root.setOnClickListener {
                val intent = Intent(this@ManagePropertiesActivity, PropertyDetailsActivity::class.java)
                intent.putExtra("propertyId", property.id)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }

        }

        override fun getItemCount() = filteredList.size
    }

    /** 🔹 Show fullscreen image viewer with animation */
    private fun showImageDialog(urls: List<String>) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ManagePropertiesActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = object : RecyclerView.Adapter<ImageHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
                    val img = ImageView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    return ImageHolder(img)
                }

                override fun onBindViewHolder(holder: ImageHolder, position: Int) {
                    Picasso.get().load(urls[position])
                        .placeholder(R.drawable.ic_home)
                        .fit()
                        .centerCrop()
                        .into(holder.img)
                }

                override fun getItemCount() = urls.size
            }
        }

        dialog.setContentView(recycler)

        // 🔹 Apply fade + slide animation when opening
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        dialog.window?.decorView?.startAnimation(fadeIn)
        dialog.window?.decorView?.startAnimation(slideIn)

        dialog.show()

        // 🔹 Add closing animation when tapping anywhere
        recycler.setOnClickListener {
            val fadeOut = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out)
            val slideOut = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
            dialog.window?.decorView?.startAnimation(fadeOut)
            dialog.window?.decorView?.startAnimation(slideOut)
            recycler.postDelayed({ dialog.dismiss() }, 350)
        }
    }


    inner class ImageHolder(val img: ImageView) : RecyclerView.ViewHolder(img)
}
