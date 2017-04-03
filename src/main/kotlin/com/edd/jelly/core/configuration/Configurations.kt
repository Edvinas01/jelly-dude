package com.edd.jelly.core.configuration

import com.edd.jelly.core.JellyGame
import com.edd.jelly.core.events.Messaging
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.io.IOUtils
import java.io.File

class Configurations {

    companion object {
        private const val CONFIG_FILE = "config.yml"

        /**
         * Folder where all game resources and assets are held.
         */
        const val ASSETS_FOLDER = "assets/"
    }

    // Configurations have to be loaded before Guice, so can't use DI here.
    private val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())

        enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
    }

    var messaging: Messaging? = null
    val config: Config = load()

    init {
        create()
    }

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
     * Create default config file which is visible to end-users if it doesn't exist.
     */
    private fun create() {
        val (file, created) = getConfigFile()
        if (created) {
            file.writeBytes(IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream(CONFIG_FILE)))
        }
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