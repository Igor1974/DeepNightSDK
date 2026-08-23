package com.deepnight.sdk.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.deepnight.sdk.ai.CommandRegistry
import com.deepnight.sdk.dap.DapNativeInterface
import com.deepnight.sdk.focus.FocusEngine
import com.deepnight.sdk.remote.RemoteInputHandler
import com.deepnight.sdk.text.TextToolsNative
import com.deepnight.sdk.ui.DeepNightCard
import com.deepnight.sdk.ui.NeonGlowSurface

class MainActivity : ComponentActivity() {
    private val audioManager = AudioCaptureManager()
    private lateinit var remoteInputHandler: RemoteInputHandler
    private lateinit var commandRegistry: CommandRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        commandRegistry = CommandRegistry(this)
        commandRegistry.registerCommand("открой кино", "ACTION_OPEN_MOVIES")
        commandRegistry.registerCommand("выключи экран", "ACTION_SCREEN_OFF")
        
        remoteInputHandler = RemoteInputHandler(
            onLongPress = { 
                if (it == KeyEvent.KEYCODE_BACK) {
                    Toast.makeText(this, "LONG PRESS BACK: Showing Recents (Simulated)", Toast.LENGTH_LONG).show()
                }
            },
            onShortPress = { }
        )

        setContent {
            var currentScreen by remember { mutableStateOf("home") }

            if (currentScreen == "home") {
                SampleScreen(onNavigate = { currentScreen = it })
            } else if (currentScreen == "benchmark") {
                BenchmarkScreen(onBack = { currentScreen = "home" })
            } else {
                FeatureDetailScreen(feature = currentScreen, onBack = { currentScreen = "home" })
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return remoteInputHandler.handleKeyEvent(event) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return remoteInputHandler.handleKeyEvent(event) || super.onKeyUp(keyCode, event)
    }

    @Composable
    fun SampleScreen(onNavigate: (String) -> Unit) {
        val context = LocalContext.current
        val focusRegistry = FocusEngine.rememberFocusRegistry()
        val items = remember { listOf("FFT Engine", "AutoEQ", "Stemming", "AI Commands", "Clean Title", "Benchmark") }
        
        var isVisualizerEnabled by remember { mutableStateOf(false) }
        val magnitudes by audioManager.magnitudes.collectAsState()
        val isVoiceActive by audioManager.isVoiceActive.collectAsState()
        val micStatus by audioManager.micStatus.collectAsState()

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                isVisualizerEnabled = true
                audioManager.startCapture()
            } else {
                audioManager.startSimulation()
                isVisualizerEnabled = true
            }
        }

        DisposableEffect(Unit) {
            onDispose { audioManager.stopCapture() }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), // Reduced padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DeepNight SDK Premium Demo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Mic Status: $micStatus", color = if (micStatus.contains("Error")) Color.Red else Color.Cyan, fontSize = 10.sp)
            
            Spacer(modifier = Modifier.height(10.dp)) // Reduced spacer

            NeonGlowSurface(glowColor = if (isVoiceActive) Color.Cyan else Color.Gray) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(500.dp)) {
                    Text(
                        text = "Native Text Tools: ${TextToolsNative.stemWord("Программирование")}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    if (isVisualizerEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        VisualizerView(magnitudes = magnitudes, isVoiceActive = isVoiceActive)
                    } else {
                        Box(Modifier.height(110.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Press 'FFT Engine' to start Visualizer", color = Color.DarkGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
            ) {
                items(items) { item ->
                    val fr = focusRegistry.get(item)
                    DeepNightCard(
                        onClick = { 
                            when (item) {
                                "Benchmark" -> onNavigate("benchmark")
                                "AutoEQ", "AI Commands", "Clean Title" -> onNavigate(item)
                                "FFT Engine" -> {
                                    if (isVisualizerEnabled) {
                                        audioManager.stopCapture()
                                        isVisualizerEnabled = false
                                    } else {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            audioManager.startCapture()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        isVisualizerEnabled = true
                                    }
                                }
                                else -> Toast.makeText(this@MainActivity, "Feature: $item", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(85.dp), // Reduced height
                        focusRequester = fr
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = item, fontSize = 14.sp, color = if (item == "FFT Engine" && isVisualizerEnabled) Color.Cyan else Color.White)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FeatureDetailScreen(feature: String, onBack: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = feature, fontSize = 32.sp, color = Color.Cyan, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            when (feature) {
                "AutoEQ" -> {
                    Text("AutoEQ algorithm balances spectral magnitudes in real-time.", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    val input = floatArrayOf(0.1f, 0.8f, 0.2f, 0.5f, 0.1f)
                    val output = FloatArray(5)
                    DapNativeInterface.calculateAutoEq(input, 5, output)
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column {
                            Text("Original", color = Color.Gray)
                            input.forEach { Text("%.2f".format(it), color = Color.White) }
                        }
                        Column {
                            Text("Balanced Gain", color = Color.Gray)
                            output.forEach { Text("%.2fx".format(it), color = Color.Green) }
                        }
                    }
                }
                "AI Commands" -> {
                    Text("Local Phrase-to-Action Mapping (Zero Cloud)", color = Color.White)
                    Spacer(Modifier.height(24.dp))
                    val testPhrase = "Открой кино"
                    val action = commandRegistry.findAction(testPhrase)
                    Text("Input: \"$testPhrase\"", color = Color.White)
                    Text("Detected Action: $action", color = Color.Cyan, fontWeight = FontWeight.Bold)
                }
                "Clean Title" -> {
                    val raw = "Мстители: Финал [2019] 4K HDR.mkv"
                    val clean = TextToolsNative.cleanTitle(raw)
                    val q = TextToolsNative.extractQuality(raw)
                    val y = TextToolsNative.extractYear(raw)
                    
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Raw: $raw", color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("Cleaned: $clean", color = Color.White, fontSize = 20.sp)
                        Text("Quality: $q", color = Color.Cyan)
                        Text("Year: $y", color = Color.Cyan)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = onBack) { Text("Back to Menu") }
        }
    }
}
