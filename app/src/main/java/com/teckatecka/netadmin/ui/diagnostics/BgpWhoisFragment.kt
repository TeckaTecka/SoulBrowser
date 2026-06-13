package com.teckatecka.netadmin.ui.diagnostics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentBgpWhoisBinding
import com.teckatecka.netadmin.network.bgp.IpInfo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BgpWhoisFragment : Fragment() {

    private var _binding: FragmentBgpWhoisBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: BgpWhoisViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBgpWhoisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doLookup(); true } else false
        }
        binding.btnLookup.setOnClickListener { doLookup() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { updateUi(it) }
        }
    }

    private fun doLookup() {
        val q = binding.editQuery.text.toString().trim()
        if (q.isNotEmpty()) viewModel.lookup(q)
    }

    private fun updateUi(state: BgpUiState) {
        binding.progressLookup.visibility = if (state is BgpUiState.Loading) View.VISIBLE else View.GONE
        binding.scrollResult.visibility   = if (state is BgpUiState.IpResult || state is BgpUiState.AsnResult) View.VISIBLE else View.GONE
        binding.textError.visibility      = if (state is BgpUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            is BgpUiState.IpResult -> {
                showIpResult(state.info, state.whois)
            }
            is BgpUiState.AsnResult -> {
                binding.cardIp.visibility    = View.GONE
                binding.cardWhois.visibility = View.VISIBLE
                binding.textWhoisContent.text = state.text
            }
            is BgpUiState.Error -> {
                binding.textError.text = getString(R.string.bgp_error, state.message)
            }
            else -> { }
        }
    }

    private fun showIpResult(info: IpInfo, whois: String) {
        binding.cardIp.visibility    = View.VISIBLE
        binding.cardWhois.visibility = View.VISIBLE

        binding.textIpValue.text      = info.ip
        binding.textAsnValue.text     = "${info.asn}  ${info.asnName}"
        binding.textOrgValue.text     = info.org
        binding.textPrefixValue.text  = info.prefix
        binding.textCountryValue.text = info.country
        binding.textRirValue.text     = info.rir
        binding.textWhoisContent.text = whois.ifEmpty { getString(R.string.no_results) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
