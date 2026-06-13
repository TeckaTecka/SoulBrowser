package com.teckatecka.netadmin.ui.snmp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentSnmpBinding
import kotlinx.coroutines.launch

class SnmpFragment : Fragment() {

    private var _binding: FragmentSnmpBinding? = null
    private val binding get() = _binding!!

    private val vm: SnmpViewModel by viewModels()
    private val adapter = SnmpVarBindAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSnmpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerSnmp.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSnmp.adapter       = adapter

        // Přednastavené šablony (chipsy)
        binding.chipGroupPresets.removeAllViews()
        listOf(
            getString(R.string.snmp_preset_system)     to { queryPreset(vm.SYSTEM_OIDS) },
            getString(R.string.snmp_preset_interfaces) to { queryWalk(vm.INTERFACE_OID) }
        ).forEach { (label, action) ->
            val chip = Chip(requireContext())
            chip.text = label
            chip.isClickable = true
            chip.setOnClickListener { action() }
            binding.chipGroupPresets.addView(chip)
        }

        binding.buttonWalk.setOnClickListener {
            val oid = binding.editOid.text.toString().trim()
            if (oid.isEmpty()) return@setOnClickListener
            queryWalk(oid)
        }

        binding.buttonGet.setOnClickListener {
            val oid = binding.editOid.text.toString().trim()
            if (oid.isEmpty()) return@setOnClickListener
            queryPreset(listOf(oid))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { renderState(it) }
            }
        }
    }

    private fun queryWalk(baseOid: String) {
        val (host, port, community) = getConnectionParams() ?: return
        vm.walk(host, port, community, baseOid)
    }

    private fun queryPreset(oids: List<String>) {
        val (host, port, community) = getConnectionParams() ?: return
        vm.getOids(host, port, community, oids)
    }

    private fun getConnectionParams(): Triple<String, Int, String>? {
        val host      = binding.editHost.text.toString().trim()
        val port      = binding.editPort.text.toString().toIntOrNull() ?: 161
        val community = binding.editCommunity.text.toString().ifEmpty { "public" }
        if (host.isEmpty()) {
            Snackbar.make(requireView(), getString(R.string.fill_required_fields), Snackbar.LENGTH_SHORT).show()
            return null
        }
        return Triple(host, port, community)
    }

    private fun renderState(state: SnmpUiState) {
        binding.progressBar.isVisible     = state is SnmpUiState.Loading
        binding.recyclerSnmp.isVisible    = state is SnmpUiState.Success
        binding.textEmpty.isVisible       = state is SnmpUiState.Idle || (state is SnmpUiState.Error)

        when (state) {
            is SnmpUiState.Success -> adapter.submitList(state.items)
            is SnmpUiState.Error   -> Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
            else                   -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
