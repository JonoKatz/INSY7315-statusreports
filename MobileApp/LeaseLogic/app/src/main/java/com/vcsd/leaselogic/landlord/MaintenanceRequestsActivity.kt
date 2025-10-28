package com.vcsd.leaselogic.landlord

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.adapters.MaintenanceRequestAdapter
import com.vcsd.leaselogic.databinding.ActivityMaintenanceRequestsBinding
import com.vcsd.leaselogic.models.MaintenanceRequest

class MaintenanceRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaintenanceRequestsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MaintenanceRequestAdapter
    private val allRequests = mutableListOf<MaintenanceRequest>()
    private val filteredRequests = mutableListOf<MaintenanceRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setSupportActionBar(binding.toolbarMaintenance)

        setupFilterDropdown()
        setupRecycler()
        setupBackButton()
        loadRequests()
    }

    /** 🔹 Setup dropdown filter */
    private fun setupFilterDropdown() {
        val options = listOf("All", "Pending", "In Progress", "Resolved")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        binding.dropdownFilter.setAdapter(filterAdapter)

        // default selection
        binding.dropdownFilter.setText("All", false)

        // Ensure dropdown always opens on click
        binding.dropdownFilter.setOnClickListener {
            binding.dropdownFilter.showDropDown()
        }

        // Apply filtering when an item is selected
        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
            filterRequests(options[position])
        }
    }


    /** 🔹 Setup RecyclerView */
    private fun setupRecycler() {
        adapter = MaintenanceRequestAdapter(this, filteredRequests) { request, newStatus ->
            confirmStatusChange(request.id, newStatus)
        }
        binding.recyclerMaintenance.layoutManager = LinearLayoutManager(this)
        binding.recyclerMaintenance.adapter = adapter
    }

    /** 🔹 Setup back button */
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    /** 🔹 Load maintenance requests from Firestore */
    private fun loadRequests() {
        val landlordId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("maintenanceRequests")
            .whereEqualTo("landlordId", landlordId)
            .addSnapshotListener { snapshot, _ ->
                allRequests.clear()
                snapshot?.forEach { doc ->
                    val req = doc.toObject(MaintenanceRequest::class.java)
                    req.id = doc.id
                    allRequests.add(req)
                }
                filterRequests(binding.dropdownFilter.text.toString())
            }
    }

    /** 🔹 Filter displayed list */
    private fun filterRequests(status: String) {
        filteredRequests.clear()
        filteredRequests.addAll(
            if (status == "All") allRequests
            else allRequests.filter { it.status.equals(status, ignoreCase = true) }
        )

        binding.txtEmpty.visibility =
            if (filteredRequests.isEmpty()) View.VISIBLE else View.GONE

        adapter.notifyDataSetChanged()
    }

    /** 🔹 Confirm status change */
    private fun confirmStatusChange(requestId: String, newStatus: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Status Change")
            .setMessage("Mark this request as '$newStatus'?")
            .setPositiveButton("Yes") { d, _ ->
                db.collection("maintenanceRequests")
                    .document(requestId)
                    .update("status", newStatus)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
