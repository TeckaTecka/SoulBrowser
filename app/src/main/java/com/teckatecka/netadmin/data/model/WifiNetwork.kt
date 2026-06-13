package com.teckatecka.netadmin.data.model

data class WifiNetwork(
    val ssid:       String,
    val bssid:      String,
    val rssi:       Int,        // dBm
    val channel:    Int,
    val frequency:  Int,        // MHz
    val security:   String,
    val band:       Band
) {
    enum class Band { GHZ_2_4, GHZ_5, GHZ_6 }

    val signalLevel: SignalLevel get() = when {
        rssi >= -50 -> SignalLevel.EXCELLENT
        rssi >= -60 -> SignalLevel.GOOD
        rssi >= -70 -> SignalLevel.FAIR
        else        -> SignalLevel.POOR
    }

    enum class SignalLevel { EXCELLENT, GOOD, FAIR, POOR }
}
