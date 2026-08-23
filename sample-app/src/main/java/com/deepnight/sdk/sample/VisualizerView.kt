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
 * Premium Live Spectrum Visualizer for DeepNight SDK.
 */
@Composable
fun VisualizerView(magnitudes: FloatArray, isVoiceActive: Boolean) {
    val activeColor = Color(0xFF00FFFF)
    val inactiveColor = Color.Gray.copy(alpha = 0.5f)

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(horizontal = 4.dp)) {
        
        val width = size.width
        val height = size.height
        val barCount = magnitudes.size
        val gap = 6.dp.toPx()
        val barWidth = (width - (gap * (barCount - 1))) / barCount
        
        magnitudes.forEachIndexed { index, magnitude ->
            // Apply a non-linear boost for more visual impact
            val boostedMagnitude = kotlin.math.sqrt(magnitude.toDouble()).toFloat()
            val barHeight = (boostedMagnitude * height * 0.9f).coerceIn(10f, height)
            val x = index * (barWidth + gap)
            
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isVoiceActive) listOf(activeColor, activeColor.copy(alpha = 0.2f)) 
                             else listOf(inactiveColor, inactiveColor.copy(alpha = 0.05f))
                ),
                topLeft = Offset(x, height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
