package com.vcsd.leaselogic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ItemTenantRequestBinding
import com.vcsd.leaselogic.models.TenantRequest

class TenantRequestAdapter(
    private val context: Context,
    private val list: MutableList<TenantRequest>
) : RecyclerView.Adapter<TenantRequestAdapter.RequestViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class RequestViewHolder(val binding: ItemTenantRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemTenantRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = list[position]
        holder.binding.txtTenantName.text = request.tenantName
        holder.binding.txtPropertyName.text = "Property: ${request.propertyName}"
        holder.binding.txtStatus.text = request.status

        holder.binding.btnApprove.setOnClickListener { updateStatus(request, "approved") }
        holder.binding.btnReject.setOnClickListener { updateStatus(request, "rejected") }
    }

    override fun getItemCount() = list.size

    private fun updateStatus(request: TenantRequest, newStatus: String) {
        val statusText = if (newStatus == "approved") "Approve" else "Reject"

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("$statusText Request")
            .setMessage("Are you sure you want to $statusText this tenant request?")
            .setPositiveButton(statusText) { d, _ ->
                d.dismiss()
                performStatusUpdate(request, newStatus)
            }
            .setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            .create()

        dialog.show()

        // 🟦 Style the buttons after showing the dialog
        val blue = androidx.core.content.ContextCompat.getColor(context, com.vcsd.leaselogic.R.color.blue_500)
        val red = androidx.core.content.ContextCompat.getColor(context, com.vcsd.leaselogic.R.color.red_accent)

        if (newStatus == "approved") {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(blue)
        } else {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(red)
        }

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(android.graphics.Color.GRAY)
    }



    private fun performStatusUpdate(request: TenantRequest, newStatus: String) {
        val requestsRef = db.collection("rentalRequests").document(request.id!!)
        val propertyRef = db.collection("properties").document(request.propertyId)

        db.runTransaction { transaction ->
            // 🔹 1. Update request status
            transaction.update(requestsRef, "status", newStatus)

            if (newStatus == "approved") {
                // 🔹 2. Link tenant to property
                val updates = mapOf(
                    "isRented" to true,
                    "tenantId" to request.tenantId,
                    "tenantName" to request.tenantName
                )
                transaction.update(propertyRef, updates)
            } else if (newStatus == "rejected") {
                // 🔹 3. Reset property availability
                transaction.update(propertyRef, "isRented", false)
                transaction.update(propertyRef, "tenantId", "")
                transaction.update(propertyRef, "tenantName", "")
            }
        }.addOnSuccessListener {
            android.widget.Toast.makeText(context, "Request $newStatus", android.widget.Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            android.widget.Toast.makeText(context, "Error updating request", android.widget.Toast.LENGTH_SHORT).show()
        }
    }


}
