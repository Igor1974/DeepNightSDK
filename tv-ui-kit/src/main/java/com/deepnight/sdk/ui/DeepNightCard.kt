package com.deepnight.sdk.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface

/**
 * DEEP NIGHT SDK - DeepNight Card
 * A premium TV card component with focus animations.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DeepNightCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    // Reduced scale for better compatibility with tight grids
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1.0f)

    Box(
        modifier = modifier
            .padding(12.dp) // Force content to be smaller than the grid cell
            .zIndex(if (isFocused) 10f else 1f)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color.White.copy(alpha = 0.15f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))),
                focusedBorder = Border(BorderStroke(2.dp, Color.Cyan))
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
