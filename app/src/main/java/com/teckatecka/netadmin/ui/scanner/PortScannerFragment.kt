package com.teckatecka.netadmin.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentPortScannerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PortScannerFragment : Fragment() {

    private var _binding: FragmentPortScannerBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: PortScannerViewModel by viewModels()
    private lateinit var adapter: PortResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPortScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PortResultAdapter()
        binding.recyclerPorts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPorts.adapter       = adapter

        binding.btnScanTop100.setOnClickListener {
            val host = binding.editTarget.text.toString().trim()
            if (host.isNotEmpty()) viewModel.scanTop100(host)
        }

        binding.btnScanRange.setOnClickListener {
            val host = binding.editTarget.text.toString().trim()
            val from = binding.editPortFrom.text.toString().toIntOrNull() ?: 1
            val to   = binding.editPortTo.text.toString().toIntOrNull() ?: 1024
            if (host.isNotEmpty()) viewModel.scanRange(host, from, to)
        }

        binding.btnStop.setOnClickListener { viewModel.stop() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { updateUi(it) }
        }
    }

    private fun updateUi(state: PortScanUiState) {
        when (state) {
            is PortScanUiState.Idle     -> {
                binding.textStatus.text = ""
                binding.progressScan.visibility = View.GONE
                binding.btnStop.visibility      = View.GONE
                adapter.submitList(emptyList())
            }
            is PortScanUiState.Scanning -> {
                binding.progressScan.visibility = View.VISIBLE
                binding.btnStop.visibility      = View.VISIBLE
                binding.textStatus.text = getString(R.string.port_scanner_scanning)
                adapter.submitList(state.found)
            }
            is PortScanUiState.Done     -> {
                binding.progressScan.visibility = View.GONE
                binding.btnStop.visibility      = View.GONE
                binding.textStatus.text = if (state.results.isEmpty())
                    getString(R.string.port_scanner_none)
                else
                    getString(R.string.port_scanner_done, state.results.size)
                adapter.submitList(state.results)
            }
            is PortScanUiState.Error    -> {
                binding.progressScan.visibility = View.GONE
                binding.btnStop.visibility      = View.GONE
                binding.textStatus.text = getString(R.string.error_network, state.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
