package com.vcsd.leaselogic.landlord

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vcsd.leaselogic.adapters.PropertyAdapter
import com.vcsd.leaselogic.databinding.ActivityLandlordPropertiesBinding
import com.vcsd.leaselogic.models.Property

class LandlordPropertiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordPropertiesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PropertyAdapter
    private var listener: ListenerRegistration? = null
    private val properties = mutableListOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandlordPropertiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView
        binding.recyclerProperties.layoutManager = LinearLayoutManager(this)
        adapter = PropertyAdapter(this, properties)
        binding.recyclerProperties.adapter = adapter

        // Setup Toolbar
        setSupportActionBar(binding.toolbarProperties)
        binding.toolbarProperties.setNavigationOnClickListener { finish() }

        // Add Property button (➕ FAB)
        binding.fabAddProperty.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }
    }

    // 🟢 LISTENER GOES HERE (starts and stops automatically)
    override fun onStart() {
        super.onStart()
        startListeningForProperties()
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun startListeningForProperties() {
        val landlordId = auth.currentUser?.uid ?: return

        // Firestore real-time updates
        listener = db.collection("properties")
            .whereEqualTo("landlordId", landlordId)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                properties.clear()
                for (doc in snapshots) {
                    val property = doc.toObject(Property::class.java).copy(id = doc.id)
                    properties.add(property)
                }

                // Update adapter
                adapter.notifyDataSetChanged()

                // Optional: empty message
                binding.txtEmpty.visibility =
                    if (properties.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
