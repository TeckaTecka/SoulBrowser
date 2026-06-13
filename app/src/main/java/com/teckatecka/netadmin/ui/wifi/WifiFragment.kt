package com.teckatecka.netadmin.ui.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentWifiBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WifiFragment : Fragment() {

    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WifiViewModel by viewModels()
    private lateinit var adapter: WifiAdapter

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startScan()
        else         binding.textWifiStatus.text = getString(R.string.wifi_permission_needed)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WifiAdapter()
        binding.recyclerWifi.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWifi.adapter       = adapter

        binding.btnWifiScan.setOnClickListener { checkPermissionAndScan() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { updateUi(it) }
        }
    }

    private fun checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startScan() {
        viewModel.scan(requireContext())
    }

    private fun updateUi(state: WifiUiState) {
        when (state) {
            is WifiUiState.Idle     -> {
                binding.progressWifi.visibility = View.GONE
                binding.textWifiStatus.text = ""
            }
            is WifiUiState.Scanning -> {
                binding.progressWifi.visibility = View.VISIBLE
                binding.textWifiStatus.text = getString(R.string.loading)
            }
            is WifiUiState.Results  -> {
                binding.progressWifi.visibility = View.GONE
                binding.textWifiStatus.text = getString(R.string.wifi_networks_found, state.networks.size)
                adapter.submitList(state.networks)
            }
            is WifiUiState.Error    -> {
                binding.progressWifi.visibility = View.GONE
                binding.textWifiStatus.text = getString(R.string.error_network, state.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
