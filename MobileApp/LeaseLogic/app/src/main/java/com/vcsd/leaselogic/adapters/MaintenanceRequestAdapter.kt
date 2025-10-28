package com.vcsd.leaselogic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vcsd.leaselogic.R
import com.vcsd.leaselogic.databinding.ItemMaintenanceRequestBinding
import com.vcsd.leaselogic.models.MaintenanceRequest
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceRequestAdapter(
    private val context: Context,
    private val list: List<MaintenanceRequest>,
    private val onStatusChange: (MaintenanceRequest, String) -> Unit
) : RecyclerView.Adapter<MaintenanceRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMaintenanceRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaintenanceRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = list[position]
        holder.binding.txtTitle.text = req.title
        holder.binding.txtDescription.text = req.description
        holder.binding.txtPriority.text = "Priority: ${req.priority}"
        holder.binding.txtStatus.text = "Status: ${req.status}"
        holder.binding.txtPropertyName.text = req.propertyName
        holder.binding.txtTenantName.text = req.tenantName

        // Tenant duration display
        req.startDate?.let {
            val start = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
            val now = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            holder.binding.txtTenantDuration.text = "Tenant since $start → $now"
        }

        // Load property and tenant images if available
        if (req.propertyImage.isNotEmpty()) {
            Picasso.get().load(req.propertyImage).placeholder(R.drawable.ic_home)
                .into(holder.binding.imgProperty)
        }

        // Status color
        val color = when (req.status.lowercase()) {
            "resolved" -> R.color.green
            "in progress" -> R.color.blue_700
            else -> R.color.red_accent
        }
        holder.binding.txtStatus.setTextColor(ContextCompat.getColor(context, color))

        // Button actions
        holder.binding.btnInProgress.setOnClickListener {
            onStatusChange(req, "In Progress")
        }
        holder.binding.btnResolved.setOnClickListener {
            onStatusChange(req, "Resolved")
        }
    }

    override fun getItemCount() = list.size
}
