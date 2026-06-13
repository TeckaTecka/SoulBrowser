package com.teckatecka.netadmin.ui.wifi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.data.model.WifiNetwork
import com.teckatecka.netadmin.databinding.ItemWifiNetworkBinding

class WifiAdapter : ListAdapter<WifiNetwork, WifiAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWifiNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemWifiNetworkBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(n: WifiNetwork) {
            b.textSsid.text     = n.ssid
            b.textBssid.text    = n.bssid
            b.textChannel.text  = b.root.context.getString(R.string.wifi_channel, n.channel)
            b.textRssi.text     = "${n.rssi} dBm"
            b.textSecurity.text = n.security

            val colorRes = when (n.signalLevel) {
                WifiNetwork.SignalLevel.EXCELLENT -> R.color.signal_excellent
                WifiNetwork.SignalLevel.GOOD      -> R.color.signal_good
                WifiNetwork.SignalLevel.FAIR      -> R.color.signal_fair
                WifiNetwork.SignalLevel.POOR      -> R.color.signal_poor
            }
            b.textRssi.setTextColor(b.root.context.getColor(colorRes))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WifiNetwork>() {
            override fun areItemsTheSame(old: WifiNetwork, new: WifiNetwork) = old.bssid == new.bssid
            override fun areContentsTheSame(old: WifiNetwork, new: WifiNetwork) = old == new
        }
    }
}
