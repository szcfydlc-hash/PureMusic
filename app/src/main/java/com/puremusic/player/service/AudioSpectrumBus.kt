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
import kotlin.math.sqrt

/**
 * Zero-permission live audio spectrum bus fed directly by ExoPlayer's decoded PCM-16 stream.
 * Produces 48 smoothly damped normalized frequency-band energy values in [0f, 1f].
 */
object AudioSpectrumBus {
    const val BAND_COUNT = 48

    private val _bands = MutableStateFlow(FloatArray(BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    private val smoothed = FloatArray(BAND_COUNT)
    private var lastEmitMs = 0L

    fun resetToIdle() {
        smoothed.fill(0f)
        _bands.value = FloatArray(BAND_COUNT)
    }

    fun updateFromPcm16(buffer: ByteBuffer, startPos: Int, limit: Int, channelCount: Int) {
        val now = SystemClock.uptimeMillis()
        if (now - lastEmitMs < 32L) return // ~30 fps lightweight publish
        lastEmitMs = now

        val channels = channelCount.coerceAtLeast(1)
        val frameBytes = channels * 2
        val totalFrames = (limit - startPos) / frameBytes
        if (totalFrames < 16) return

        // Extract up to 256 mono samples evenly strided across this PCM buffer
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
            mono[i] = s / 32768f
        }

        // Compute overall RMS + lightweight Goertzel-like multi-band energy across 48 bands
        var rmsAcc = 0f
        for (v in mono) rmsAcc += v * v
        val rms = sqrt(rmsAcc / sampleCount).coerceIn(0f, 1f)

        val next = FloatArray(BAND_COUNT)
        val twoPiOverN = (2.0 * Math.PI) / sampleCount
        for (b in 0 until BAND_COUNT) {
            // Map band index logarithmically to musical frequency bins (1..64)
            val freqBin = 1.0 + (b.toDouble() / (BAND_COUNT - 1)) * (b.toDouble() / (BAND_COUNT - 1)) * 56.0
            var re = 0.0
            var im = 0.0
            val step = 2
            var idx = 0
            while (idx < sampleCount) {
                val angle = twoPiOverN * freqBin * idx
                val v = mono[idx].toDouble()
                re += v * cos(angle)
                im += v * kotlin.math.sin(angle)
                idx += step
            }
            val mag = (sqrt(re * re + im * im) / (sampleCount / step.toDouble())).toFloat()
            // Blend spectral magnitude with overall RMS for natural musical fullness
            val target = (mag * 3.8f + rms * 0.45f).coerceIn(0f, 1f)
            val prev = smoothed[b]
            val damped = if (target > prev) {
                prev + (target - prev) * 0.58f
            } else {
                prev + (target - prev) * 0.24f
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
