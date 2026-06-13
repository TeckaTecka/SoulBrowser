package com.teckatecka.netadmin.data.model

data class ScanResult(
    val ip:       String,
    val mac:      String  = "",
    val hostname: String  = "",
    val vendor:   String  = "",
    val isUp:     Boolean = false,
    val pingMs:   Long    = -1
)
