package com.edd.jelly.util.resources

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.edd.jelly.exception.GameException
import com.google.inject.Inject

class ResourceManager @Inject constructor() {

    companion object {
        private val ATLAS_DIRECTORY = "textures"
        private val ATLAS_FILE_TYPE = "atlas"

        private val DEV_ATLAS_NAME = "dev"
    }

    private val atlases = mutableMapOf<String, TextureAtlas>()

    /**
     * Texture atlas which is used for development purposes.
     */
    lateinit var devAtlas: TextureAtlas

    init {
        devAtlas = loadAtlas(DEV_ATLAS_NAME)
    }

    /**
     * Load texture atlas by specifying its [path].
     *
     * @param path texture atlas path.
     * @return loaded texture atlas.
     */
    fun loadAtlas(path: String): TextureAtlas {
        return atlases.getOrPut(path, defaultValue = {
            val fullPath = if (path.endsWith(ATLAS_FILE_TYPE)) {
                path
            } else {
                "$path.$ATLAS_FILE_TYPE"
            }
            TextureAtlas("$ATLAS_DIRECTORY/$fullPath")
        })
    }

    /**
     * Get texture atlas by name.
     *
     * @param name texture atlas name.
     * @return texture atlas.
     */
    operator fun get(name: String): TextureAtlas {
        return atlases.getOrElse(name, defaultValue = {
            throw GameException("Texture atlas with path: $name is not loaded")
        })
    }
}

operator fun TextureAtlas.get(name: String): TextureRegion =
        findRegion(name)