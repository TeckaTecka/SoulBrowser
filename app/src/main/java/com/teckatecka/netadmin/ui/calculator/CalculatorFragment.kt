package com.teckatecka.netadmin.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentCalculatorBinding
import com.teckatecka.netadmin.utils.CidrResult
import com.teckatecka.netadmin.utils.VlsmSubnet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: CalculatorViewModel by viewModels()
    private val vlsmSubnets = mutableListOf<Pair<TextInputEditText, TextInputEditText>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CIDR výpočet po Enter nebo kliknutí na tlačítko
        binding.editCidrInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.calculateCidr(binding.editCidrInput.text.toString())
                true
            } else false
        }
        binding.btnCidrCalculate.setOnClickListener {
            viewModel.calculateCidr(binding.editCidrInput.text.toString())
        }

        // Přidání VLSM podsítě
        binding.btnVlsmAdd.setOnClickListener { addVlsmRow() }
        binding.btnVlsmCalculate.setOnClickListener { runVlsmCalculation() }

        // Tab přepínání CIDR ↔ VLSM
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val isCidr = tab.position == 0
                binding.layoutCidrInput.visibility  = if (isCidr) View.VISIBLE else View.GONE
                binding.btnCidrCalculate.visibility = if (isCidr) View.VISIBLE else View.GONE
                binding.cardCidrResult.visibility   = if (isCidr && viewModel.state.value is CalcUiState.CidrSuccess) View.VISIBLE else View.GONE
                binding.sectionVlsm.visibility      = if (!isCidr) View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        lifecycleScope.launch {
            viewModel.state.collectLatest { state -> updateUi(state) }
        }
    }

    private fun updateUi(state: CalcUiState) {
        when (state) {
            is CalcUiState.Idle, is CalcUiState.Loading -> { /* nic */ }

            is CalcUiState.CidrSuccess -> showCidrResult(state.result)

            is CalcUiState.VlsmSuccess -> {
                val adapter = VlsmResultAdapter(state.results)
                binding.recyclerVlsmResults.adapter = adapter
            }

            is CalcUiState.Error -> {
                binding.cardCidrResult.visibility = View.GONE
                binding.layoutCidrInput.error     = state.message
            }
        }
    }

    private fun showCidrResult(r: CidrResult) {
        binding.layoutCidrInput.error = null
        binding.cardCidrResult.visibility = View.VISIBLE
        binding.textCidrNetwork.text   = getString(R.string.cidr_network)   + ":  " + r.networkAddr
        binding.textCidrBroadcast.text = getString(R.string.cidr_broadcast) + ": " + r.broadcastAddr
        binding.textCidrFirst.text     = getString(R.string.cidr_first_host)+ ": " + r.firstHost
        binding.textCidrLast.text      = getString(R.string.cidr_last_host) + ":  " + r.lastHost
        binding.textCidrHosts.text     = getString(R.string.cidr_host_count)+ ": " + r.hostCount
        binding.textCidrMask.text      = getString(R.string.cidr_subnet_mask)+ ": "+ r.subnetMask
        binding.textCidrWildcard.text  = getString(R.string.cidr_wildcard)  + ":  " + r.wildcardMask
    }

    private fun addVlsmRow() {
        val row    = layoutInflater.inflate(R.layout.item_vlsm_row, binding.containerVlsmSubnets, false)
        val editName  = row.findViewById<TextInputEditText>(R.id.edit_vlsm_name)
        val editHosts = row.findViewById<TextInputEditText>(R.id.edit_vlsm_hosts)
        val btnRemove = row.findViewById<View>(R.id.btn_vlsm_remove)

        val pair = Pair(editName, editHosts)
        vlsmSubnets.add(pair)

        btnRemove.setOnClickListener {
            binding.containerVlsmSubnets.removeView(row)
            vlsmSubnets.remove(pair)
        }
        binding.containerVlsmSubnets.addView(row)
    }

    private fun runVlsmCalculation() {
        val base    = binding.editVlsmBase.text.toString()
        val subnets = vlsmSubnets.mapNotNull { (name, hosts) ->
            val h = hosts.text.toString().toIntOrNull() ?: return@mapNotNull null
            VlsmSubnet(name = name.text.toString().ifEmpty { "Subnet" }, hostsNeeded = h)
        }
        viewModel.calculateVlsm(base, subnets)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
