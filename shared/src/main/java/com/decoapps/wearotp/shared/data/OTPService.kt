package com.decoapps.wearotp.shared.data

import kotlinx.serialization.Serializable

@Serializable
data class OTPService(
    val id: String,
    val name: String? = null,
    val token: String? = null
)