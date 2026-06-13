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
import com.teckatecka.netadmin.databinding.FragmentScannerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScannerViewModel by viewModels()
    private lateinit var adapter: HostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HostAdapter()
        binding.recyclerHosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHosts.adapter       = adapter

        binding.btnScan.setOnClickListener {
            when (val state = viewModel.state.value) {
                is ScannerUiState.Scanning -> viewModel.stopScan()
                else                       -> viewModel.startScan()
            }
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUi(state)
            }
        }
    }

    private fun updateUi(state: ScannerUiState) {
        when (state) {
            is ScannerUiState.Idle -> {
                binding.btnScan.setText(R.string.scanner_start)
                binding.progressScan.visibility  = View.GONE
                binding.textScanStatus.text = ""
                adapter.submitList(emptyList())
            }
            is ScannerUiState.Scanning -> {
                binding.btnScan.setText(R.string.scanner_stop)
                binding.progressScan.visibility  = View.VISIBLE
                binding.textScanStatus.text = getString(R.string.scanner_found_devices, state.found.size)
                adapter.submitList(state.found)
            }
            is ScannerUiState.Done -> {
                binding.btnScan.setText(R.string.scanner_start)
                binding.progressScan.visibility  = View.GONE
                binding.textScanStatus.text = getString(R.string.scanner_found_devices, state.results.size)
                adapter.submitList(state.results)
            }
            is ScannerUiState.Error -> {
                binding.btnScan.setText(R.string.scanner_start)
                binding.progressScan.visibility  = View.GONE
                binding.textScanStatus.text = getString(R.string.error_network, state.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
