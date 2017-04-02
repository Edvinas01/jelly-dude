package com.edd.jelly.core.ui

import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class UI @Inject constructor(
        private val resourceManager: ResourceManager
) {

    init {
        // assetManager.load("")
    }
}