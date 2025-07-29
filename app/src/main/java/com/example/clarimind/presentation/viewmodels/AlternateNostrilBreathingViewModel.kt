package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlternateNostrilBreathingViewModel : ViewModel() {
    private val phases = listOf(
        "Inhale" to "Left",
        "Exhale" to "Right",
        "Inhale" to "Right",
        "Exhale" to "Left"
    )
    private val phaseDuration = 4 // seconds for each phase
    private var phaseIndex = 0
    private val _phase = MutableStateFlow(phases[0].first)
    val phase: StateFlow<String> = _phase
    private val _side = MutableStateFlow(phases[0].second)
    val side: StateFlow<String> = _side
    private val _timeLeft = MutableStateFlow(phaseDuration)
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
                val (currentPhase, currentSide) = phases[phaseIndex]
                _phase.value = currentPhase
                _side.value = currentSide
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
        val (currentPhase, currentSide) = phases[phaseIndex]
        _phase.value = currentPhase
        _side.value = currentSide
        _timeLeft.value = phaseDuration
    }
} 