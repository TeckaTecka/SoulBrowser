package com.teckatecka.netadmin.ui.diagnostics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentDiagnosticsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiagnosticsFragment : Fragment() {

    private var _binding: FragmentDiagnosticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiagnosticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiagnosticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPing.setOnClickListener {
            val host  = binding.editTarget.text.toString().trim()
            val count = binding.editCount.text.toString().toIntOrNull() ?: 4
            if (host.isNotEmpty()) viewModel.ping(host, count)
        }

        binding.btnTraceroute.setOnClickListener {
            val host = binding.editTarget.text.toString().trim()
            if (host.isNotEmpty()) viewModel.traceroute(host)
        }

        binding.btnDns.setOnClickListener {
            val host = binding.editTarget.text.toString().trim()
            if (host.isNotEmpty()) viewModel.dnsLookup(host)
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { updateUi(it) }
        }
    }

    private fun updateUi(state: DiagUiState) {
        when (state) {
            is DiagUiState.Idle    -> binding.textOutput.text = ""

            is DiagUiState.PingRunning -> {
                binding.textOutput.text = state.lines.joinToString("\n") { line ->
                    if (line.ms >= 0) "[$${line.seq}] ${line.host}  ${line.ms}ms"
                    else              "[$${line.seq}] Request timeout"
                }
            }

            is DiagUiState.PingDone -> {
                val summary = getString(
                    R.string.ping_result_stats,
                    state.sent, state.received,
                    state.minMs, state.avgMs, state.maxMs,
                    ((state.sent - state.received) * 100 / state.sent)
                )
                binding.textOutput.text = state.lines.joinToString("\n") { line ->
                    if (line.ms >= 0) "[${line.seq}] ${line.host}  ${line.ms}ms"
                    else              "[${line.seq}] Request timeout"
                } + "\n\n$summary"
            }

            is DiagUiState.TracerouteRunning -> {
                binding.textOutput.text = state.hops.joinToString("\n") { hop ->
                    val display = hop.hostname.takeIf { it != hop.ip && it.isNotEmpty() } ?: hop.ip
                    val ms      = if (hop.ms >= 0) "${hop.ms}ms" else "*"
                    "${hop.hop.toString().padStart(2)}  $display  $ms"
                }
            }

            is DiagUiState.TracerouteDone -> {
                binding.textOutput.text = state.hops.joinToString("\n") { hop ->
                    val display = hop.hostname.takeIf { it != hop.ip && it.isNotEmpty() } ?: hop.ip
                    val ms      = if (hop.ms >= 0) "${hop.ms}ms" else "*"
                    "${hop.hop.toString().padStart(2)}  $display  $ms"
                }
            }

            is DiagUiState.DnsResult -> {
                binding.textOutput.text = state.records.joinToString("\n") { "${it.type.padEnd(6)}  ${it.value}" }
            }

            is DiagUiState.Error -> {
                binding.textOutput.text = getString(R.string.error_network, state.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
