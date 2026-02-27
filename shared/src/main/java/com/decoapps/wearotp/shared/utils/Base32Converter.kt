package com.decoapps.wearotp.shared.utils


fun base32ToHex(base32: String): String {
    val cleanBase32 = base32.replace(" ", "").uppercase()

    val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    var bits = ""
    for (char in cleanBase32) {
        val value = base32Chars.indexOf(char)
        if (value == -1) {
            throw IllegalArgumentException("Invalid Character: $char")
        }
        bits += value.toString(2).padStart(5, '0')
    }

    val bytes = mutableListOf<Byte>()
    var i = 0
    while (i + 8 <= bits.length) {
        val byteStr = bits.substring(i, i + 8)
        bytes.add(byteStr.toInt(2).toByte())
        i += 8
    }

    return bytes.joinToString("") { byte ->
        String.format("%02X", byte)
    }
}