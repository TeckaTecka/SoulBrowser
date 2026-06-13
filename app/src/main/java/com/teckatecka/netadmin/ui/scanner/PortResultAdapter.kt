package com.teckatecka.netadmin.ui.scanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.ItemPortResultBinding
import com.teckatecka.netadmin.network.scanner.PortResult

class PortResultAdapter : ListAdapter<PortResult, PortResultAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemPortResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemPortResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: PortResult) {
            b.textPort.text    = p.port.toString()
            b.textService.text = p.service.takeIf { it.isNotEmpty() } ?: "unknown"
            b.textState.text   = b.root.context.getString(when (p.state) {
                PortResult.State.OPEN     -> R.string.port_open
                PortResult.State.CLOSED   -> R.string.port_closed
                PortResult.State.FILTERED -> R.string.port_filtered
            })
            b.textState.setTextColor(b.root.context.getColor(when (p.state) {
                PortResult.State.OPEN     -> R.color.port_open
                PortResult.State.CLOSED   -> R.color.port_closed
                PortResult.State.FILTERED -> R.color.port_filtered
            }))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PortResult>() {
            override fun areItemsTheSame(a: PortResult, b: PortResult) = a.port == b.port
            override fun areContentsTheSame(a: PortResult, b: PortResult) = a == b
        }
    }
}
