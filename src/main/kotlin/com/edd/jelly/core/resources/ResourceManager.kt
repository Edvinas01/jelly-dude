package com.edd.jelly.core.resources

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class ResourceManager @Inject constructor() {

    companion object {
        private val TEXTURE_DIRECTORY = "textures"
        private val PNG_FILE_TYPE = "png"

        private val ATLAS_FILE_TYPE = "atlas"
        private val MAIN_ATLAS_NAME = "main"

        private val FONT_DIRECTORY = "fonts"
        private val FONT_SIZE = 16
        private val MAIN_FONT_NAME = "kong_text"
    }

    private val textures = mutableMapOf<String, Texture>()
    private val atlases = mutableMapOf<String, TextureAtlas>()
    private val fonts = mutableMapOf<String, BitmapFont>()

    /**
     * Main texture atlas.
     */
    val mainAtlas: TextureAtlas

    init {
        println("lod meh")
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
            TextureAtlas("$TEXTURE_DIRECTORY/$fullPath")
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

        val gen = FreeTypeFontGenerator(Gdx.files.internal("$FONT_DIRECTORY/$name.ttf"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 16
        return gen.generateFont(param)
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
}

operator fun TextureAtlas.get(name: String): TextureRegion? =
        findRegion(name)