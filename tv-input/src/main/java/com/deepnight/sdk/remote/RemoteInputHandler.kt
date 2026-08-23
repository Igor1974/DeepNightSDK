package com.deepnight.sdk.remote

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent

/**
 * DEEP NIGHT SDK - Remote Input Handler
 * Handles specialized TV remote interactions like long presses.
 */
class RemoteInputHandler(
    private val longPressTimeout: Long = 600L,
    private val onLongPress: (Int) -> Unit,
    private val onShortPress: (Int) -> Unit = {}
) {
    private val handler = Handler(Looper.getMainLooper())
    private var longPressJob: Runnable? = null
    private var isLongPressTriggered = false

    /**
     * Call this from onKeyEvent or dispatchKeyEvent.
     * @return true if the event was handled.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // Only handle specific keys for long press (e.g. BACK, HOME)
        if (keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_HOME) {
            return false
        }

        if (action == KeyEvent.ACTION_DOWN) {
            if (event.repeatCount == 0) {
                isLongPressTriggered = false
                longPressJob = Runnable {
                    isLongPressTriggered = true
                    onLongPress(keyCode)
                }
                handler.postDelayed(longPressJob!!, longPressTimeout)
            }
            return true
        }

        if (action == KeyEvent.ACTION_UP) {
            longPressJob?.let {
                handler.removeCallbacks(it)
                longPressJob = null
            }
            
            if (!isLongPressTriggered) {
                onShortPress(keyCode)
            }
            
            val handled = isLongPressTriggered
            isLongPressTriggered = false
            return handled || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME
        }

        return false
    }
}
