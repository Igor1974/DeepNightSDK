package com.deepnight.sdk.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.deepnight.sdk.ui.NeonGlowSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BenchmarkScreen(onBack: () -> Unit) {
    var nativeFftResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    var kotlinFftResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    
    var nativeDataResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    var kotlinDataResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "DeepNight SDK: Performance Benchmark", fontSize = 24.sp, color = Color.White)
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "1. DSP Signal Analysis (100k FFT Loops)", fontSize = 16.sp, color = Color.Cyan)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BenchmarkCard(title = "Kotlin (JVM)", result = kotlinFftResult)
            BenchmarkCard(title = "Native DAP Core", result = nativeFftResult, highlight = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "2. Real-time Data Stream Processing (10MB Byte Manipulation)", fontSize = 16.sp, color = Color.Cyan)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BenchmarkCard(title = "Kotlin (JVM)", result = kotlinDataResult)
            BenchmarkCard(title = "Native Engine", result = nativeDataResult, highlight = true)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (nativeDataResult != null && kotlinDataResult != null && nativeFftResult != null && kotlinFftResult != null) {
            val speedupData = (kotlinDataResult!!.timeMs / nativeDataResult!!.timeMs).coerceAtLeast(1.0)
            val speedupFft = (kotlinFftResult!!.timeMs / nativeFftResult!!.timeMs).coerceAtLeast(1.0)
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NeonGlowSurface(glowColor = Color.Green) {
                    Text(
                        text = "DATA: ${"%.1f".format(speedupData)}X FASTER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
                NeonGlowSurface(glowColor = Color.Cyan) {
                    Text(
                        text = "DSP: ${"%.1f".format(speedupFft)}X FASTER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                scope.launch {
                    isRunning = true
                    kotlinFftResult = withContext(Dispatchers.Default) { BenchmarkManager.runKotlinFftBenchmark() }
                    nativeFftResult = withContext(Dispatchers.Default) { BenchmarkManager.runNativeFftBenchmark() }
                    
                    kotlinDataResult = withContext(Dispatchers.Default) { BenchmarkManager.runKotlinDataBenchmark() }
                    nativeDataResult = withContext(Dispatchers.Default) { BenchmarkManager.runNativeDataBenchmark() }
                    isRunning = false
                }
            }, enabled = !isRunning) {
                Text(text = if (isRunning) "Stress Testing..." else "Run B2B Benchmark")
            }

            Button(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}

@Composable
fun BenchmarkCard(title: String, result: BenchmarkManager.Result?, highlight: Boolean = false) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 12.sp, color = if (highlight) Color.Cyan else Color.White)
        if (result != null) {
            Text(text = "${"%.2f".format(result.timeMs)} ms", fontSize = 16.sp, color = Color.White)
            Text(text = "${result.opsPerSec / 1000}k ops/sec", fontSize = 10.sp, color = Color.Gray)
        } else {
            Text(text = "--", fontSize = 16.sp, color = Color.Gray)
        }
    }
}
