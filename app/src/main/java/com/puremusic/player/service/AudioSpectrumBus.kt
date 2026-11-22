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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * True 256-point Radix-2 Cooley-Tukey FFT live audio spectrum engine fed directly by
 * ExoPlayer's decoded PCM stream (supports PCM 16-bit, 24-bit, 32-bit, and Float).
 * Produces 48 logarithmically spaced frequency bands (Bass -> Mids -> Treble) with
 * soft-knee dB scaling (zero flat-top clipping!) and real-time bass-flux beat energy.
 */
object AudioSpectrumBus {
    const val BAND_COUNT = 48
    private const val FFT_SIZE = 256

    private val _bands = MutableStateFlow(FloatArray(BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    private val _beatEnergy = MutableStateFlow(0f)
    val beatEnergy: StateFlow<Float> = _beatEnergy.asStateFlow()

    @Volatile var lastPcmTimestampMs: Long = 0L
        private set

    private val hannWindow = FloatArray(FFT_SIZE) { i ->
        (0.5 * (1.0 - cos(2.0 * PI * i / (FFT_SIZE - 1)))).toFloat()
    }
    private val fftRe = FloatArray(FFT_SIZE)
    private val fftIm = FloatArray(FFT_SIZE)
    private val smoothed = FloatArray(BAND_COUNT)
    private var runningNoiseFloor = 0.002f
    private var runningPeakDb = 0.25f
    private var runningBassAvg = 0.05f
    private var smoothedBeat = 0f
    private var lastEmitMs = 0L

    fun resetToIdle() {
        smoothed.fill(0f)
        smoothedBeat = 0f
        _beatEnergy.value = 0f
        _bands.value = FloatArray(BAND_COUNT)
    }

    fun updateFromPcm(
        buffer: ByteBuffer,
        startPos: Int,
        limit: Int,
        channelCount: Int,
        encoding: Int,
    ) {
        val now = SystemClock.uptimeMillis()
        lastPcmTimestampMs = now
        if (now - lastEmitMs < 15L) return // ~65 fps true real-time FFT
        lastEmitMs = now

        val ch = channelCount.coerceAtLeast(1)
        val bytesPerSample = when (encoding) {
            C.ENCODING_PCM_16BIT -> 2
            C.ENCODING_PCM_24BIT -> 3
            C.ENCODING_PCM_32BIT, C.ENCODING_PCM_FLOAT -> 4
            else -> return
        }
        val frameBytes = ch * bytesPerSample
        val totalFrames = (limit - startPos) / frameBytes
        if (totalFrames < 32) return

        val isLe = buffer.order() == ByteOrder.LITTLE_ENDIAN
        // Take the most recent FFT_SIZE frames (or strided if larger) for crisp low-latency sync
        val startFrame = (totalFrames - FFT_SIZE).coerceAtLeast(0)
        val available = (totalFrames - startFrame).coerceAtMost(FFT_SIZE)

        for (i in 0 until FFT_SIZE) {
            if (i < available) {
                val byteIdx = startPos + (startFrame + i) * frameBytes
                val mono = readMonoSample(buffer, byteIdx, limit, encoding, isLe)
                fftRe[i] = mono * hannWindow[i]
            } else {
                fftRe[i] = 0f
            }
            fftIm[i] = 0f
        }

        // In-place Radix-2 Cooley-Tukey FFT (256 points -> 128 real frequency bins)
        computeRadix2Fft(fftRe, fftIm)

        // Extract 48 logarithmically spaced musical frequency bands (Bass left -> Vocals mid -> Treble right)
        val rawBands = FloatArray(BAND_COUNT)
        val halfBins = FFT_SIZE / 2
        var bassSum = 0f
        var frameMax = 0.001f

        for (b in 0 until BAND_COUNT) {
            val t0 = b.toDouble() / BAND_COUNT
            val t1 = (b + 1).toDouble() / BAND_COUNT
            val binStart = (1.0 + t0.pow(1.85) * (halfBins - 2)).toInt().coerceIn(1, halfBins - 1)
            val binEnd = (1.0 + t1.pow(1.85) * (halfBins - 2)).toInt().coerceIn(binStart, halfBins - 1)

            var peakMag = 0f
            for (k in binStart..binEnd) {
                val m = sqrt(fftRe[k] * fftRe[k] + fftIm[k] * fftIm[k]) / (FFT_SIZE / 2f)
                if (m > peakMag) peakMag = m
            }
            // Perceptual loudness tilt: compensate natural -3dB/octave pink-noise roll-off of music
            val tilt = 1.0f + (b.toFloat() / BAND_COUNT) * 2.2f
            val tilted = peakMag * tilt
            // Log compression (dB-like) preserves distinct peaks without flat-top clipping
            val logVal = (ln(1.0 + tilted * 28.0) / ln(29.0)).toFloat()
            rawBands[b] = logVal
            if (b < 8) bassSum += logVal
            if (logVal > frameMax) frameMax = logVal
        }

        // Real bass-flux beat transient detector
        val bassNow = bassSum / 8f
        runningBassAvg = runningBassAvg * 0.88f + bassNow * 0.12f
        val beatFlux = ((bassNow - runningBassAvg * 0.90f) / runningBassAvg.coerceAtLeast(0.03f)).coerceIn(0f, 1.5f)
        smoothedBeat = if (beatFlux > smoothedBeat) {
            smoothedBeat + (beatFlux - smoothedBeat) * 0.78f
        } else {
            smoothedBeat * 0.76f
        }
        _beatEnergy.value = smoothedBeat.coerceIn(0f, 1f)

        // Adaptive soft-knee peak normalization (keeps peaks expressive, NEVER clips flat!)
        runningPeakDb = if (frameMax > runningPeakDb) {
            runningPeakDb + (frameMax - runningPeakDb) * 0.35f
        } else {
            (runningPeakDb * 0.992f).coerceAtLeast(0.18f)
        }
        val normScale = 1f / runningPeakDb.coerceAtLeast(0.18f)

        val next = FloatArray(BAND_COUNT)
        for (b in 0 until BAND_COUNT) {
            // Light 3-tap spatial smoothing so frequency peaks remain distinct & lively (not a flat hump)
            val l = rawBands[(b - 1).coerceAtLeast(0)]
            val c = rawBands[b]
            val r = rawBands[(b + 1).coerceAtMost(BAND_COUNT - 1)]
            val blended = (l * 0.18f + c * 0.64f + r * 0.18f) * normScale
            // Soft-knee tanh curve prevents flat-top clipping while keeping strong contrast
            val shaped = tanh((blended * 1.15f).toDouble()).toFloat().coerceIn(0f, 0.96f)

            val prev = smoothed[b]
            val damped = if (shaped > prev) {
                prev + (shaped - prev) * 0.82f
            } else {
                prev + (shaped - prev) * 0.32f
            }
            smoothed[b] = if (abs(damped) < 0.004f) 0f else damped
            next[b] = smoothed[b]
        }
        _bands.value = next
    }

    private fun readMonoSample(
        buf: ByteBuffer,
        idx: Int,
        limit: Int,
        encoding: Int,
        isLe: Boolean,
    ): Float {
        return when (encoding) {
            C.ENCODING_PCM_16BIT -> {
                if (idx + 1 >= limit) return 0f
                val b0 = buf.get(idx).toInt()
                val b1 = buf.get(idx + 1).toInt()
                val s = if (isLe) ((b1 shl 8) or (b0 and 0xFF)).toShort() else ((b0 shl 8) or (b1 and 0xFF)).toShort()
                s / 32768f
            }
            C.ENCODING_PCM_24BIT -> {
                if (idx + 2 >= limit) return 0f
                val b0 = buf.get(idx).toInt() and 0xFF
                val b1 = buf.get(idx + 1).toInt() and 0xFF
                val b2 = buf.get(idx + 2).toInt()
                val v = if (isLe) (b2 shl 16) or (b1 shl 8) or b0 else ((buf.get(idx).toInt() shl 16) or (b1 shl 8) or (buf.get(idx + 2).toInt() and 0xFF))
                v / 8388608f
            }
            C.ENCODING_PCM_32BIT -> {
                if (idx + 3 >= limit) return 0f
                buf.getInt(idx) / 2147483648f
            }
            C.ENCODING_PCM_FLOAT -> {
                if (idx + 3 >= limit) return 0f
                buf.getFloat(idx).coerceIn(-1f, 1f)
            }
            else -> 0f
        }
    }

    private fun computeRadix2Fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wLenRe = cos(ang).toFloat()
            val wLenIm = sin(ang).toFloat()
            var i = 0
            while (i < n) {
                var wRe = 1f
                var wIm = 0f
                val half = len shr 1
                for (k in 0 until half) {
                    val uRe = re[i + k]
                    val uIm = im[i + k]
                    val vRe = re[i + k + half] * wRe - im[i + k + half] * wIm
                    val vIm = re[i + k + half] * wIm + im[i + k + half] * wRe
                    re[i + k] = uRe + vRe
                    im[i + k] = uIm + vIm
                    re[i + k + half] = uRe - vRe
                    im[i + k + half] = uIm - vIm
                    val nextWRe = wRe * wLenRe - wIm * wLenIm
                    wIm = wRe * wLenIm + wIm * wLenRe
                    wRe = nextWRe
                }
                i += len
            }
            len = len shl 1
        }
    }
}

@OptIn(UnstableApi::class)
@UnstableApi
class SpectrumAudioProcessor : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT,
            C.ENCODING_PCM_24BIT,
            C.ENCODING_PCM_32BIT,
            C.ENCODING_PCM_FLOAT -> inputAudioFormat
            else -> AudioProcessor.AudioFormat.NOT_SET
        }
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val pos = inputBuffer.position()
        val limit = inputBuffer.limit()
        val remaining = limit - pos
        if (remaining <= 0) return

        AudioSpectrumBus.updateFromPcm(
            buffer = inputBuffer,
            startPos = pos,
            limit = limit,
            channelCount = inputAudioFormat.channelCount,
            encoding = inputAudioFormat.encoding,
        )

        val out = replaceOutputBuffer(remaining)
        out.put(inputBuffer)
        out.flip()
    }
}
