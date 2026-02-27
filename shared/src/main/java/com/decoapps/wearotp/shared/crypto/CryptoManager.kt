package com.decoapps.wearotp.shared.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {

    private val keystoreAlias = "secret"

    private val keystore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val cipher = Cipher.getInstance(TRANSFORMATION)

    private fun getEncryptCipher() : Cipher {
        cipher.init(Cipher.ENCRYPT_MODE, getKey(keystoreAlias))
        return cipher
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        cipher.init(Cipher.DECRYPT_MODE, getKey(keystoreAlias), IvParameterSpec(iv))
        return cipher
    }

    private fun getKey(alias: String): SecretKey {
        return (keystore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: createKey(alias).also { key ->
                keystore.setEntry(alias, KeyStore.SecretKeyEntry(key), null)
            }
    }

    private fun createKey(alias: String) : SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val encryptCipher = getEncryptCipher()
        val encryptedBytes = encryptCipher.doFinal(bytes)
        outputStream.use {
            it.write(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    fun decrypt(inputStream: InputStream) : ByteArray {
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            getDecryptCipherForIv(iv).doFinal(encryptedBytes)
        }
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val encryptCipher = getEncryptCipher()
        val iv = encryptCipher.iv
        val encrypted = encryptCipher.doFinal(bytes)
        return iv + encrypted
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        val iv = bytes.copyOfRange(0, cipher.blockSize)
        val data = bytes.copyOfRange(cipher.blockSize, bytes.size)
        return getDecryptCipherForIv(iv).doFinal(data)
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}