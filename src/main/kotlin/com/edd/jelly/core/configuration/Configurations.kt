package com.edd.jelly.core.configuration

import com.edd.jelly.core.JellyGame
import com.edd.jelly.core.events.Messaging
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class Configurations {

    companion object {
        private const val CONFIG_FILE = "config.yml"

        /**
         * Folder where all game resources and assets are held.
         */
        const val ASSETS_FOLDER = "assets/"

        const val MENU_LEVEL_NAME = "menu"
    }

    // Configurations have to be loaded before Guice, so can't use DI here.
    val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())

//        enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
    }

    private var messaging: Messaging? = null
    val config: Config = load()

    /**
     * Setup configuration settings for the game.
     */
    fun setup(game: JellyGame) {
        messaging = game.messaging
    }

    /**
     * Save configuration settings to file.
     */
    fun save() {
        save(config)
        messaging?.send(ConfigChangedEvent(config))
    }

    /**
     * Save configuration settings to file.
     */
    private fun save(config: Config) {
        getConfigFile().first.writeBytes(mapper.writeValueAsBytes(config))
    }

    /**
     * Load config file from classpath resources.
     */
    private fun load(): Config {
        return mapper.readValue(ClassLoader.getSystemResourceAsStream(CONFIG_FILE), Config::class.java)
    }

    private fun getConfigFile(): Pair<File, Boolean> {
        val file = File("$ASSETS_FOLDER$CONFIG_FILE")
        var created = false

        if (!file.exists()) {
            file.createNewFile()
            created = true
        }
        return Pair(file, created)
    }
}