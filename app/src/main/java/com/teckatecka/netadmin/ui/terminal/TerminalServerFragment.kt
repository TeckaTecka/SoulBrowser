package com.teckatecka.netadmin.ui.terminal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.data.model.RdpProfile
import com.teckatecka.netadmin.databinding.DialogAddRdpBinding
import com.teckatecka.netadmin.databinding.FragmentTerminalServerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TerminalServerFragment : Fragment() {

    private var _binding: FragmentTerminalServerBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: TerminalServerViewModel by viewModels()
    private lateinit var adapter: RdpProfileAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTerminalServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RdpProfileAdapter(
            onConnect = { profile -> launchRdp(profile) },
            onWinRm   = { profile -> viewModel.checkWinRm(profile.host) },
            onDelete  = { profile -> viewModel.deleteProfile(profile) }
        )
        binding.recyclerProfiles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProfiles.adapter       = adapter

        binding.fabAddProfile.setOnClickListener { showAddDialog() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is TerminalUiState.ProfileList -> {
                        binding.textEmpty.visibility     = if (state.profiles.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerProfiles.visibility = if (state.profiles.isEmpty()) View.GONE else View.VISIBLE
                        binding.textWinrmResult.visibility  = View.GONE
                        adapter.submitList(state.profiles)
                    }
                    is TerminalUiState.WinRmResult -> {
                        binding.textWinrmResult.visibility = View.VISIBLE
                        binding.textWinrmResult.text = if (state.ok)
                            getString(R.string.terminal_winrm_ok)
                        else
                            getString(R.string.terminal_winrm_fail, state.message)
                        binding.textWinrmResult.setTextColor(
                            requireContext().getColor(if (state.ok) R.color.success else R.color.error)
                        )
                    }
                }
            }
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddRdpBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.terminal_add_profile))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val host     = dialogBinding.editHost.text.toString().trim()
                val port     = dialogBinding.editPort.text.toString().toIntOrNull() ?: 3389
                val username = dialogBinding.editUsername.text.toString().trim()
                val domain   = dialogBinding.editDomain.text.toString().trim()
                val label    = host
                if (host.isNotEmpty()) {
                    viewModel.addProfile(RdpProfile(host = host, port = port, username = username, domain = domain, label = label))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /** Spustí systémový RDP klient přes Intent (např. Microsoft Remote Desktop). */
    private fun launchRdp(profile: RdpProfile) {
        val uri = Uri.parse("rdp://full%20address=s:${profile.host}:${profile.port}&username=s:${profile.username}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            binding.textWinrmResult.visibility = View.VISIBLE
            binding.textWinrmResult.text       = getString(R.string.terminal_no_rdp_client)
            binding.textWinrmResult.setTextColor(requireContext().getColor(R.color.warning))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
