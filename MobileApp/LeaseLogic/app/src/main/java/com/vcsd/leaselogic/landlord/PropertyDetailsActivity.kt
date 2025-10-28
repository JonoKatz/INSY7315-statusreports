package com.vcsd.leaselogic.landlord

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.adapters.FullScreenImageAdapter
import com.vcsd.leaselogic.adapters.ImagePagerAdapter
import com.vcsd.leaselogic.databinding.ActivityPropertyDetailsBinding
import com.vcsd.leaselogic.models.Property

class PropertyDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPropertyDetailsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var propertyId: String? = null
    private var currentProperty: Property? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        propertyId = intent.getStringExtra("propertyId")

        setSupportActionBar(binding.toolbarPropertyDetails)
        binding.toolbarPropertyDetails.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
        }

        if (propertyId == null) {
            finish()
            return
        }

        loadProperty()

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditPropertyActivity::class.java)
            intent.putExtra("propertyId", propertyId)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener { deleteProperty() }
    }

    private fun loadProperty() {
        db.collection("properties").document(propertyId!!).get()
            .addOnSuccessListener { doc ->
                val property = doc.toObject(Property::class.java)
                if (property != null) {
                    currentProperty = property
                    displayPropertyDetails(property)
                }
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error loading property", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun displayPropertyDetails(property: Property) {
        binding.txtName.text = property.name
        binding.txtPrice.text = "R ${property.price} / month"
        binding.txtSize.text = "Size: ${property.size}"
        binding.txtLocation.text = "Location: ${property.location}"
        binding.txtDescription.text = property.description

        val imageUrls = property.imageUrls
        if (imageUrls.isNotEmpty()) {
            val adapter = ImagePagerAdapter(this, imageUrls) { position ->
                showFullScreenGallery(imageUrls, position)
            }
            binding.viewPagerImages.adapter = adapter
            findViewById<WormDotsIndicator>(R.id.dotsIndicator).attachTo(binding.viewPagerImages)
        }

        // 🔹 Only landlord can edit or delete
        val isLandlord = property.landlordId == auth.currentUser?.uid
        binding.btnEdit.visibility =
            if (isLandlord) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnDelete.visibility =
            if (isLandlord) android.view.View.VISIBLE else android.view.View.GONE
    }

    /** 🔹 Swipeable fullscreen image viewer with fade animation + zoom */
    private fun showFullScreenGallery(images: List<String>, startPosition: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_fullscreen_gallery)

        // Apply fade-in animation when opening
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        dialog.window?.decorView?.startAnimation(fadeIn)

        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewPagerFullScreen)
        val adapter = FullScreenImageAdapter(this, images) {
            // Fade-out animation before dismissing
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            dialog.window?.decorView?.startAnimation(fadeOut)
            viewPager.postDelayed({ dialog.dismiss() }, 350)
        }

        viewPager.adapter = adapter
        viewPager.setCurrentItem(startPosition, false)
        dialog.show()
    }

    private fun deleteProperty() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this property?")
            .setPositiveButton("Delete") { d, _ ->
                db.collection("properties").document(propertyId!!).delete()
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Property deleted", Snackbar.LENGTH_SHORT)
                            .setAction("OK") { finish() }
                            .show()
                    }
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}
