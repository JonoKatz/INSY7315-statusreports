package com.vcsd.leaselogic.models

data class TenantRequest(
    var id: String? = null,
    val propertyId: String = "",
    val landlordId: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val propertyName: String = "",
    val status: String = "pending"

)
