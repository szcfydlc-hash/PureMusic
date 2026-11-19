package com.puremusic.player.service

import android.media.audiofx.Equalizer
import com.puremusic.player.data.model.EqualizerBand
import com.puremusic.player.data.model.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioEqualizerManager {

    private var equalizer: Equalizer? = null
    private var attachedSessionId: Int = -1

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    @Synchronized
    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == attachedSessionId) return
        releaseInternal()

        runCatching {
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq
            attachedSessionId = audioSessionId
            publishSnapshot(eq)
        }.onFailure {
            _state.value = EqualizerState(available = false)
        }
    }

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        val eq = equalizer ?: return
        runCatching {
            eq.enabled = enabled
            publishSnapshot(eq)
        }
    }

    @Synchronized
    fun setBandLevel(bandIndex: Int, levelMb: Int) {
        val eq = equalizer ?: return
        runCatching {
            val range = eq.bandLevelRange
            val clamped = levelMb.coerceIn(range[0].toInt(), range[1].toInt()).toShort()
            eq.setBandLevel(bandIndex.toShort(), clamped)
            publishSnapshot(eq, customPreset = true)
        }
    }

    @Synchronized
    fun selectPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        runCatching {
            if (presetIndex in 0 until eq.numberOfPresets) {
                eq.usePreset(presetIndex.toShort())
                publishSnapshot(eq, forcedPresetIndex = presetIndex)
            }
        }
    }

    @Synchronized
    fun release() {
        releaseInternal()
        _state.value = EqualizerState(available = false)
    }

    private fun releaseInternal() {
        runCatching { equalizer?.release() }
        equalizer = null
        attachedSessionId = -1
    }

    private fun publishSnapshot(
        eq: Equalizer,
        customPreset: Boolean = false,
        forcedPresetIndex: Int? = null,
    ) {
        val range = eq.bandLevelRange
        val minMb = range[0].toInt()
        val maxMb = range[1].toInt()
        val bandCount = eq.numberOfBands.toInt()
        val bands = (0 until bandCount).map { idx ->
            val bandShort = idx.toShort()
            EqualizerBand(
                index = idx,
                centerFreqHz = eq.getCenterFreq(bandShort) / 1000,
                levelMb = eq.getBandLevel(bandShort).toInt(),
            )
        }
        val presetCount = eq.numberOfPresets.toInt()
        val presets = (0 until presetCount).map { idx ->
            eq.getPresetName(idx.toShort())
        }
        val currentPreset = when {
            customPreset -> -1
            forcedPresetIndex != null -> forcedPresetIndex
            else -> runCatching { eq.currentPreset.toInt() }.getOrDefault(-1)
        }

        _state.value = EqualizerState(
            available = true,
            enabled = eq.enabled,
            minBandLevelMb = minMb,
            maxBandLevelMb = maxMb,
            bands = bands,
            presets = presets,
            currentPreset = currentPreset,
        )
    }
}
