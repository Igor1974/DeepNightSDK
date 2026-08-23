package com.deepnight.sdk.sample

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.deepnight.sdk.dap.DapNativeInterface
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages audio capture and processing via DAP Core SDK.
 * Optimized for compatibility with real TV hardware.
 */
class AudioCaptureManager {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    
    private val _magnitudes = MutableStateFlow(FloatArray(32) { 0f })
    val magnitudes = _magnitudes.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive = _isVoiceActive.asStateFlow()

    private val _micStatus = MutableStateFlow("Idle")
    val micStatus = _micStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startSimulation() {
        if (captureJob != null) stopCapture()
        _micStatus.value = "Simulation"
        captureJob = scope.launch {
            val audioData = ByteArray(1024)
            val currentMagnitudes = FloatArray(32)
            var phase = 0f
            while (isActive) {
                for (i in audioData.indices) {
                    val sample = (128 + 90 * kotlin.math.sin(phase + i * 0.15f) + 40 * kotlin.math.sin(phase * 0.7f + i * 0.08f)).toInt()
                    audioData[i] = sample.toByte()
                }
                DapNativeInterface.processFft(audioData, audioData.size, currentMagnitudes)
                _magnitudes.value = currentMagnitudes.copyOf()
                _isVoiceActive.value = true
                phase += 0.5f
                delay(40)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startCapture() {
        if (captureJob != null) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT // Standard 16-bit for better TV support
        )

        if (bufferSize <= 0) {
            _micStatus.value = "Error: Invalid buffer size"
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, // Better for remotes
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _micStatus.value = "Error: Init failed"
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _micStatus.value = "Hardware Mic Active"

            captureJob = scope.launch {
                val audioData = ShortArray(bufferSize / 2)
                val byteBuffer = ByteArray(bufferSize / 2)
                val currentMagnitudes = FloatArray(32)

                while (isActive) {
                    val read = audioRecord?.read(audioData, 0, audioData.size) ?: 0
                    if (read > 0) {
                        // Convert 16-bit PCM to 8-bit for our simplified FFT engine demo
                        for (i in 0 until read) {
                            byteBuffer[i] = ((audioData[i] + 32768) shr 8).toByte()
                        }
                        DapNativeInterface.processFft(byteBuffer, read, currentMagnitudes)
                        _isVoiceActive.value = DapNativeInterface.isVoiceActive(byteBuffer, read, 0.05f)
                        _magnitudes.value = currentMagnitudes.copyOf()
                    }
                    delay(30)
                }
            }
        } catch (e: Exception) {
            _micStatus.value = "Error: ${e.message}"
            Log.e("AudioCapture", "Error starting capture", e)
        }
    }

    fun stopCapture() {
        captureJob?.cancel()
        captureJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _micStatus.value = "Idle"
        _magnitudes.value = FloatArray(32) { 0f }
        _isVoiceActive.value = false
    }
}
