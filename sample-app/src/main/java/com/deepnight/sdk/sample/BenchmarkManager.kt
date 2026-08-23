package com.deepnight.sdk.sample

import com.deepnight.sdk.dap.DapNativeInterface
import com.deepnight.sdk.text.TextToolsNative
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.system.measureNanoTime

/**
 * DEEP NIGHT SDK - Benchmark Manager
 */
object BenchmarkManager {

    data class Result(val timeMs: Double, val opsPerSec: Long, val checksum: Long = 0)

    fun runNativeFftBenchmark(iterations: Int = 100000): Result {
        var checksum: Long
        val totalNano = measureNanoTime {
            // Processing happens in Native, returning checksum to prevent optimization
            checksum = DapNativeInterface.runFftBenchmark(iterations)
        }
        val totalMs = totalNano / 1_000_000.0
        val ops = iterations / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong(), checksum)
    }

    fun runKotlinFftBenchmark(iterations: Int = 100000): Result {
        val bufferSize = 1024
        val audioData = FloatArray(bufferSize) { sin(it * 0.1f) }
        var checksum = 0.0
        
        val totalNano = measureNanoTime {
            repeat(iterations) {
                var idx = 0
                repeat(32) {
                    var sum = 0.0f
                    repeat(32) {
                        val v = audioData[idx++]
                        sum += sqrt(abs(v * cos(v)))
                    }
                    checksum += sum * 0.03125f
                }
                audioData[iterations % bufferSize] += 0.001f
            }
        }
        val totalMs = totalNano / 1_000_000.0
        val ops = iterations / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong(), (checksum * 100.0).toLong())
    }

    fun runMathNativeBenchmark(iterations: Int = 1000000): Result {
        var checksum: Long
        val totalNano = measureNanoTime {
            checksum = TextToolsNative.runHeavyBenchmark(iterations)
        }
        val totalMs = totalNano / 1_000_000.0
        val ops = iterations / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong(), checksum)
    }

    fun runMathKotlinBenchmark(iterations: Int = 1000000): Result {
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
