package com.teckatecka.netadmin.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.teckatecka.netadmin.databinding.FragmentMoreBinding

/**
 * Fragment "Více" — obsahuje RouterOS správu, Speed Test, Monitoring a nastavení.
 */
class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Fáze 2 — navigace na RouterOS, Speed Test, Monitoring, Terminal Server Manager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
