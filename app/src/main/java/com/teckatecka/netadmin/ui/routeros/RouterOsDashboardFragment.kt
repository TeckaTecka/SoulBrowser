package com.teckatecka.netadmin.ui.routeros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentRouterosDashboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RouterOsDashboardFragment : Fragment() {

    private var _binding: FragmentRouterosDashboardBinding? = null
    private val binding  get() = _binding!!

    // Sdílíme ViewModel s RouterOsFragment (activityViewModels nebo navGraphViewModels)
    private val viewModel: RouterOsViewModel by activityViewModels()
    private lateinit var ifaceAdapter: InterfaceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRouterosDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ifaceAdapter = InterfaceAdapter()
        binding.recyclerInterfaces.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInterfaces.adapter       = ifaceAdapter

        binding.btnDisconnect.setOnClickListener {
            viewModel.disconnect()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                if (state is RouterOsUiState.Connected) updateDashboard(state)
            }
        }
    }

    private fun updateDashboard(state: RouterOsUiState.Connected) {
        val info = state.info ?: return
        binding.textIdentity.text  = info.identity
        binding.textBoard.text     = info.boardName
        binding.textVersion.text   = "RouterOS ${info.version}"
        binding.textUptime.text    = getString(R.string.routeros_uptime) + ": " + info.uptime

        binding.textCpuValue.text  = "${info.cpuLoad}%"
        binding.progressCpu.progress = info.cpuLoad

        val memPercent = if (info.totalMemory > 0) ((info.totalMemory - info.freeMemory) * 100 / info.totalMemory).toInt() else 0
        binding.textMemValue.text  = "$memPercent%"
        binding.progressMem.progress = memPercent

        ifaceAdapter.submitList(state.interfaces)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
