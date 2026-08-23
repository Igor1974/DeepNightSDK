package com.deepnight.sdk.sample

import com.deepnight.sdk.dap.DapNativeInterface
import com.deepnight.sdk.text.TextToolsNative
import kotlin.math.*
import kotlin.system.measureNanoTime

/**
 * DEEP NIGHT SDK - Benchmark Manager
 */
object BenchmarkManager {

    data class Result(val timeMs: Double, val opsPerSec: Long)

    fun runNativeFftBenchmark(iterations: Int = 500000): Result {
        val micros = DapNativeInterface.runFftBenchmark(iterations)
        val ms = micros / 1000.0
        val ops = iterations / (ms / 1000.0)
        return Result(ms, ops.toLong())
    }

    fun runKotlinFftBenchmark(iterations: Int = 500000): Result {
        val bufferSize = 1024
        val audioData = FloatArray(bufferSize) { sin(it * 0.1f) }
        val magnitudes = FloatArray(32)
        
        val totalNano = measureNanoTime {
            repeat(iterations) {
                var idx = 0
                for (b in 0 until 32) {
                    var sum = 0.0f
                    for (j in 0 until 32) {
                        val v = audioData[idx++]
                        sum += sqrt(abs(v * cos(v)))
                    }
                    magnitudes[b] = sum * 0.03125f
                }
            }
        }
        val totalMs = totalNano / 1_000_000.0
        val ops = iterations / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong())
    }

    fun runMathNativeBenchmark(iterations: Int = 5000000): Result {
        val micros = TextToolsNative.runHeavyBenchmark(iterations)
        val ms = micros / 1000.0
        val ops = iterations / (ms / 1000.0)
        return Result(ms, ops.toLong())
    }

    fun runMathKotlinBenchmark(iterations: Int = 5000000): Result {
        val totalNano = measureNanoTime {
            var dummy = 0.0
            for (i in 0 until iterations) {
                val x = i.toDouble() * 0.001
                dummy += sin(x) * cos(x * 1.5) + sqrt(abs(tan(x * 0.5)))
                if (dummy > 1e10) dummy = 0.0
            }
        }
        val totalMs = totalNano / 1_000_000.0
        val ops = iterations / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong())
    }
}
