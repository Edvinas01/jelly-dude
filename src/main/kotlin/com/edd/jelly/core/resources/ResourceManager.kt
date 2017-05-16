package com.edd.jelly.core.resources

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.util.NullMusic
import com.edd.jelly.util.NullSound
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Singleton
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

@Singleton
class ResourceManager @Inject constructor(
        private val messaging: Messaging,
        configurations: Configurations,
        objectMapper: ObjectMapper
) {

    private companion object {
        const val MESSAGES_FILE = "messages.yml"
        const val TEXTURE_DIRECTORY = "textures/"
        const val PNG_FILE_TYPE = "png"
        const val ATLAS_FILE_TYPE = "atlas"
        const val SOUND_DIR = "sounds/"
        const val SOUND_FORMAT = ".ogg"

        val LOG: Logger = LogManager.getLogger(ResourceManager::class.java)
    }

    private val languageMap = loadLanguages(objectMapper)
    var language = languageMap[configurations.config.game.language]!!
        private set

    val languages: Collection<Language>
        get() = languageMap.values

    private val textures = mutableMapOf<String, Texture>()
    private val atlases = mutableMapOf<String, TextureAtlas>()
    private val regions = mutableMapOf<String, TextureRegion>()
    private val sounds = mutableMapOf<String, Sound>()
    private val songs = mutableMapOf<String, Music>()

    /**
     * Blank texture which can be used as a placeholder.
     */
    val blank = Texture(Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
        setColor(Color.PURPLE)
        drawPixel(0, 0)
    })

    /**
     * Main game UI skin.
     */
    val skin: Skin

    /**
     * Main texture atlas.
     */
    val atlas: TextureAtlas

    init {
        initListeners()

        atlas = getAtlas("jelly_stuff")
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
            TextureAtlas(FileHandle(File("${Configurations.ASSETS_FOLDER}$TEXTURE_DIRECTORY$fullPath")))
        })
    }

    /**
     * Get a cached texture by its name.
     */
    fun getTexture(name: String, fullName: Boolean = false) = getTexturePath(name, fullName).let {
        textures.getOrPut(it, defaultValue = {
            loadTexture(it)
        })
    }

    /**
     * Get a cached texture region by its name.
     */
    fun getRegion(name: String, fullName: Boolean = false) = getTexturePath(name, fullName).let {
        regions.getOrPut(it, defaultValue = {
            TextureRegion(loadTexture(it))
        })
    }

    /**
     * Get and cache a sound by its name, sound is loaded from sound directory.
     */
    fun getSound(name: String): Sound {
        val fullName = name.asSoundPath()

        return sounds.getOrPut(fullName, defaultValue = {
            val file = File(fullName)
            if (!file.exists()) {
                LOG.warn("Sound: {}, does not exist", fullName)
                NullSound
            } else {
                Gdx.audio.newSound(FileHandle(file))
            }
        })
    }

    /**
     * Get and cache music by its name, music is loaded from sound directory.
     */
    fun getMusic(name: String): Music {
        val fullName = name.asSoundPath()

        return songs.getOrPut(fullName, defaultValue = {
            val file = File(fullName)
            if (!file.exists()) {
                LOG.warn("Music: {}, does not exist", fullName)
                NullMusic
            } else {
                Gdx.audio.newMusic(FileHandle(file))
            }
        })
    }

    /**
     * Load a texture by specifying a full path.
     */
    private fun loadTexture(path: String) = File(path).let {
        if (it.isDirectory || !it.exists()) {

            LOG.warn("Texture: {}, does not exist", path)
            blank
        } else {

            LOG.debug("Loading texture: {}", path)
            Texture(FileHandle(it))
        }
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

    /**
     * Get a full, formatted texture path.
     */
    private fun getTexturePath(name: String, fullName: Boolean) = if (fullName) {
        "${Configurations.ASSETS_FOLDER}$name"
    } else {
        val typedPath = if (name.endsWith(PNG_FILE_TYPE)) {
            name
        } else {
            "$name.$PNG_FILE_TYPE"
        }
        "${Configurations.ASSETS_FOLDER}$TEXTURE_DIRECTORY$typedPath"
    }

    private data class MessageBundle(val name: String, val messages: Map<String, String>)

    /**
     * Get string as sound path.
     */
    private fun String.asSoundPath() =
            "${Configurations.ASSETS_FOLDER}$SOUND_DIR$this$SOUND_FORMAT"
}

operator fun TextureAtlas.get(name: String): TextureRegion? =
        findRegion(name)