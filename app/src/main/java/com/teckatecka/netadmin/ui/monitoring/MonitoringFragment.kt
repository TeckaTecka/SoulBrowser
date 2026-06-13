package com.teckatecka.netadmin.ui.monitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.DialogAddMonitorHostBinding
import com.teckatecka.netadmin.databinding.FragmentMonitoringBinding
import com.teckatecka.netadmin.service.MonitoringWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MonitoringFragment : Fragment() {

    private var _binding: FragmentMonitoringBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: MonitoringViewModel by viewModels()
    private lateinit var adapter: MonitoredHostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MonitoredHostAdapter(
            onDelete = { viewModel.removeHost(it) }
        )
        binding.recyclerHosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHosts.adapter       = adapter

        binding.fabAddHost.setOnClickListener { showAddDialog() }
        scheduleBackgroundMonitoring()

        lifecycleScope.launch {
            viewModel.hosts.collectLatest { hosts ->
                binding.textEmpty.visibility       = if (hosts.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerHosts.visibility   = if (hosts.isEmpty()) View.GONE else View.VISIBLE
                adapter.submitList(hosts)
            }
        }
    }

    private fun scheduleBackgroundMonitoring() {
        val request = PeriodicWorkRequestBuilder<MonitoringWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            MonitoringWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddMonitorHostBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.monitoring_add_host))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val host     = dialogBinding.editHost.text.toString().trim()
                val interval = dialogBinding.editInterval.text.toString().toIntOrNull() ?: 5
                if (host.isNotEmpty()) viewModel.addHost(host, interval)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopAll()
        _binding = null
    }
}
