package com.teckatecka.netadmin.ui.routeros

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.ItemRouterosInterfaceBinding

class InterfaceAdapter : ListAdapter<RouterInterface, InterfaceAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemRouterosInterfaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemRouterosInterfaceBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(iface: RouterInterface) {
            b.textIfaceName.text   = iface.name
            b.textIfaceType.text   = iface.type
            b.textIfaceIp.text     = iface.ipAddress.takeIf { it.isNotEmpty() } ?: iface.macAddress
            b.textIfaceStatus.text = if (iface.running) "UP" else "DOWN"
            b.textIfaceStatus.setTextColor(
                b.root.context.getColor(if (iface.running) R.color.host_up else R.color.host_down)
            )
            b.textIfaceTx.text = "↑ ${formatBytes(iface.txBytes)}"
            b.textIfaceRx.text = "↓ ${formatBytes(iface.rxBytes)}"
        }

        private fun formatBytes(bytes: Long): String = when {
            bytes > 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes > 1_048_576     -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes > 1_024         -> "%.1f KB".format(bytes / 1_024.0)
            else                  -> "$bytes B"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RouterInterface>() {
            override fun areItemsTheSame(a: RouterInterface, b: RouterInterface) = a.name == b.name
            override fun areContentsTheSame(a: RouterInterface, b: RouterInterface) = a == b
        }
    }
}
