package com.deepnight.sdk.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.tv.material3.Text
import com.deepnight.sdk.focus.FocusEngine
import com.deepnight.sdk.text.TextToolsNative
import com.deepnight.sdk.ui.DeepNightCard
import com.deepnight.sdk.ui.NeonGlowSurface

class MainActivity : ComponentActivity() {
    private val audioManager = AudioCaptureManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("home") }

            if (currentScreen == "home") {
                SampleScreen(onNavigate = { currentScreen = it })
            } else if (currentScreen == "benchmark") {
                BenchmarkScreen(onBack = { currentScreen = "home" })
            }
        }
    }

    @Composable
    fun SampleScreen(onNavigate: (String) -> Unit) {
        val context = LocalContext.current
        val focusRegistry = FocusEngine.rememberFocusRegistry()
        val items = remember { listOf("FFT Engine", "AutoEQ", "Stemming", "VAD", "Clean Title", "Benchmark") }
        
        var isVisualizerEnabled by remember { mutableStateOf(false) }
        val magnitudes by audioManager.magnitudes.collectAsState()
        val isVoiceActive by audioManager.isVoiceActive.collectAsState()

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                isVisualizerEnabled = true
                audioManager.startCapture()
            } else {
                Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        DisposableEffect(Unit) {
            onDispose { audioManager.stopCapture() }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DeepNight SDK Premium Demo", color = Color.White)
            
            Spacer(modifier = Modifier.height(16.dp))

            NeonGlowSurface(glowColor = if (isVoiceActive) Color.Cyan else Color.Gray) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Native Text Tools: ${TextToolsNative.stemWord("Программирование")}",
                        color = Color.White
                    )
                    if (isVisualizerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VisualizerView(magnitudes = magnitudes, isVoiceActive = isVoiceActive)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    val fr = focusRegistry.get(item)
                    DeepNightCard(
                        onClick = { 
                            when (item) {
                                "Benchmark" -> onNavigate("benchmark")
                                "FFT Engine", "VAD" -> {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        if (isVisualizerEnabled) {
                                            audioManager.stopCapture()
                                            isVisualizerEnabled = false
                                        } else {
                                            isVisualizerEnabled = true
                                            audioManager.startCapture()
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                else -> Toast.makeText(this@MainActivity, "Feature: $item", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(120.dp),
                        focusRequester = fr
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = item, color = if (item == "FFT Engine" && isVisualizerEnabled) Color.Cyan else Color.White)
                        }
                    }
                }
            }
        }
    }
}
