package com.vcsd.leaselogic.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ActivityManageTenantsBinding
import com.vcsd.leaselogic.databinding.ItemTenantBinding
import com.vcsd.leaselogic.models.Tenant
import com.vcsd.leaselogic.R

class ManageTenantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageTenantsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val tenantsList = mutableListOf<Tenant>()
    private val filteredList = mutableListOf<Tenant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageTenantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.recyclerTenants.layoutManager = LinearLayoutManager(this)
        binding.btnBack.setOnClickListener { finish() }

        setupFilterDropdown()
        loadTenants()
    }

    /** 🔹 Dropdown filter setup */
    private fun setupFilterDropdown() {
        val filterOptions = listOf("All", "Active", "Pending", "Rejected")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filterOptions)
        binding.dropdownFilter.setAdapter(adapter)

        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
            val selected = filterOptions[position]
            filterTenants(selected)
        }
    }

    /** 🔹 Load tenants from Firestore */
    private fun loadTenants() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerTenants.visibility = View.GONE
        binding.txtNoTenants.visibility = View.GONE

        db.collection("tenants")
            .whereEqualTo("landlordId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                tenantsList.clear()
                for (doc in snapshot) {
                    val tenant = doc.toObject(Tenant::class.java)
                    tenantsList.add(tenant)
                }

                binding.progressBar.visibility = View.GONE

                if (tenantsList.isEmpty()) {
                    binding.txtNoTenants.visibility = View.VISIBLE
                } else {
                    filteredList.clear()
                    filteredList.addAll(tenantsList)
                    binding.recyclerTenants.visibility = View.VISIBLE
                    binding.recyclerTenants.adapter = TenantAdapter()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.txtNoTenants.visibility = View.VISIBLE
                binding.txtNoTenants.text = "Failed to load tenants"
            }
    }

    /** 🔹 Filter tenant list by status */
    private fun filterTenants(status: String) {
        filteredList.clear()
        filteredList.addAll(
            if (status == "All") tenantsList
            else tenantsList.filter { it.status.equals(status, ignoreCase = true) }
        )

        if (filteredList.isEmpty()) {
            binding.txtNoTenants.visibility = View.VISIBLE
            binding.recyclerTenants.visibility = View.GONE
        } else {
            binding.txtNoTenants.visibility = View.GONE
            binding.recyclerTenants.visibility = View.VISIBLE
            binding.recyclerTenants.adapter = TenantAdapter()
        }
    }

    /** 🔹 RecyclerView Adapter */
    inner class TenantAdapter : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

        inner class TenantViewHolder(val itemBinding: ItemTenantBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
            val b = ItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TenantViewHolder(b)
        }

        override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
            val tenant = filteredList[position]

            holder.itemBinding.txtTenantName.text = tenant.name
            holder.itemBinding.txtPropertyName.text = "Property: ${tenant.propertyName ?: "N/A"}"
            holder.itemBinding.txtStatus.text = tenant.status ?: "Pending"

            // 🎨 Status color
            val statusColor = when (tenant.status?.lowercase()) {
                "active", "approved" -> ContextCompat.getColor(this@ManageTenantsActivity, R.color.green)
                "rejected" -> ContextCompat.getColor(this@ManageTenantsActivity, R.color.red_accent)
                else -> ContextCompat.getColor(this@ManageTenantsActivity, R.color.blue_500)
            }
            holder.itemBinding.txtStatus.setTextColor(statusColor)

            // 🔹 Approve button
            holder.itemBinding.btnApprove.setOnClickListener {
                showConfirmationDialog(
                    title = "Approve Tenant",
                    message = "Approve ${tenant.name} for ${tenant.propertyName}?",
                    confirmText = "Approve"
                ) {
                    updateTenantStatus(tenant.id ?: "", "Approved")
                }
            }

            // 🔹 Reject button
            holder.itemBinding.btnReject.setOnClickListener {
                showConfirmationDialog(
                    title = "Reject Tenant",
                    message = "Reject ${tenant.name} for ${tenant.propertyName}?",
                    confirmText = "Reject"
                ) {
                    updateTenantStatus(tenant.id ?: "", "Rejected")
                }
            }
        }

        override fun getItemCount() = filteredList.size
    }

    /** 🔹 Update tenant status in Firestore */
    private fun updateTenantStatus(tenantId: String, newStatus: String) {
        if (tenantId.isEmpty()) {
            Toast.makeText(this, "Invalid tenant ID", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("tenants").document(tenantId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Tenant $newStatus", Toast.LENGTH_SHORT).show()
                loadTenants()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    /** 🔹 Reusable confirmation dialog */
    private fun showConfirmationDialog(
        title: String,
        message: String,
        confirmText: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmText) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
