package com.puremusic.player.service

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import com.puremusic.player.data.model.EqualizerBand
import com.puremusic.player.data.model.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

object AudioEqualizerManager {

    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var attachedSessionId: Int = -1
    @Volatile private var volumeNormalizationEnabled: Boolean = false
    @Volatile var preampLinearGain: Float = 1.0f
        private set

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
        runCatching {
            val le = LoudnessEnhancer(audioSessionId)
            le.setTargetGain(if (volumeNormalizationEnabled) 240 else 0)
            le.enabled = volumeNormalizationEnabled
            loudnessEnhancer = le
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
            val clamped = levelMb.coerceIn(range[0].toInt().coerceAtLeast(-1200), range[1].toInt().coerceAtMost(1000)).toShort()
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
    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        volumeNormalizationEnabled = enabled
        runCatching {
            loudnessEnhancer?.setTargetGain(if (enabled) 240 else 0)
            loudnessEnhancer?.enabled = enabled
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
        runCatching { loudnessEnhancer?.release() }
        loudnessEnhancer = null
        attachedSessionId = -1
        preampLinearGain = 1.0f
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

        // Automatic anti-clipping pre-amp headroom so positive EQ boosts NEVER overflow 0 dBFS / crackle
        val maxBoostMb = bands.maxOfOrNull { it.levelMb }?.coerceAtLeast(0) ?: 0
        preampLinearGain = if (eq.enabled && maxBoostMb > 0) {
            val attenDb = (maxBoostMb / 100.0) + 1.5
            10.0.pow(-attenDb / 20.0).toFloat().coerceIn(0.20f, 1.0f)
        } else if (eq.enabled) {
            0.90f // Gentle -1dB safety headroom whenever EQ DSP is active
        } else {
            1.0f
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
