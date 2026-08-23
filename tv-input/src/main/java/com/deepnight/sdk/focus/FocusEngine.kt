package com.deepnight.sdk.focus

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

/**
 * DEEP NIGHT SDK - Focus Engine
 * Provides robust focus management for Android TV.
 */
object FocusEngine {
    private const val TAG = "DeepNightFocus"

    /**
     * Safely requests focus for a FocusRequester.
     */
    fun FocusRequester.safeRequestFocus(tag: String = "Unknown") {
        try {
            this.requestFocus()
        } catch (e: Exception) {
            Log.w(TAG, "[$tag] Focus request failed: ${e.message}")
        }
    }

    /**
     * Helper for managing FocusRequesters in a list/grid.
     */
    class FocusRegistry {
        private val requesters = mutableMapOf<String, FocusRequester>()

        fun get(key: String): FocusRequester {
            return requesters.getOrPut(key) { FocusRequester() }
        }

        fun register(key: String, requester: FocusRequester) {
            requesters[key] = requester
        }

        fun remove(key: String) {
            requesters.remove(key)
        }

        fun requestFocus(key: String, tag: String = "Registry") {
            requesters[key]?.safeRequestFocus(tag)
        }
    }

    @Composable
    fun rememberFocusRegistry(): FocusRegistry {
        return remember { FocusRegistry() }
    }

    @Composable
    fun RegisterFocus(key: String, registry: FocusRegistry, requester: FocusRequester) {
        DisposableEffect(key) {
            registry.register(key, requester)
            onDispose {
                registry.remove(key)
            }
        }
    }
}
