package com.decoapps.wearotp.shared.crypto

import android.util.Log
import com.decoapps.wearotp.shared.data.OTPService
import kotlinx.serialization.json.Json
import java.io.File

class TokenFileManager {

    companion object {
        fun getTokensDirectory(baseDir: File): File {
            return File(baseDir, "tokens")
        }
    }

    private val json = Json { prettyPrint = false }

    fun saveEncryptedToken(directory: File, service: OTPService): Boolean {
        val cryptoManager = CryptoManager()

        return try {
            if (!directory.exists()) {
                directory.mkdirs()
            }

            //check if file with same id already exists
            if (File(directory, service.id).exists()) {
                Log.d("TokenFileManager", "File with id ${service.id} already exists, skipping save.")
                return true
            }

            val filename = service.id
            val tokenFile = File(directory, filename)

            // Serializza il servizio in JSON
            val jsonString = json.encodeToString(service)
            val jsonBytes = jsonString.toByteArray()

            // Crittografa e salva il file
            tokenFile.outputStream().use { outputStream ->
                cryptoManager.encrypt(jsonBytes, outputStream)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadEncryptedTokens(directory: File): List<OTPService> {
        val cryptoManager = CryptoManager()
        val files = try {
            if (!directory.exists()) {
                return emptyList()
            }

            //Log.d("Files in directory", directory.list()?.joinToString(", ") ?: "No files")

            directory.listFiles { file ->
                file.isFile
            }?.mapNotNull { tokenFile ->
                try {
                    // Decrittografa il file
                    val decryptedBytes = tokenFile.inputStream().use { inputStream ->
                        cryptoManager.decrypt(inputStream)
                    }

                    // Deserializza il JSON
                    val jsonString = String(decryptedBytes)
                    json.decodeFromString<OTPService>(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
        return files
    }

    fun deleteToken(directory: File, id: String): Boolean {
        val file = File(directory, id)
        return try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

