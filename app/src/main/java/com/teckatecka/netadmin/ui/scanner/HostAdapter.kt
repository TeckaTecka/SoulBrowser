package com.teckatecka.netadmin.ui.scanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.data.model.ScanResult
import com.teckatecka.netadmin.databinding.ItemHostBinding

class HostAdapter : ListAdapter<ScanResult, HostAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScanResult) {
            binding.textIp.text       = item.ip
            binding.textHostname.text = item.hostname.takeIf { it.isNotEmpty() } ?: ""
            binding.textVendor.text   = item.vendor.takeIf { it.isNotEmpty() } ?: ""
            binding.textPing.text     = if (item.pingMs >= 0) "${item.pingMs}ms" else ""
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ScanResult>() {
            override fun areItemsTheSame(old: ScanResult, new: ScanResult) = old.ip == new.ip
            override fun areContentsTheSame(old: ScanResult, new: ScanResult) = old == new
        }
    }
}
