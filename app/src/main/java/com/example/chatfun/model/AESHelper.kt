package com.example.chatfun.model

import android.util.Base64
import okio.Utf8
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class AESHelper {

    private val pswdIterations = 10
    private val keySize = 256
    private val cypherInstance = "AES/CBC/PKCS5Padding"
    private val secretKeyInstance = "PBKDF2WithHmacSHA1"
    private val plainText = "sampleText"
    private val AESSalt = "exampleSalt"
    private val initializationVector = "8119745113154120"


    fun encrypt(textToEncrypt: String): String? {
        val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
        val cipher = Cipher.getInstance(cypherInstance)
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(initializationVector.toByteArray()))
        val encrypted = cipher.doFinal(textToEncrypt.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(textToDecrypt: String?): String? {
        val encryted_bytes: ByteArray = Base64.decode(textToDecrypt, Base64.DEFAULT)
        val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
        val cipher = Cipher.getInstance(cypherInstance)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(initializationVector.toByteArray()))
        val decrypted = cipher.doFinal(encryted_bytes)
        return String(decrypted)
    }

    private fun getRaw(plainText: String, salt: String): ByteArray? {
        try {

            val factory: SecretKeyFactory = SecretKeyFactory.getInstance(secretKeyInstance)
            val spec: KeySpec = PBEKeySpec(
                plainText.toCharArray(),
                salt.toByteArray(),
                pswdIterations,
                keySize
            )
            return factory.generateSecret(spec).getEncoded()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }
}