package com.edd.jelly.core.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

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
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
    }

    val config: Config = load()

    /**
     * Load config file from classpath resources.
     */
    private fun load(): Config {
        return mapper.readValue(ClassLoader
                .getSystemResourceAsStream(CONFIG_FILE), Config::class.java)
    }
}