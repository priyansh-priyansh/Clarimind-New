package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BoxBreathingViewModel : ViewModel() {
    private val phases = listOf("Inhale", "Hold", "Exhale", "Hold")
    private val phaseDuration = 4 // seconds
    private val _phase = MutableStateFlow(phases[0])
    val phase: StateFlow<String> = _phase
    private val _timeLeft = MutableStateFlow(phaseDuration)
    val timeLeft: StateFlow<Int> = _timeLeft
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    private var phaseIndex = 0

    fun toggleBreathing() {
        _isRunning.value = !_isRunning.value
        if (_isRunning.value) startBreathing() else stopBreathing()
    }

    private fun startBreathing() {
        viewModelScope.launch {
            while (_isRunning.value) {
                _phase.value = phases[phaseIndex]
                _timeLeft.value = phaseDuration
                for (i in phaseDuration downTo 1) {
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
        _timeLeft.value = phaseDuration
        _phase.value = phases[phaseIndex]
    }
} 