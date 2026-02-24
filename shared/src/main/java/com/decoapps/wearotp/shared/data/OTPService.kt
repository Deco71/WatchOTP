package com.decoapps.wearotp.shared.data

import kotlinx.serialization.Serializable

@Serializable
data class OTPService(
    val id: String,
    val issuer: String? = null,
    val accountName: String? = null,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val interval: Int = 30,
)