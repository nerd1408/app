package com.witvpn.ikev2.presentation.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun Calendar.toStringWithPattern(pattern: String = "HH:mm dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return sdf.format(this.time)
}