package com.edd.jelly.core.resources

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class ResourceManager @Inject constructor() {

    companion object {
        private val TEXTURE_DIRECTORY = "textures"
        private val PNG_FILE_TYPE = "png"

        private val ATLAS_FILE_TYPE = "atlas"
        private val MAIN_ATLAS_NAME = "main"
    }

    private val textures = mutableMapOf<String, Texture>()
    private val atlases = mutableMapOf<String, TextureAtlas>()
    val skin: Skin

    /**
     * Main texture atlas.
     */
    val mainAtlas: TextureAtlas

    init {
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
}

operator fun TextureAtlas.get(name: String): TextureRegion? =
        findRegion(name)