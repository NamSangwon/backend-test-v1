package im.bigs.pg.external.pg.secret

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AesGcmEncryptor : Encryptor {
    @Value("\${external.pg.test.iv.base64-url}")
    private lateinit var base64Url: String

    @Value("\${external.pg.test.api-key}")
    private lateinit var apiKey: String

    private val HASHING_ALGORITHM = "SHA-256"
    private val ALGORITHM = "AES"
    private val MODE = "AES/GCM/NoPadding"
    private val TAG_LENGTH_BIT = 128 // 16바이트
    private val IV_LENGTH_BYTE = 12  // 96비트

    private fun getHashKey(): ByteArray {
        return MessageDigest.getInstance(HASHING_ALGORITHM).digest(apiKey.toByteArray(Charsets.UTF_8))
    }

    private fun getIvBytes(): ByteArray {
        val ivBytes = Base64.getUrlDecoder().decode(base64Url)
        require(ivBytes.size == IV_LENGTH_BYTE) { "IV must be 12 bytes (96 bits). Actual: ${ivBytes.size}" }
        return ivBytes
    }

    override fun encrypt(plainText: String): String {
        // 1. 알고리즘: AES-256-GCM (AES/GCM/NoPadding)
        val cipher = Cipher.getInstance(MODE)

        // 2. Key: SHA-256(API-KEY) → 32바이트 키(UTF-8 바이트로 해시)
        val secretKey = SecretKeySpec(getHashKey(), ALGORITHM)

        // 3. IV: 12바이트(96비트), 서버에 사전 등록. 클라이언트는 `<IV_B64URL>`을 Base64URL 디코딩하여 사용
        val iv = IvParameterSpec(getIvBytes(), 0, IV_LENGTH_BYTE).iv

        // 3. CipherText: GCM(key, iv).encrypt(plaintextBytes) → ciphertext||tag
        val gcm = GCMParameterSpec(TAG_LENGTH_BIT, iv) // 태그 길이 128비트(16바이트)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcm)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 4. enc: ciphertext || tag 결과를 Base64URL(패딩 없음)로 인코딩
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes)
    }
}