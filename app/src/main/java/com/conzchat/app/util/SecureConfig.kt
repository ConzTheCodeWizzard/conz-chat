package com.conzchat.app.util

/**
 * SecureConfig — stores sensitive constants as XOR-obfuscated byte arrays.
 *
 * No sensitive string ever appears in plain text in the compiled bytecode.
 * Each value is XOR'd against a 16-byte key that is itself assembled from
 * four separate fragments at runtime, making static analysis significantly
 * harder.
 */
object SecureConfig {

    // ── XOR key — assembled from 4 fragments at runtime ────────────────────
    private fun key(): ByteArray {
        val a = byteArrayOf(0x43, 0x7A, 0x21, 0x4E)
        val b = byteArrayOf(0x38, 0x6B, 0x2D, 0x51)
        val c = byteArrayOf(0x7F, 0x19, 0x55, 0x3C)
        val d = byteArrayOf(0x62, 0x0A, 0x4D, 0x77)
        return a + b + c + d
    }

    private fun decode(encoded: ByteArray): String {
        val k = key()
        return String(ByteArray(encoded.size) { i ->
            (encoded[i].toInt() xor k[i % k.size].toInt()).toByte()
        })
    }

    // ── Agora App ID ────────────────────────────────────────────────────────
    // Encodes: ceaa75b1fad242a3bc17085d18351c3d
    private val AGORA_ID_ENC = byteArrayOf(
        0x20, 0x1F, 0x40, 0x2F, 0x0F, 0x5E, 0x4F, 0x60,
        0x19, 0x78, 0x31, 0x0E, 0x56, 0x38, 0x2C, 0x44,
        0x21, 0x19, 0x10, 0x79, 0x08, 0x53, 0x18, 0x35,
        0x4E, 0x21, 0x66, 0x09, 0x53, 0x69, 0x7E, 0x13
    )
    fun agoraAppId(): String = decode(AGORA_ID_ENC)

    // ── Server base URL ─────────────────────────────────────────────────────
    // Encodes: http://167.99.85.232:8080
    private val SERVER_URL_ENC = byteArrayOf(
        0x2B, 0x0E, 0x55, 0x3E, 0x02, 0x44, 0x02, 0x60,
        0x49, 0x2E, 0x7B, 0x05, 0x5B, 0x24, 0x75, 0x42,
        0x6D, 0x48, 0x12, 0x7C, 0x02, 0x53, 0x1D, 0x69,
        0x4F
    )
    fun serverBaseUrl(): String = decode(SERVER_URL_ENC)

    // ── GIPHY API key ───────────────────────────────────────────────────────
    // Encodes: gvnL7xPoArGXv6249XCJl87Hto1qv9wa
    private val GIPHY_ENC = byteArrayOf(
        0x24, 0x0C, 0x4F, 0x02, 0x0F, 0x13, 0x7D, 0x3E,
        0x3E, 0x6B, 0x12, 0x64, 0x14, 0x3C, 0x7F, 0x43,
        0x7A, 0x22, 0x62, 0x04, 0x54, 0x53, 0x1A, 0x19,
        0x0B, 0x76, 0x64, 0x4D, 0x14, 0x33, 0x3A, 0x16
    )
    fun giphyApiKey(): String = decode(GIPHY_ENC)
}
