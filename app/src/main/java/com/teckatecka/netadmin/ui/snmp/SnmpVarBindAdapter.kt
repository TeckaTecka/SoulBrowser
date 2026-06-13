package com.teckatecka.netadmin.ui.snmp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teckatecka.netadmin.databinding.ItemSnmpVarbindBinding
import com.teckatecka.netadmin.network.snmp.SnmpVarBind

class SnmpVarBindAdapter : ListAdapter<SnmpVarBind, SnmpVarBindAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemSnmpVarbindBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SnmpVarBind) {
            b.textOid.text   = item.oid
            b.textValue.text = item.value
            b.textType.text  = item.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSnmpVarbindBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SnmpVarBind>() {
            override fun areItemsTheSame(a: SnmpVarBind, b: SnmpVarBind) = a.oid == b.oid
            override fun areContentsTheSame(a: SnmpVarBind, b: SnmpVarBind) = a == b
        }
    }
}
