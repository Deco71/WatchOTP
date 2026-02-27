package com.decoapps.wearotp.shared.crypto

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

fun createRSAKeys(): KeyPair? {
    return try {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.generateKeyPair()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        null
    }
}

fun rsaEncrypt(data: String, key: PublicKey): ByteArray {
    val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
}

fun rsaDecrypt(data: ByteArray, key: PrivateKey): String {
    val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, key)
    return String(cipher.doFinal(data), Charsets.UTF_8)
}
