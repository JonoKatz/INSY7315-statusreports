package com.vcsd.leaselogic.models

data class User(
    val name: String = "",
    val email: String = "",
    val role: String = "" // "Tenant" or "Landlord"
)
