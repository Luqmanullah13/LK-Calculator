package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class FeedbackManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val clickPcm: ShortArray by lazy {
        val sampleRate = 22050
        val numSamples = (sampleRate * 0.014).toInt() // 14ms crisp tactile click
        ShortArray(numSamples) { i ->
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * 340.0)
            val wave = sin(2.0 * PI * 980.0 * t) * 0.65 + sin(2.0 * PI * 2100.0 * t) * 0.35
            (wave * envelope * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
    }

    fun triggerFeedback(vibrate: Boolean, sound: Boolean, heavy: Boolean = false) {
        if (vibrate) {
            vibrateClick(heavy)
        }
        if (sound) {
            playClickSound()
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateClick(heavy: Boolean) {
        try {
            val durationMs = if (heavy) 36L else 16L
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (heavy) 200 else 110
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                vibrator.vibrate(durationMs)
            }
        } catch (_: Throwable) {
            // Safe no-op in environments without vibrator hardware (e.g. unit tests)
        }
    }

    private fun playClickSound() {
        scope.launch {
            try {
                val sampleRate = 22050
                val bufferSizeBytes = clickPcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .build()

                track.write(clickPcm, 0, clickPcm.size)
                track.play()
                kotlinx.coroutines.delay(25)
                track.stop()
                track.release()
            } catch (_: Throwable) {
                // Safe no-op in headless/Robolectric environments
            }
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
