package com.decoapps.wearotp.shared.utils

import android.content.Context
import java.text.DateFormat
import java.util.Date


fun formatLastSync(epochMilliseconds: Long?, context: Context): String {
    if (epochMilliseconds == null) return "Never"
    val date = Date(epochMilliseconds)
    val formatter = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        context.resources.configuration.locales[0]
    )
    return formatter.format(date)
}