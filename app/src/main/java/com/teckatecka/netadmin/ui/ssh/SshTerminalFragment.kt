package com.teckatecka.netadmin.ui.ssh

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
import com.google.android.material.snackbar.Snackbar
import com.teckatecka.netadmin.databinding.FragmentSshTerminalBinding
import kotlinx.coroutines.launch

class SshTerminalFragment : Fragment() {

    private var _binding: FragmentSshTerminalBinding? = null
    private val binding get() = _binding!!

    private val vm: SshTerminalViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSshTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonConnect.setOnClickListener {
            val host     = binding.editHost.text.toString().trim()
            val port     = binding.editPort.text.toString().toIntOrNull() ?: 22
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString()
            if (host.isEmpty() || username.isEmpty()) {
                Snackbar.make(view, getString(com.teckatecka.netadmin.R.string.fill_required_fields), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.connect(host, port, username, password)
        }

        binding.buttonDisconnect.setOnClickListener {
            vm.disconnect()
        }

        binding.buttonSend.setOnClickListener {
            val cmd = binding.editCommand.text.toString()
            vm.sendCommand(cmd)
            binding.editCommand.text?.clear()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.state.collect { renderState(it) } }
                launch { vm.output.collect { binding.textTerminal.text = it } }
            }
        }
    }

    private fun renderState(state: SshUiState) {
        when (state) {
            is SshUiState.Idle -> {
                binding.layoutConnect.isVisible    = true
                binding.layoutTerminal.isVisible   = false
                binding.progressBar.isVisible      = false
            }
            is SshUiState.Connecting -> {
                binding.progressBar.isVisible      = true
                binding.layoutConnect.isVisible    = false
                binding.layoutTerminal.isVisible   = false
            }
            is SshUiState.Connected -> {
                binding.progressBar.isVisible      = false
                binding.layoutConnect.isVisible    = false
                binding.layoutTerminal.isVisible   = true
            }
            is SshUiState.Error -> {
                binding.progressBar.isVisible      = false
                binding.layoutConnect.isVisible    = true
                binding.layoutTerminal.isVisible   = false
                Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
