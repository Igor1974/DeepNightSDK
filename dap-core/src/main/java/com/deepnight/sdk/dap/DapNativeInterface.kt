package com.deepnight.sdk.dap

/**
 * DEEP NIGHT SDK - DAP Native Interface
 * JNI wrapper for the high-performance C++ audio engine.
 */
object DapNativeInterface {
    init {
        System.loadLibrary("dap-core")
    }

    /**
     * Processes raw audio data into frequency magnitudes.
     */
    external fun processFft(
        audioData: ByteArray,
        size: Int,
        outMagnitudes: FloatArray
    )

    /**
     * Calculates AutoEQ parameters based on frequency data.
     */
    external fun calculateAutoEq(
        fftData: FloatArray,
        size: Int,
        outEq: FloatArray
    )

    /**
     * Detects voice activity based on energy threshold.
     */
    external fun isVoiceActive(
        audioData: ByteArray,
        size: Int,
        threshold: Float
    ): Boolean

    /**
     * Runs honest FFT loops in C++ for benchmark. Returns checksum.
     */
    external fun runFftBenchmark(iterations: Int): Long
}
