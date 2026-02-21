package com.decoapps.wearotp.mobile.screens.otp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoapps.wearotp.shared.data.TOTP.generateTOTP
import java.lang.Long
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.String
import kotlin.text.uppercase
import java.time.Instant

/**
 * Converte una stringa Base32 in una stringa esadecimale
 * Base32 usa i caratteri A-Z e 2-7
 */
fun base32ToHex(base32: String): String {
    // Rimuovi spazi e converti in maiuscolo
    val cleanBase32 = base32.replace(" ", "").uppercase()

    // Mappa dei caratteri Base32
    val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    // Converti Base32 in byte array
    var bits = ""
    for (char in cleanBase32) {
        val value = base32Chars.indexOf(char)
        if (value == -1) {
            throw IllegalArgumentException("Carattere Base32 non valido: $char")
        }
        bits += value.toString(2).padStart(5, '0')
    }

    // Converti bits in byte array
    val bytes = mutableListOf<Byte>()
    var i = 0
    while (i + 8 <= bits.length) {
        val byteStr = bits.substring(i, i + 8)
        bytes.add(byteStr.toInt(2).toByte())
        i += 8
    }

    // Converti byte array in stringa hex
    return bytes.joinToString("") { byte ->
        String.format("%02X", byte)
    }
}

@Composable
fun OTPScreen() {
    HelloWorldScreen()
}

fun test(seed : String) {

    // Seed for HMAC-SHA1 - 20 bytes
    val T0: kotlin.Long = 0
    val X: kotlin.Long = 30

    var steps = "0"
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    df.timeZone = TimeZone.getTimeZone("UTC")
    val epoch = Instant.now().epochSecond
    val T: kotlin.Long = (epoch - T0) / X
    steps = Long.toHexString(T).uppercase(Locale.getDefault())
    val fmtTime = java.lang.String.format("%1$-11s", epoch)
    val utcTime: String? = df.format(Date(epoch * 1000))
    print(
        "|  " + fmtTime + "  |  " + utcTime +
                "  | " + steps + " |"
    )
    println(
            generateTOTP(
                seed, steps, "6",
                "HmacSHA1"
            ) + "| SHA1   |"
        )
    println(
        "+---------------+-----------------------+" +
                "------------------+--------+--------+"
    )

}


@Composable
fun HelloWorldScreen() {
    val inputText = remember { mutableStateOf("") }
    val addedTexts = remember { mutableStateOf(listOf<String>()) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello World",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                label = { Text("Enter text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    test("I65VU7K5ZQL7WB4E")
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Add")
            }

            // Display added texts
            addedTexts.value.forEach { text ->
                Text(
                    text = text,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
