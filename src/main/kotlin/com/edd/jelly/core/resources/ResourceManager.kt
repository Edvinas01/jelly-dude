package com.edd.jelly.core.resources

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.edd.jelly.core.configuration.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class ResourceManager @Inject constructor(
        private val messaging: Messaging,
        configurations: Configurations,
        objectMapper: ObjectMapper
) {

    companion object {
        const private val MESSAGES_FILE = "messages.yml"
        const private val TEXTURE_DIRECTORY = "textures"
        const private val PNG_FILE_TYPE = "png"

        const private val ATLAS_FILE_TYPE = "atlas"
        const private val MAIN_ATLAS_NAME = "main"
    }

    private val languageMap = loadLanguages(objectMapper)
    var language = languageMap[configurations.config.game.language]!!
        private set

    val languages: Collection<Language>
        get() = languageMap.values

    private val textures = mutableMapOf<String, Texture>()
    private val atlases = mutableMapOf<String, TextureAtlas>()

    val skin: Skin

    /**
     * Main texture atlas.
     */
    val mainAtlas: TextureAtlas

    init {
        initListeners()

        mainAtlas = getAtlas(MAIN_ATLAS_NAME)
        skin = Skin(Gdx.files.internal("ui/uiskin.json"))
    }

    /**
     * Get texture atlas by specifying its [name]. If texture atlas is not loaded into memory, its first then loaded
     * and afterwards returned.
     *
     * @param name texture atlas name.
     * @return loaded texture atlas.
     */
    fun getAtlas(name: String): TextureAtlas {
        return atlases.getOrPut(name, defaultValue = {
            val fullPath = if (name.endsWith(ATLAS_FILE_TYPE)) {
                name
            } else {
                "$name.$ATLAS_FILE_TYPE"
            }
            TextureAtlas("$TEXTURE_DIRECTORY/$fullPath")
        })
    }

    /**
     * Get texture by name. Calls to this method are cached.
     *
     * @param name texture name.
     * @return texture.
     */
    fun getTexture(name: String): Texture {
        return textures.getOrPut(name, defaultValue = {
            val fullPath = if (name.endsWith(PNG_FILE_TYPE)) {
                name
            } else {
                "$name.$PNG_FILE_TYPE"
            }
            Texture("$TEXTURE_DIRECTORY/$fullPath")
        })
    }

    /**
     * Initialize listeners for resource manager.
     */
    private fun initListeners() {
        messaging.listen<ConfigChangedEvent> { (config) ->
            language = languageMap[config.game.language]!!
        }
    }

    /**
     * Load all internal languages and convert them to a map where key is the language internal name.
     */
    private fun loadLanguages(mapper: ObjectMapper): Map<String, Language> {
        val bundles: Map<String, MessageBundle> = mapper.readValue(
                ClassLoader.getSystemResourceAsStream(MESSAGES_FILE),
                object : TypeReference<Map<String, MessageBundle>>() {}
        )

        return bundles.map {
            it.key to with(it.value) {
                Language(messages, LanguageHandle(it.key, name))
            }
        }.toMap()
    }

    private data class MessageBundle(val name: String, val messages: Map<String, String>)
}

operator fun TextureAtlas.get(name: String): TextureRegion? =
        findRegion(name)