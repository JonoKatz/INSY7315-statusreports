package com.vcsd.leaselogic.tenant

import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ActivityTenantMaintenanceRequestBinding
import com.vcsd.leaselogic.models.MaintenanceRequest

class TenantMaintenanceRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantMaintenanceRequestBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantMaintenanceRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbarTenantMaintenance)
        binding.toolbarTenantMaintenance.setNavigationOnClickListener { finish() }

        setupPriorityDropdown()
        setupSubmitButton()
    }

    /** Priority Dropdown */
    private fun setupPriorityDropdown() {
        val priorities = listOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, priorities)

        val dropdown = binding.priorityDropdown
        dropdown.inputType = InputType.TYPE_NULL // Prevent keyboard
        dropdown.setAdapter(adapter)
        dropdown.setText("Medium", false) // Default
        dropdown.setOnClickListener { dropdown.showDropDown() }
    }

    /** Submit Button Logic */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val prio = binding.priorityDropdown.text.toString().trim()
            val currentUser = auth.currentUser

            if (title.isEmpty() || desc.isEmpty() || prio.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tenantId = currentUser.uid

            // Fetch tenant name from Firestore
            db.collection("users").document(tenantId).get()
                .addOnSuccessListener { doc ->
                    val tenantName = doc.getString("name") ?: "Unknown"

                    val request = MaintenanceRequest(
                        title = title,
                        description = desc,
                        priority = prio,
                        tenantId = tenantId,
                        tenantName = tenantName
                    )

                    db.collection("maintenance_requests").add(request)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }
}
