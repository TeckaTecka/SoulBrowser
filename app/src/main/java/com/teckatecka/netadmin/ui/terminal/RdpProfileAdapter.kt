package com.teckatecka.netadmin.ui.terminal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.data.model.RdpProfile
import com.teckatecka.netadmin.databinding.ItemRdpProfileBinding

class RdpProfileAdapter(
    private val onConnect: (RdpProfile) -> Unit,
    private val onWinRm:   (RdpProfile) -> Unit,
    private val onDelete:  (RdpProfile) -> Unit
) : ListAdapter<RdpProfile, RdpProfileAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemRdpProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemRdpProfileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: RdpProfile) {
            b.textProfileHost.text     = "${p.host}:${p.port}"
            b.textProfileUsername.text = p.username.takeIf { it.isNotEmpty() } ?: "—"
            b.textProfileDomain.text   = p.domain.takeIf { it.isNotEmpty() } ?: ""
            b.btnConnect.setOnClickListener  { onConnect(p) }
            b.btnWinrm.setOnClickListener    { onWinRm(p) }
            b.btnDelete.setOnClickListener   { onDelete(p) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RdpProfile>() {
            override fun areItemsTheSame(a: RdpProfile, b: RdpProfile) = a.id == b.id
            override fun areContentsTheSame(a: RdpProfile, b: RdpProfile) = a == b
        }
    }
}
