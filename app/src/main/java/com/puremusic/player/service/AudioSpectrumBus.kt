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
 * Zero-permission beat-reactive live audio spectrum bus fed directly by ExoPlayer's decoded PCM-16 stream.
 * Computes bass-flux beat punch + 48 Gaussian-smoothed dynamic-contrast bands in [0f, 1f] so quiet
 * passages stay serene and drum kicks / vocal swells create unmistakable graceful waves.
 */
object AudioSpectrumBus {
    const val BAND_COUNT = 48

    private val _bands = MutableStateFlow(FloatArray(BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    private val _beatEnergy = MutableStateFlow(0f)
    val beatEnergy: StateFlow<Float> = _beatEnergy.asStateFlow()

    private val smoothed = FloatArray(BAND_COUNT)
    private var runningPeak = 0.06f
    private var runningBassAvg = 0.04f
    private var smoothedBeat = 0f
    private var lastEmitMs = 0L

    fun resetToIdle() {
        smoothed.fill(0f)
        runningPeak = 0.06f
        runningBassAvg = 0.04f
        smoothedBeat = 0f
        _beatEnergy.value = 0f
        _bands.value = FloatArray(BAND_COUNT)
    }

    fun updateFromPcm16(buffer: ByteBuffer, startPos: Int, limit: Int, channelCount: Int) {
        val now = SystemClock.uptimeMillis()
        if (now - lastEmitMs < 14L) return // ~70 fps snappy tracking
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
            val hann = 0.5f * (1f - cos((2.0 * Math.PI * i) / (sampleCount - 1)).toFloat())
            mono[i] = (s / 32768f) * hann
        }

        // Compute 24 musical frequency bins, then mirror gracefully around center for a sculpted ribbon feel
        val halfBands = BAND_COUNT / 2
        val rawHalf = FloatArray(halfBands)
        var frameMax = 0.001f
        var bassSum = 0f
        val twoPiOverN = (2.0 * Math.PI) / sampleCount

        for (b in 0 until halfBands) {
            val t = b.toDouble() / (halfBands - 1)
            val freqBin = 1.0 + t.pow(1.75) * 44.0
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
            val weighted = mag * (1.0f + t.toFloat() * 1.15f)
            rawHalf[b] = weighted
            if (b < 5) bassSum += weighted
            if (weighted > frameMax) frameMax = weighted
        }

        // Bass-flux beat detector: detects drum kicks & rhythm transients clearly
        val bassNow = bassSum / 5f
        runningBassAvg = runningBassAvg * 0.90f + bassNow * 0.10f
        val rawBeat = ((bassNow - runningBassAvg * 0.78f) / runningBassAvg.coerceAtLeast(0.008f)).coerceIn(0f, 1.4f)
        smoothedBeat = if (rawBeat > smoothedBeat) {
            smoothedBeat + (rawBeat - smoothedBeat) * 0.72f
        } else {
            smoothedBeat * 0.82f
        }
        _beatEnergy.value = smoothedBeat.coerceIn(0f, 1f)

        // Adaptive peak AGC with slower decay so dynamic contrast stays punchy
        runningPeak = if (frameMax > runningPeak) {
            runningPeak + (frameMax - runningPeak) * 0.38f
        } else {
            (runningPeak * 0.985f).coerceAtLeast(0.02f)
        }
        val normScale = 1f / runningPeak.coerceAtLeast(0.02f)

        // Arrange bins into a graceful organic contour (low-mid energy rich near center, shimmering highs toward edges)
        val rawFull = FloatArray(BAND_COUNT)
        for (i in 0 until BAND_COUNT) {
            val distFromCenter = abs(i - (BAND_COUNT - 1) / 2f) / ((BAND_COUNT - 1) / 2f)
            val binIdx = (distFromCenter * (halfBands - 1)).toInt().coerceIn(0, halfBands - 1)
            val norm = (rawHalf[binIdx] * normScale).coerceIn(0f, 1.25f)
            // Dynamic contrast expansion (power 1.35) + rhythmic beat boost so beats visually pop
            val beatBoost = (1f - distFromCenter * 0.40f) * smoothedBeat * 0.46f
            val contrasted = (norm.pow(1.12f) * 1.15f + beatBoost).coerceIn(0f, 1f)
            rawFull[i] = contrasted
        }

        // 5-tap Gaussian spatial smoothing for liquid, graceful ribbon curves (never spiky noise)
        val next = FloatArray(BAND_COUNT)
        for (i in 0 until BAND_COUNT) {
            val m2 = rawFull[(i - 2).coerceAtLeast(0)]
            val m1 = rawFull[(i - 1).coerceAtLeast(0)]
            val c0 = rawFull[i]
            val p1 = rawFull[(i + 1).coerceAtMost(BAND_COUNT - 1)]
            val p2 = rawFull[(i + 2).coerceAtMost(BAND_COUNT - 1)]
            val graceful = m2 * 0.10f + m1 * 0.22f + c0 * 0.36f + p1 * 0.22f + p2 * 0.10f

            val prev = smoothed[i]
            val damped = if (graceful > prev) {
                prev + (graceful - prev) * 0.84f
            } else {
                prev + (graceful - prev) * 0.34f
            }
            smoothed[i] = if (abs(damped) < 0.004f) 0f else damped
            next[i] = smoothed[i]
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
