package com.decoapps.wearotp.shared.cryptoPreferences

import androidx.datastore.core.Serializer
import com.decoapps.wearotp.shared.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class CryptoPreferences(
    val privateKeyBase64: String? = null,
    val publicKeyBase64: String? = null
)

object CryptoPreferencesSerializer: Serializer<CryptoPreferences> {
    override val defaultValue: CryptoPreferences
        get() = CryptoPreferences()

    override suspend fun readFrom(input: InputStream): CryptoPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        val encryptedBase64BytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = CryptoManager().decrypt(encryptedBase64BytesDecoded)
        val decodedJsonString = decryptedBytes.decodeToString()
        return Json.decodeFromString(decodedJsonString)
    }

    override suspend fun writeTo(
        t: CryptoPreferences,
        output: OutputStream
    ) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = CryptoManager().encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }

}