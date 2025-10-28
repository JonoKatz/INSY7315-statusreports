package com.vcsd.leaselogic.landlord

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vcsd.leaselogic.databinding.ActivityEditPropertyBinding
import com.vcsd.leaselogic.models.Property
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vcsd.leaselogic.ui.SuccessAnimationActivity
import com.vcsd.leaselogic.R



class EditPropertyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPropertyBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var propertyId: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditPropertyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        propertyId = intent.getStringExtra("propertyId")

        // Toolbar setup
        setSupportActionBar(binding.toolbarEditProperty)
        binding.toolbarEditProperty.setNavigationOnClickListener { finish() }

        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProperty()

        binding.btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        binding.btnSaveChanges.setOnClickListener { saveChanges() }
    }

    private fun loadProperty() {
        db.collection("properties").document(propertyId!!)
            .get()
            .addOnSuccessListener { doc ->
                val property = doc.toObject(Property::class.java)
                if (property != null) {
                    binding.etName.setText(property.name)
                    binding.etPrice.setText(property.price.toString())
                    binding.etLocation.setText(property.location)
                    binding.etSize.setText(property.size)
                    binding.etDescription.setText(property.description)

                    Glide.with(this)
                        .load(property.imageUrl)
                        .into(binding.imgPropertyPreview)
                }
            }
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val size = binding.etSize.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // ✅ Validation checks
        when {
            name.isEmpty() -> {
                binding.etName.error = "Enter a property name"
                return
            }
            priceText.isEmpty() -> {
                binding.etPrice.error = "Enter a price"
                return
            }
            location.isEmpty() -> {
                binding.etLocation.error = "Enter a location"
                return
            }
            size.isEmpty() -> {
                binding.etSize.error = "Enter property size"
                return
            }
            description.isEmpty() -> {
                binding.etDescription.error = "Enter a description"
                return
            }
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.etPrice.error = "Enter a valid price"
            return
        }

        val docRef = db.collection("properties").document(propertyId!!)

        // ✅ Upload image if changed
        if (imageUri != null) {
            val fileRef = storage.reference.child("property_images/${propertyId}.jpg")
            fileRef.putFile(imageUri!!)
                .continueWithTask { fileRef.downloadUrl }
                .addOnSuccessListener { uri ->
                    updateFirestore(docRef, name, price, location, size, description, uri.toString())
                }
                .addOnFailureListener {
                    showErrorDialog("Image upload failed. Please try again.")
                }
        } else {
            updateFirestore(docRef, name, price, location, size, description, null)
        }
    }


    private fun updateFirestore(
        docRef: com.google.firebase.firestore.DocumentReference,
        name: String,
        price: Double,
        location: String,
        size: String,
        description: String,
        imageUrl: String?
    ) {
        val updates = hashMapOf(
            "name" to name,
            "price" to price,
            "location" to location,
            "size" to size,
            "description" to description
        )

        if (imageUrl != null) updates["imageUrl"] = imageUrl

        docRef.update(updates as Map<String, Any>)
            .addOnSuccessListener {
                val intent = Intent(this, SuccessAnimationActivity::class.java)
                intent.putExtra("message", "Property updated successfully!")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()


            }
            .addOnFailureListener {
                showErrorDialog("Failed to update property. Please try again.")
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.imgPropertyPreview.setImageURI(imageUri)
        }
    }

    private fun showSuccessDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Update")
            .setMessage("Save changes to this property?")
            .setPositiveButton("Save") { _, _ -> saveChanges() }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}
