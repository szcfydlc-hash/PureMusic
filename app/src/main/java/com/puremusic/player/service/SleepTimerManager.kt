package com.puremusic.player.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SleepTimerManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timerJob: Job? = null

    private val _remainingMs = MutableStateFlow<Long?>(null)
    val remainingMs: StateFlow<Long?> = _remainingMs.asStateFlow()

    fun start(minutes: Int, onFinished: () -> Unit) {
        cancel()
        if (minutes <= 0) return

        val endTimeMs = System.currentTimeMillis() + minutes * 60_000L
        timerJob = scope.launch {
            while (true) {
                val left = endTimeMs - System.currentTimeMillis()
                if (left <= 0L) {
                    _remainingMs.value = null
                    onFinished()
                    break
                }
                _remainingMs.value = left
                delay(1_000L)
            }
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _remainingMs.value = null
    }
}
