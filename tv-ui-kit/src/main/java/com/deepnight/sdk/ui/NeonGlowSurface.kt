package com.deepnight.sdk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults

/**
 * DEEP NIGHT SDK - Neon Glow Surface
 * A specialized surface with a customizable glow effect.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NeonGlowSurface(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF00FFFF),
    glowRadius: Dp = 12.dp,
    isGlowEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(glowRadius)
            .drawBehind {
                if (isGlowEnabled) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().nativePaint
                        paint.color = Color.Transparent.toArgb()
                        paint.setShadowLayer(
                            glowRadius.toPx(),
                            0f, 0f,
                            glowColor.toArgb()
                        )
                        canvas.drawRoundRect(
                            0f, 0f,
                            size.width, size.height,
                            16.dp.toPx(), 16.dp.toPx(),
                            Paint()
                        )
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = SurfaceDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
