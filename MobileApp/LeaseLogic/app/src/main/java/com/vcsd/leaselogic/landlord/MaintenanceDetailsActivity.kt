package com.vcsd.leaselogic.landlord

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.databinding.ActivityMaintenanceDetailsBinding
import com.vcsd.leaselogic.models.MaintenanceRequest
import com.vcsd.leaselogic.models.Property
import com.vcsd.leaselogic.models.Tenant
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaintenanceDetailsBinding
    private lateinit var db: FirebaseFirestore
    private var requestId: String? = null
    private var currentRequest: MaintenanceRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        requestId = intent.getStringExtra("requestId")

        setSupportActionBar(binding.toolbarMaintenanceDetails)
        binding.toolbarMaintenanceDetails.setNavigationOnClickListener { finish() }

        if (requestId == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadMaintenanceDetails()

        binding.btnMarkInProgress.setOnClickListener {
            confirmStatusChange("In Progress")
        }

        binding.btnMarkResolved.setOnClickListener {
            confirmStatusChange("Resolved")
        }
    }

    private fun loadMaintenanceDetails() {
        db.collection("maintenanceRequests").document(requestId!!)
            .get()
            .addOnSuccessListener { doc ->
                val request = doc.toObject(MaintenanceRequest::class.java)
                if (request != null) {
                    currentRequest = request
                    displayDetails(request)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayDetails(req: MaintenanceRequest) {
        binding.txtTitle.text = req.title
        binding.txtDescription.text = req.description
        binding.txtPriority.text = "Priority: ${req.priority}"
        binding.txtStatus.text = "Status: ${req.status}"
        binding.txtDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(req.timestamp))

        // 🔹 Load tenant info
        db.collection("tenants").document(req.tenantId)
            .get()
            .addOnSuccessListener { tDoc ->
                val tenant = tDoc.toObject(Tenant::class.java)
                if (tenant != null) {
                    binding.txtTenantName.text = tenant.name
                    binding.txtTenantEmail.text = tenant.email
                    tenant.profilePic?.let { url ->
                        Picasso.get().load(url)
                            .placeholder(R.drawable.ic_user)
                            .into(binding.imgTenant)
                    }

// 🕓 Tenant duration
                    tenant.startDate?.let { moveIn ->
                        val months = getMonthDifference(moveIn, System.currentTimeMillis())
                        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(moveIn))
                        binding.txtTenantDuration.text =
                            "Tenant since: $formattedDate ($months month${if (months != 1) "s" else ""})"
                    } ?: run {
                        binding.txtTenantDuration.visibility = View.GONE
                    }


                }
            }

        // 🔹 Load property info
        db.collection("properties").document(req.propertyId ?: "")
            .get()
            .addOnSuccessListener { pDoc ->
                val property = pDoc.toObject(Property::class.java)
                if (property != null) {
                    binding.txtPropertyName.text = property.name
                    binding.txtPropertyLocation.text = property.location
                }
            }

        // 🔹 Only landlord can update status
        val isLandlord = FirebaseAuth.getInstance().currentUser?.uid == req.landlordId
        binding.layoutActions.visibility = if (isLandlord) View.VISIBLE else View.GONE
    }

    private fun confirmStatusChange(newStatus: String) {
        val reqId = currentRequest?.id ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Status")
            .setMessage("Mark this request as '$newStatus'?")
            .setPositiveButton("Yes") { d, _ ->
                db.collection("maintenanceRequests").document(reqId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show()
                        binding.txtStatus.text = "Status: $newStatus"
                    }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getMonthDifference(startMillis: Long, endMillis: Long): Int {
        val startCal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }

        val yearDiff = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        return yearDiff * 12 + monthDiff
    }

}
