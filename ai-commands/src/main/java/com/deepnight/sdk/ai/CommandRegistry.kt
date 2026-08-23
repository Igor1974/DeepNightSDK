package com.deepnight.sdk.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * DEEP NIGHT SDK - Command Registry
 * Manages mapping of voice phrases to system actions.
 */
class CommandRegistry(private val context: Context) {
    private val gson = Gson()
    private val storageFile = File(context.filesDispatcher, "ai_commands.json")
    
    // Internal mapping: Phrase -> Action ID
    private var commandMap = mutableMapOf<String, String>()

    init {
        loadCommands()
    }

    private val Context.filesDispatcher: File
        get() = this.filesDir

    /**
     * Registers a new command.
     */
    fun registerCommand(phrase: String, actionId: String) {
        commandMap[phrase.lowercase().trim()] = actionId
        saveCommands()
    }

    /**
     * Finds an action ID for a given phrase.
     */
    fun findAction(phrase: String): String? {
        val cleanPhrase = phrase.lowercase().trim()
        return commandMap[cleanPhrase]
    }

    /**
     * Returns all registered commands.
     */
    fun getAllCommands(): Map<String, String> = commandMap.toMap()

    private fun loadCommands() {
        if (storageFile.exists()) {
            try {
                val json = storageFile.readText()
                val type = object : TypeToken<Map<String, String>>() {}.type
                commandMap = gson.fromJson(json, type) ?: mutableMapOf()
            } catch (e: Exception) {
                commandMap = mutableMapOf()
            }
        }
    }

    private fun saveCommands() {
        try {
            val json = gson.toJson(commandMap)
            storageFile.writeText(json)
        } catch (e: Exception) {
            // Log or handle error
        }
    }
}
