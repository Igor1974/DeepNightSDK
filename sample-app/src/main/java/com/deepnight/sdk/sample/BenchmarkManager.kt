package com.deepnight.sdk.sample

import com.deepnight.sdk.dap.DapNativeInterface
import com.deepnight.sdk.text.TextToolsNative
import kotlin.math.*
import kotlin.system.measureNanoTime

/**
 * DEEP NIGHT SDK - Benchmark Manager
 */
object BenchmarkManager {

    data class Result(val timeMs: Double, val opsPerSec: Long, val checksum: Long = 0)

    fun runNativeFftBenchmark(iterations: Int = 100000): Result {
        var checksum: Long
        val totalNano = measureNanoTime {
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

    /**
     * NEW: Data Stream Benchmark (Bitwise operations)
     */
    fun runNativeDataBenchmark(sizeMb: Int = 2, iterations: Int = 5): Result {
        val micros = DapNativeInterface.runDataBenchmark(sizeMb, iterations)
        val ms = micros / 1000.0
        val totalBytes = sizeMb * 1024L * 1024L * iterations
        val ops = totalBytes / (ms / 1000.0)
        return Result(ms, ops.toLong())
    }

    fun runKotlinDataBenchmark(sizeMb: Int = 2, iterations: Int = 5): Result {
        val size = sizeMb * 1024 * 1024
        val data = ByteArray(size) { 0xAA.toByte() }
        
        val totalNano = measureNanoTime {
            repeat(iterations) {
                for (i in 0 until size) {
                    var val8 = data[i].toInt() and 0xFF
                    val8 = ((val8 xor 0x55) + (val8 shl 1)) and 0xFF
                    val8 = ((val8 shr 1) xor (val8 shl 3)) and 0xFF
                    data[i] = val8.toByte()
                }
            }
        }
        val totalMs = totalNano / 1_000_000.0
        val totalBytes = sizeMb * 1024L * 1024L * iterations
        val ops = totalBytes / (totalMs / 1000.0)
        return Result(totalMs, ops.toLong())
    }
}
