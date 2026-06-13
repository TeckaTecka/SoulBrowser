package com.teckatecka.netadmin.ui.speedtest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.databinding.ItemSpeedtestHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SpeedTestRecord(
    val timestamp:    Long,
    val downloadMbps: Double,
    val uploadMbps:   Double,
    val pingMs:       Long
)

class SpeedTestHistoryAdapter :
    ListAdapter<SpeedTestRecord, SpeedTestHistoryAdapter.ViewHolder>(DIFF) {

    private val fmt = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemSpeedtestHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemSpeedtestHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: SpeedTestRecord) {
            b.textHistoryTime.text     = fmt.format(Date(r.timestamp))
            b.textHistoryDownload.text = "↓ %.1f".format(r.downloadMbps)
            b.textHistoryUpload.text   = "↑ %.1f".format(r.uploadMbps)
            b.textHistoryPing.text     = "${r.pingMs}ms"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SpeedTestRecord>() {
            override fun areItemsTheSame(a: SpeedTestRecord, b: SpeedTestRecord) = a.timestamp == b.timestamp
            override fun areContentsTheSame(a: SpeedTestRecord, b: SpeedTestRecord) = a == b
        }
    }
}
