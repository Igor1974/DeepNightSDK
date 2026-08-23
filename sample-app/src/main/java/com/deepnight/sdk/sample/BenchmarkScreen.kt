package com.deepnight.sdk.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
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
    
    var nativeMathResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    var kotlinMathResult by remember { mutableStateOf<BenchmarkManager.Result?>(null) }
    
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

        Text(text = "1. Audio Processing (100k FFT Loops)", fontSize = 16.sp, color = Color.Cyan)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BenchmarkCard(title = "Kotlin", result = kotlinFftResult)
            BenchmarkCard(title = "Native DAP Core", result = nativeFftResult, highlight = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "2. High-Precision Math (1M Iterations)", fontSize = 16.sp, color = Color.Cyan)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BenchmarkCard(title = "Kotlin", result = kotlinMathResult)
            BenchmarkCard(title = "Native Engine", result = nativeMathResult, highlight = true)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (nativeMathResult != null && kotlinMathResult != null && nativeFftResult != null && kotlinFftResult != null) {
            val speedupMath = (kotlinMathResult!!.timeMs / nativeMathResult!!.timeMs).coerceAtLeast(1.0)
            val speedupFft = (kotlinFftResult!!.timeMs / nativeFftResult!!.timeMs).coerceAtLeast(1.0)
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NeonGlowSurface(glowColor = Color.Green) {
                    Text(
                        text = "MATH: ${"%.1f".format(speedupMath)}X FASTER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
                NeonGlowSurface(glowColor = Color.Cyan) {
                    Text(
                        text = "AUDIO: ${"%.1f".format(speedupFft)}X FASTER",
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
                    
                    kotlinMathResult = withContext(Dispatchers.Default) { BenchmarkManager.runMathKotlinBenchmark() }
                    nativeMathResult = withContext(Dispatchers.Default) { BenchmarkManager.runMathNativeBenchmark() }
                    isRunning = false
                }
            }, enabled = !isRunning) {
                Text(text = if (isRunning) "Running Stress Tests..." else "Start Benchmark")
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
            .width(180.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 12.sp, color = if (highlight) Color.Cyan else Color.White)
        if (result != null) {
            val timeText = if (result.timeMs < 0.01) "< 0.01 ms" else "${"%.2f".format(result.timeMs)} ms"
            Text(text = timeText, fontSize = 16.sp, color = Color.White)
            Text(text = "${result.opsPerSec / 1000}k ops/sec", fontSize = 10.sp, color = Color.Gray)
        } else {
            Text(text = "--", fontSize = 16.sp, color = Color.Gray)
        }
    }
}
