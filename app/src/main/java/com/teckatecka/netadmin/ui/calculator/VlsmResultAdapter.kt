package com.teckatecka.netadmin.ui.calculator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.databinding.ItemVlsmResultBinding
import com.teckatecka.netadmin.utils.VlsmResult

class VlsmResultAdapter(private val items: List<VlsmResult>) :
    RecyclerView.Adapter<VlsmResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVlsmResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val b: ItemVlsmResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: VlsmResult) {
            b.textVlsmName.text     = item.name
            b.textVlsmNetwork.text  = "${item.network}/${item.prefixLen}"
            b.textVlsmMask.text     = item.subnetMask
            b.textVlsmRange.text    = "${item.firstHost} – ${item.lastHost}"
            b.textVlsmHosts.text    = item.usableHosts.toString()
        }
    }
}
