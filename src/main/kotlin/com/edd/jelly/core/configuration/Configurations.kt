package com.edd.jelly.core.configuration

import com.edd.jelly.game.JellyGame
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.xenomachina.argparser.ArgParser
import java.io.File

class Configurations(argParser: ArgParser) {

    companion object {
        private const val CONFIG_FILE = "config.yml"

        const val GIT_HUB_URL = "https://github.com/Edvinas01/jelly-dude"
        const val GIT_HUB_NEW_ISSUE_URL = "$GIT_HUB_URL/issues/new"

        /**
         * Folder where all game resources and assets are held.
         */
        const val ASSETS_FOLDER = "assets/"

        const val MENU_LEVEL_NAME = "menu"
    }

    // Configurations have to be loaded before Guice, so can't use DI here.
    val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())
        enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
    }

    // Is dev mode enabled.
    private val dev by argParser.flagging(
            "-d",
            "--dev",
            help = "developer mode"
    )

    private var messaging: Messaging? = null

    val config = load(dev)

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
    private fun load(internal: Boolean): Config {
        if (internal) {
            return mapper.readValue(ClassLoader.getSystemResourceAsStream(CONFIG_FILE), Config::class.java)
        }

        return getConfigFile().let { (file, created) ->
            if (created) {
                val config = mapper.readValue(ClassLoader.getSystemResourceAsStream(CONFIG_FILE), Config::class.java)
                save(config)
                config
            } else {
                mapper.readValue(file, Config::class.java)
            }
        }
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