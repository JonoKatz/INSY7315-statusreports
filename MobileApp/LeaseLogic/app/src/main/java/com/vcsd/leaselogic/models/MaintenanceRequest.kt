package com.vcsd.leaselogic.models

data class MaintenanceRequest(
    var id: String = "",
    val propertyId: String = "",
    val propertyName: String = "",
    val propertyImage: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val tenantProfilePic: String = "",
    val startDate: Long? = null,
    val title: String = "",
    val description: String = "",
    val priority: String = "",
    var status: String = "Pending",
    val images: List<String> = emptyList(),
    val landlordId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
