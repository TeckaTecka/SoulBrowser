package com.teckatecka.netadmin.ui.speedtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentSpeedtestBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpeedTestFragment : Fragment() {

    private var _binding: FragmentSpeedtestBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: SpeedTestViewModel by viewModels()
    private lateinit var historyAdapter: SpeedTestHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSpeedtestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = SpeedTestHistoryAdapter()
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter       = historyAdapter

        binding.btnStartTest.setOnClickListener { viewModel.startTest() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { updateUi(it) }
        }
        lifecycleScope.launch {
            viewModel.history.collectLatest { historyAdapter.submitList(it) }
        }
    }

    private fun updateUi(state: SpeedTestUiState) {
        when (state) {
            is SpeedTestUiState.Idle -> {
                binding.btnStartTest.isEnabled     = true
                binding.btnStartTest.setText(R.string.speedtest_start)
                binding.progressTest.visibility   = View.GONE
                binding.textStatus.text           = ""
            }

            is SpeedTestUiState.Testing -> {
                binding.btnStartTest.isEnabled   = false
                binding.progressTest.visibility  = View.VISIBLE
                binding.progressTest.progress    = (state.progress * 100).toInt()
                binding.textStatus.text = when (state.phase) {
                    SpeedTestUiState.Phase.PING     -> getString(R.string.speedtest_phase_ping)
                    SpeedTestUiState.Phase.DOWNLOAD -> getString(R.string.speedtest_phase_download)
                    SpeedTestUiState.Phase.UPLOAD   -> getString(R.string.speedtest_phase_upload)
                }
            }

            is SpeedTestUiState.Done -> {
                binding.btnStartTest.isEnabled       = true
                binding.btnStartTest.setText(R.string.speedtest_start)
                binding.progressTest.visibility     = View.GONE
                binding.textStatus.text             = ""
                binding.textDownloadValue.text      = "%.1f".format(state.downloadMbps)
                binding.textUploadValue.text        = "%.1f".format(state.uploadMbps)
                binding.textPingValue.text          = state.pingMs.toString()
            }

            is SpeedTestUiState.Error -> {
                binding.btnStartTest.isEnabled   = true
                binding.progressTest.visibility  = View.GONE
                binding.textStatus.text          = getString(R.string.error_network, state.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
