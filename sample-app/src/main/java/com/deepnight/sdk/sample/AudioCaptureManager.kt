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
 */
class AudioCaptureManager {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    
    private val _magnitudes = MutableStateFlow(FloatArray(32) { 0f })
    val magnitudes = _magnitudes.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive = _isVoiceActive.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("MissingPermission")
    fun startCapture() {
        if (captureJob != null) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_8BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_8BIT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioCapture", "Failed to initialize AudioRecord")
            return
        }

        audioRecord?.startRecording()

        captureJob = scope.launch {
            val audioData = ByteArray(bufferSize)
            val currentMagnitudes = FloatArray(32)

            while (isActive) {
                val read = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                if (read > 0) {
                    // Process through DAP SDK
                    DapNativeInterface.processFft(audioData, read, currentMagnitudes)
                    _isVoiceActive.value = DapNativeInterface.isVoiceActive(audioData, read, 0.05f)
                    _magnitudes.value = currentMagnitudes.copyOf()
                }
                delay(30) // ~30 FPS UI update
            }
        }
    }

    fun stopCapture() {
        captureJob?.cancel()
        captureJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        _magnitudes.value = FloatArray(32) { 0f }
    }
}
