package com.teckatecka.netadmin.ui.routeros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentRouterosDashboardBinding
import com.teckatecka.netadmin.databinding.FragmentRouterosConnectBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RouterOsFragment : Fragment() {

    private var _binding: FragmentRouterosConnectBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: RouterOsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRouterosConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConnect.setOnClickListener {
            val host     = binding.editHost.text.toString().trim()
            val port     = binding.editPort.text.toString().toIntOrNull() ?: 8728
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString()

            if (host.isEmpty()) {
                binding.textConnectStatus.text = getString(R.string.error_invalid_host)
                return@setOnClickListener
            }
            binding.btnConnect.isEnabled      = false
            binding.textConnectStatus.text    = getString(R.string.loading)
            viewModel.connect(host, port, username, password)
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is RouterOsUiState.Connecting    -> {
                        binding.btnConnect.isEnabled   = false
                        binding.textConnectStatus.text = getString(R.string.loading)
                    }
                    is RouterOsUiState.Connected     -> {
                        binding.btnConnect.isEnabled   = true
                        binding.textConnectStatus.text = ""
                        // Přepni na dashboard
                        findNavController().navigate(R.id.action_routeros_to_dashboard)
                    }
                    is RouterOsUiState.Error         -> {
                        binding.btnConnect.isEnabled   = true
                        binding.textConnectStatus.text = getString(R.string.routeros_connection_error, state.message)
                    }
                    is RouterOsUiState.Disconnected  -> {
                        binding.btnConnect.isEnabled   = true
                        binding.textConnectStatus.text = ""
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
