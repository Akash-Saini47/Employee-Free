package com.salarywise.app.domain.model

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/** Device-bound PIN verifier backed by the Android Keystore. */
object PinSecurity {
    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "salarywise_pin_hmac"
    private const val TRANSFORMATION = "HmacSHA256"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).setDigests(KeyProperties.DIGEST_SHA256).build()
        )
        return generator.generateKey()
    }

    fun hashPin(pin: String): String {
        require(pin.length == 4 && pin.all(Char::isDigit)) { "PIN must contain exactly 4 digits" }
        val mac = Mac.getInstance(TRANSFORMATION)
        mac.init(getOrCreateKey())
        val digest = mac.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        return try {
            hashPin(pin) == storedHash
        } catch (_: Exception) {
            false
        }
    }
}
