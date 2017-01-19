package com.edd.jelly.core

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.game.render.RenderingSystem
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton

class GameModule(private val game: Game) : Module {

    override fun configure(binder: Binder) {
        binder.bind(SpriteBatch::class.java).toInstance(game.batch)
    }

    @Provides
    @Singleton
    fun systems(): Systems {
        return Systems(listOf(
                RenderingSystem::class.java
        ))
    }
}

data class Systems(val systems: List<Class<out EntitySystem>>)