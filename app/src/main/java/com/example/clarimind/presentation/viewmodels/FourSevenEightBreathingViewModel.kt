package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FourSevenEightBreathingViewModel : ViewModel() {
    private val phases = listOf(
        "Inhale" to 4,
        "Hold" to 7,
        "Exhale" to 8
    )
    private var phaseIndex = 0
    private val _phase = MutableStateFlow(phases[0].first)
    val phase: StateFlow<String> = _phase
    private val _timeLeft = MutableStateFlow(phases[0].second)
    val timeLeft: StateFlow<Int> = _timeLeft
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun toggleBreathing() {
        _isRunning.value = !_isRunning.value
        if (_isRunning.value) startBreathing() else stopBreathing()
    }

    private fun startBreathing() {
        viewModelScope.launch {
            while (_isRunning.value) {
                val (currentPhase, duration) = phases[phaseIndex]
                _phase.value = currentPhase
                _timeLeft.value = duration
                for (i in duration downTo 1) {
                    if (!_isRunning.value) return@launch
                    _timeLeft.value = i
                    delay(1000)
                }
                phaseIndex = (phaseIndex + 1) % phases.size
            }
        }
    }

    private fun stopBreathing() {
        _isRunning.value = false
        val (currentPhase, duration) = phases[phaseIndex]
        _phase.value = currentPhase
        _timeLeft.value = duration
    }
} 