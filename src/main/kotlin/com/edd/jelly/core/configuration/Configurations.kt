package com.edd.jelly.core.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

class Configurations {

    companion object {
        const val CONFIG_FILE = "config.yml"
    }

    // Configurations have to be loaded before Guice, so can't use DI here.
    private val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())
    }

    val config: Config = load()
    /**
     * Load config file from resources.
     */
    private fun load(): Config {
        return mapper.readValue(ClassLoader
                .getSystemResourceAsStream(CONFIG_FILE), Config::class.java)
    }
}