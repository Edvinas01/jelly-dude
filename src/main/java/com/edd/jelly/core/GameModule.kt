package com.edd.jelly.core

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.edd.jelly.game.render.RenderingSystem
import com.edd.jelly.game.transform.TransformSystem
import com.google.inject.*

class GameModule(private val game: Game) : Module {

    override fun configure(binder: Binder) {
        binder.bind(Camera::class.java)
                .annotatedWith(UICamera::class.java)
                .toInstance(game.uiCamera)

        binder.bind(Camera::class.java)
                .toInstance(game.camera)

        binder.bind(Batch::class.java)
                .toInstance(game.batch)
    }

    @Provides
    @Singleton
    fun systems(): Systems {
        return Systems(listOf(
                RenderingSystem::class.java,
                TransformSystem::class.java
        ))
    }
}

@BindingAnnotation
@Target(AnnotationTarget.PROPERTY)
annotation class UICamera

data class Systems(val systems: List<Class<out EntitySystem>>)