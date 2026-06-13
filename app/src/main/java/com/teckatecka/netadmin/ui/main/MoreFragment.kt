package com.teckatecka.netadmin.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRouteros.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_routeros)
        }
        binding.btnSpeedtest.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_speedtest)
        }
        binding.btnPortScanner.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_portscanner)
        }
        binding.btnBgpWhois.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_bgpwhois)
        }
        binding.btnMonitoring.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_monitoring)
        }
        binding.btnTerminal.setOnClickListener {
            findNavController().navigate(R.id.action_more_to_terminal)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
