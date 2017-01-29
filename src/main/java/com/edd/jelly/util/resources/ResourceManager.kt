package com.edd.jelly.util.resources

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.google.inject.Inject

class ResourceManager @Inject constructor() {

    companion object {
        private val ATLAS_DIRECTORY = "textures"
        private val ATLAS_FILE_TYPE = "atlas"
        private val MAIN_ATLAS_NAME = "main"

        private val FONT_DIRECTORY = "fonts"
        private val FONT_SIZE = 16
        private val MAIN_FONT_NAME = "kong_text"
    }

    private val atlases = mutableMapOf<String, TextureAtlas>()
    private val fonts = mutableMapOf<String, BitmapFont>()

    /**
     * Main texture atlas.
     */
    lateinit var mainAtlas: TextureAtlas

    init {
        mainAtlas = getAtlas(MAIN_ATLAS_NAME)
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
            TextureAtlas("$ATLAS_DIRECTORY/$fullPath")
        })
    }

    /**
     * Get main game font.
     *
     * @return font.
     */
    fun getFont(): BitmapFont {
        return getFont(MAIN_FONT_NAME)
    }

    /**
     * Get font by name.
     *
     * @param name font name.
     * @return font.
     */
    fun getFont(name: String): BitmapFont {
        // todo load by name
        return BitmapFont()
    }
}

operator fun TextureAtlas.get(name: String): TextureRegion? =
        findRegion(name)