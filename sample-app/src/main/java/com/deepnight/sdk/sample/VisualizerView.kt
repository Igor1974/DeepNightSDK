package com.deepnight.sdk.sample

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Live Spectrum Visualizer using Compose Canvas.
 */
@Composable
fun VisualizerView(magnitudes: FloatArray, isVoiceActive: Boolean) {
    val glowAlpha by animateFloatAsState(if (isVoiceActive) 0.8f else 0.3f)
    val barColor = if (isVoiceActive) Color.Cyan else Color.DarkGray

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)) {
        
        val width = size.width
        val height = size.height
        val barWidth = width / magnitudes.size
        
        magnitudes.forEachIndexed { index, magnitude ->
            val barHeight = magnitude * height
            val x = index * barWidth
            
            drawRect(
                color = barColor.copy(alpha = glowAlpha),
                topLeft = Offset(x + 2f, height - barHeight),
                size = Size(barWidth - 4f, barHeight)
            )
        }
    }
}
