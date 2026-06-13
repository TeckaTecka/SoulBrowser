package com.teckatecka.netadmin.ui.monitoring

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.ItemMonitoredHostBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonitoredHostAdapter(
    private val onDelete: (MonitoredHost) -> Unit
) : ListAdapter<MonitoredHost, MonitoredHostAdapter.ViewHolder>(DIFF) {

    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemMonitoredHostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemMonitoredHostBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(h: MonitoredHost) {
            b.textMonitorHost.text = h.host

            when (h.isUp) {
                true  -> {
                    b.textMonitorStatus.text     = b.root.context.getString(R.string.monitoring_status_up)
                    b.textMonitorStatus.setTextColor(b.root.context.getColor(R.color.host_up))
                    b.textMonitorPing.text        = if (h.lastPingMs >= 0) "${h.lastPingMs}ms" else ""
                }
                false -> {
                    b.textMonitorStatus.text     = b.root.context.getString(R.string.monitoring_status_down)
                    b.textMonitorStatus.setTextColor(b.root.context.getColor(R.color.host_down))
                    b.textMonitorPing.text        = ""
                }
                null  -> {
                    b.textMonitorStatus.text     = "…"
                    b.textMonitorPing.text        = ""
                }
            }

            b.textMonitorLastCheck.text = if (h.lastCheckMs > 0)
                fmt.format(Date(h.lastCheckMs))
            else ""

            b.textMonitorInterval.text = "${h.intervalMin}m"
            b.btnMonitorDelete.setOnClickListener { onDelete(h) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MonitoredHost>() {
            override fun areItemsTheSame(a: MonitoredHost, b: MonitoredHost) = a.host == b.host
            override fun areContentsTheSame(a: MonitoredHost, b: MonitoredHost) = a == b
        }
    }
}
