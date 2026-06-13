package com.teckatecka.netadmin.network.bgp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class IpInfo(
    val ip:      String,
    val asn:     String,
    val org:     String,
    val country: String,
    val rir:     String,
    val prefix:  String,
    val asnName: String
)

data class WhoisResult(
    val query:    String,
    val raw:      String        // celý WHOIS text
)

/**
 * Volá bgpview.io public API pro BGP lookup a rdap.org pro RDAP/WHOIS.
 * Žádný API klíč není potřeba — oba endpointy jsou veřejně přístupné.
 */
class BgpWhoisClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Lookup IP adresy — vrátí ASN, organizaci, zemi, prefix. */
    suspend fun lookupIp(ip: String): IpInfo = withContext(Dispatchers.IO) {
        val url  = "https://api.bgpview.io/ip/$ip"
        val json = get(url)
        val data = json.getJSONObject("data")
        val prefixes = data.optJSONArray("prefixes")
        val prefix   = prefixes?.optJSONObject(0)
        val asn      = prefix?.optJSONObject("asn")

        IpInfo(
            ip      = ip,
            asn     = asn?.optString("asn", "")?.let { "AS$it" } ?: "",
            asnName = asn?.optString("name", "") ?: "",
            org     = asn?.optString("description", "") ?: "",
            country = asn?.optString("country_code", "") ?: "",
            rir     = asn?.optString("rir_allocation", "") ?: "",
            prefix  = prefix?.optString("prefix", "") ?: ""
        )
    }

    /** RDAP lookup pro IP — strukturovaná odpověď místo raw WHOIS textu. */
    suspend fun rdapIp(ip: String): WhoisResult = withContext(Dispatchers.IO) {
        val url  = "https://rdap.org/ip/$ip"
        val json = get(url)
        val name    = json.optString("name", "")
        val handle  = json.optString("handle", "")
        val country = json.optString("country", "")
        val events  = json.optJSONArray("events")
        val remarks = json.optJSONArray("remarks")

        val sb = StringBuilder()
        if (handle.isNotEmpty())  sb.appendLine("Handle:  $handle")
        if (name.isNotEmpty())    sb.appendLine("Name:    $name")
        if (country.isNotEmpty()) sb.appendLine("Country: $country")
        remarks?.let { arr ->
            for (i in 0 until arr.length()) {
                val r    = arr.getJSONObject(i)
                val desc = r.optJSONArray("description")
                if (desc != null) {
                    for (j in 0 until desc.length()) sb.appendLine(desc.getString(j))
                }
            }
        }
        events?.let { arr ->
            for (i in 0 until arr.length()) {
                val ev = arr.getJSONObject(i)
                sb.appendLine("${ev.optString("eventAction")}: ${ev.optString("eventDate")}")
            }
        }

        WhoisResult(query = ip, raw = sb.toString().trim())
    }

    /** BGP prefix lookup pro ASN číslo. */
    suspend fun lookupAsn(asn: String): String = withContext(Dispatchers.IO) {
        val asnNum = asn.removePrefix("AS").removePrefix("as")
        val url    = "https://api.bgpview.io/asn/$asnNum"
        val json   = get(url)
        val data   = json.getJSONObject("data")
        buildString {
            appendLine("ASN:         AS$asnNum")
            appendLine("Name:        ${data.optString("name")}")
            appendLine("Description: ${data.optString("description")}")
            appendLine("Country:     ${data.optString("country_code")}")
            appendLine("RIR:         ${data.optString("rir_allocation")}")
            appendLine("Website:     ${data.optString("website")}")
        }.trim()
    }

    private fun get(url: String): JSONObject {
        val req  = Request.Builder().url(url).build()
        val body = client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: throw Exception("Empty response")
        }
        return JSONObject(body)
    }
}
