package com.puremusic.player.service

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Zero-permission live audio spectrum bus fed directly by ExoPlayer's decoded PCM-16 stream.
 * Uses Automatic Gain Control (AGC) + spatial smoothing so both quiet ambient tracks and loud
 * tracks drive expressive, fluid 48-band spectrum motion in [0f, 1f].
 */
object AudioSpectrumBus {
    const val BAND_COUNT = 48

    private val _bands = MutableStateFlow(FloatArray(BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    private val smoothed = FloatArray(BAND_COUNT)
    private var runningPeak = 0.08f
    private var lastEmitMs = 0L

    fun resetToIdle() {
        smoothed.fill(0f)
        runningPeak = 0.08f
        _bands.value = FloatArray(BAND_COUNT)
    }

    fun updateFromPcm16(buffer: ByteBuffer, startPos: Int, limit: Int, channelCount: Int) {
        val now = SystemClock.uptimeMillis()
        if (now - lastEmitMs < 20L) return // ~50 fps fluid updates
        lastEmitMs = now

        val channels = channelCount.coerceAtLeast(1)
        val frameBytes = channels * 2
        val totalFrames = (limit - startPos) / frameBytes
        if (totalFrames < 16) return

        val sampleCount = totalFrames.coerceAtMost(256)
        val stride = (totalFrames / sampleCount).coerceAtLeast(1)
        val mono = FloatArray(sampleCount)
        val isLittleEndian = buffer.order() == ByteOrder.LITTLE_ENDIAN
        for (i in 0 until sampleCount) {
            val byteIdx = startPos + i * stride * frameBytes
            if (byteIdx + 1 >= limit) break
            val b0 = buffer.get(byteIdx).toInt()
            val b1 = buffer.get(byteIdx + 1).toInt()
            val s = if (isLittleEndian) {
                ((b1 shl 8) or (b0 and 0xFF)).toShort()
            } else {
                ((b0 shl 8) or (b1 and 0xFF)).toShort()
            }
            // Hann window for clean spectral peaks
            val hann = 0.5f * (1f - cos((2.0 * Math.PI * i) / (sampleCount - 1)).toFloat())
            mono[i] = (s / 32768f) * hann
        }

        val rawMags = FloatArray(BAND_COUNT)
        var frameMax = 0.001f
        val twoPiOverN = (2.0 * Math.PI) / sampleCount
        for (b in 0 until BAND_COUNT) {
            val t = b.toDouble() / (BAND_COUNT - 1)
            // Log-spaced musical frequency bins from sub-bass to crisp treble
            val freqBin = 1.0 + t.pow(1.65) * 48.0
            var re = 0.0
            var im = 0.0
            var idx = 0
            while (idx < sampleCount) {
                val angle = twoPiOverN * freqBin * idx
                val v = mono[idx].toDouble()
                re += v * cos(angle)
                im += v * sin(angle)
                idx += 2
            }
            val mag = (sqrt(re * re + im * im) / (sampleCount / 2.0)).toFloat()
            // Gentle perceptual tilt so mids & highs remain lively alongside bass
            val weighted = mag * (1.0f + t.toFloat() * 1.4f)
            rawMags[b] = weighted
            if (weighted > frameMax) frameMax = weighted
        }

        // Automatic Gain Control (AGC): fast rise, slow decay so ambient tracks stay expressive
        runningPeak = if (frameMax > runningPeak) {
            runningPeak + (frameMax - runningPeak) * 0.45f
        } else {
            (runningPeak * 0.965f).coerceAtLeast(0.025f)
        }
        val normScale = 1f / runningPeak.coerceAtLeast(0.025f)

        // Spatial 3-tap smoothing so bars form sculptural undulating waves
        val next = FloatArray(BAND_COUNT)
        for (b in 0 until BAND_COUNT) {
            val left = rawMags[(b - 1).coerceAtLeast(0)]
            val cur = rawMags[b]
            val right = rawMags[(b + 1).coerceAtMost(BAND_COUNT - 1)]
            val spatial = (left * 0.22f + cur * 0.56f + right * 0.22f) * normScale
            // Perceptual compression curve (sqrt) for vivid musical dynamics
            val target = sqrt(spatial.coerceIn(0f, 1.2f)).coerceIn(0f, 1f)
            val prev = smoothed[b]
            val damped = if (target > prev) {
                prev + (target - prev) * 0.65f
            } else {
                prev + (target - prev) * 0.22f
            }
            smoothed[b] = if (abs(damped) < 0.005f) 0f else damped
            next[b] = smoothed[b]
        }
        _bands.value = next
    }
}

@OptIn(UnstableApi::class)
@UnstableApi
class SpectrumAudioProcessor : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return if (inputAudioFormat.encoding == C.ENCODING_PCM_16BIT) {
            inputAudioFormat
        } else {
            AudioProcessor.AudioFormat.NOT_SET
        }
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val pos = inputBuffer.position()
        val limit = inputBuffer.limit()
        val remaining = limit - pos
        if (remaining <= 0) return

        AudioSpectrumBus.updateFromPcm16(
            buffer = inputBuffer,
            startPos = pos,
            limit = limit,
            channelCount = inputAudioFormat.channelCount,
        )

        val out = replaceOutputBuffer(remaining)
        out.put(inputBuffer)
        out.flip()
    }
}
