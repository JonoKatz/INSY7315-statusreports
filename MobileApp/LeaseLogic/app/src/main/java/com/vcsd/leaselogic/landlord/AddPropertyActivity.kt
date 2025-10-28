package com.vcsd.leaselogic.landlord

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.adapters.ImagePreviewAdapter
import com.vcsd.leaselogic.databinding.ActivityAddPropertyBinding
import java.io.ByteArrayOutputStream
import java.util.*

class AddPropertyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPropertyBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private val imageUris = mutableListOf<Uri>()

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 1001
        private const val REQUEST_IMAGE_CAMERA = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPropertyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbarAddProperty)
        binding.toolbarAddProperty.setNavigationOnClickListener { finish() }

        binding.btnSelectImage.setOnClickListener { showImagePickerOptions() }
        binding.btnAddProperty.setOnClickListener { saveProperty() }
        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    /** 🔹 Ask user: Take photo or choose from gallery */
    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Add Property Photos")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> openCamera()
                    "Choose from Gallery" -> openGallery()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_IMAGE_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA)
    }

    /** 🔹 Handle camera/gallery results */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_IMAGE_GALLERY -> {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        imageUris.add(uri)
                    }
                } else if (data.data != null) {
                    imageUris.add(data.data!!)
                }
            }

            REQUEST_IMAGE_CAMERA -> {
                val bitmap = data.extras?.get("data") as? Bitmap ?: return
                val uri = getImageUriFromBitmap(bitmap)
                uri?.let { imageUris.add(it) }
            }
        }

        updatePreview()
    }

    /** 🔹 Convert camera bitmap to Uri so we can upload it */
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Property_${UUID.randomUUID()}",
            null
        )
        return Uri.parse(path)
    }

    /** 🔹 Show image preview & allow removal with smooth horizontal snapping */
    private fun updatePreview() {
        if (imageUris.isNotEmpty()) {
            binding.imgPreview.setImageURI(imageUris.last())
            binding.recyclerImagePreview.apply {
                layoutManager = LinearLayoutManager(this@AddPropertyActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = ImagePreviewAdapter(imageUris) { uri ->
                    imageUris.remove(uri)
                    updatePreview()
                }
                visibility = View.VISIBLE

                // ✅ Enable smooth snapping (like carousel)
                val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
                if (onFlingListener == null) { // prevent duplicate attachments
                    snapHelper.attachToRecyclerView(this)
                }
            }

            Toast.makeText(this, "${imageUris.size} image(s) selected", Toast.LENGTH_SHORT).show()
        } else {
            binding.imgPreview.setImageResource(R.drawable.ic_home)
            binding.recyclerImagePreview.visibility = View.GONE
        }
    }


    /** 🔹 Validate & upload all images */
    private fun saveProperty() {
        val name = binding.etName.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val size = binding.etSize.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        val price = priceText.toDoubleOrNull()

        if (name.isEmpty() || price == null || price <= 0 || location.isEmpty() ||
            size.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnAddProperty.isEnabled = false
        uploadImagesAndSaveProperty(name, price, location, size, description)
    }

    /** 🔹 Upload all selected images to Firebase */
    /** 🔹 Upload all selected images to Firebase with progress dialog */
    private fun uploadImagesAndSaveProperty(
        name: String,
        price: Double,
        location: String,
        size: String,
        description: String
    ) {
        val landlordId = auth.currentUser?.uid ?: return
        val propertyId = UUID.randomUUID().toString()

        // ✅ Show loading dialog
        val progressDialog = AlertDialog.Builder(this)
            .setView(layoutInflater.inflate(R.layout.dialog_loading, null))
            .setCancelable(false)
            .create()
        progressDialog.show()

        val uploadTasks = imageUris.map { uri ->
            val ref = storage.reference.child("property_images/$propertyId/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).continueWithTask { ref.downloadUrl }
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess<Uri>(uploadTasks)
            .addOnSuccessListener { uris ->
                val urls = uris.map { it.toString() }
                val propertyData = hashMapOf(
                    "id" to propertyId,
                    "landlordId" to landlordId,
                    "name" to name,
                    "price" to price,
                    "location" to location,
                    "size" to size,
                    "description" to description,
                    "imageUrls" to urls,
                    "isRented" to false,
                    "dateCreated" to System.currentTimeMillis()
                )

                db.collection("properties").document(propertyId)
                    .set(propertyData)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Property added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error saving property.", Toast.LENGTH_SHORT).show()
                        binding.btnAddProperty.isEnabled = true
                    }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Error uploading images.", Toast.LENGTH_SHORT).show()
                binding.btnAddProperty.isEnabled = true
            }
    }

}
