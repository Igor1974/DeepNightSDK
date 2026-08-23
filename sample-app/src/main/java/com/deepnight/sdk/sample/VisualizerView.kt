package com.deepnight.sdk.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Premium Multi-color Spectrum Visualizer for DeepNight SDK.
 */
@Composable
fun VisualizerView(magnitudes: FloatArray, isVoiceActive: Boolean) {
    // Cyberpunk color palette
    val colors = listOf(
        Color(0xFF00FFFF), // Cyan
        Color(0xFF8A2BE2), // BlueViolet
        Color(0xFFFF00FF)  // Magenta
    )
    
    val inactiveColor = Color.Gray.copy(alpha = 0.3f)

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(horizontal = 4.dp)) {
        
        val width = size.width
        val height = size.height
        val barCount = magnitudes.size
        val gap = 4.dp.toPx()
        val barWidth = (width - (gap * (barCount - 1))) / barCount
        
        magnitudes.forEachIndexed { index, magnitude ->
            // Apply non-linear boost for visual impact
            val boostedMagnitude = kotlin.math.sqrt(magnitude.toDouble()).toFloat()
            val barHeight = (boostedMagnitude * height * 0.95f).coerceIn(8f, height)
            val x = index * (barWidth + gap)
            
            drawRoundRect(
                brush = if (isVoiceActive) {
                    Brush.verticalGradient(
                        colors = colors,
                        startY = height,
                        endY = height - barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(inactiveColor, inactiveColor.copy(alpha = 0.1f))
                    )
                },
                topLeft = Offset(x, height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
