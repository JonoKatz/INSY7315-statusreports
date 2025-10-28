package com.vcsd.leaselogic.models

data class Tenant(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val profilePic: String? = null,
    val propertyId: String? = null,
    val propertyName: String? = null, // ✅ Added for display in ManageTenantsActivity
    val landlordId: String = "",
    var status: String = "Pending",   // ✅ Added for approval/rejection workflow
    val startDate: Long? = null       // (optional: for “tenant since” display later)
)
