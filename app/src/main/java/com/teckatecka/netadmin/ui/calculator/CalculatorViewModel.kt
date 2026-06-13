package com.teckatecka.netadmin.ui.calculator

import androidx.lifecycle.ViewModel
import com.teckatecka.netadmin.utils.CidrCalculator
import com.teckatecka.netadmin.utils.CidrResult
import com.teckatecka.netadmin.utils.VlsmResult
import com.teckatecka.netadmin.utils.VlsmSubnet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class CalcUiState {
    object Idle    : CalcUiState()
    object Loading : CalcUiState()
    data class CidrSuccess(val result: CidrResult)         : CalcUiState()
    data class VlsmSuccess(val results: List<VlsmResult>)  : CalcUiState()
    data class Error(val message: String)                  : CalcUiState()
}

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow<CalcUiState>(CalcUiState.Idle)
    val state: StateFlow<CalcUiState> = _state

    fun calculateCidr(input: String) {
        _state.value = CalcUiState.Loading
        _state.value = try {
            val result = CidrCalculator.calculate(input.trim())
            CalcUiState.CidrSuccess(result)
        } catch (e: Exception) {
            CalcUiState.Error(e.message ?: "Invalid input")
        }
    }

    fun calculateVlsm(baseNetwork: String, subnets: List<VlsmSubnet>) {
        _state.value = CalcUiState.Loading
        _state.value = try {
            val results = CidrCalculator.calculateVlsm(baseNetwork.trim(), subnets)
            CalcUiState.VlsmSuccess(results)
        } catch (e: Exception) {
            CalcUiState.Error(e.message ?: "Calculation error")
        }
    }

    fun reset() {
        _state.value = CalcUiState.Idle
    }
}
