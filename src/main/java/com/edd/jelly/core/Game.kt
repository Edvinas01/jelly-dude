package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.util.meters
import com.google.inject.Guice
import com.google.inject.Injector

class Game : ApplicationAdapter() {

    // Game bootstrapping.
    internal lateinit var engine: Engine

    // Rendering.
    internal lateinit var uiCamera: Camera
    internal lateinit var camera: Camera
    internal lateinit var batch: SpriteBatch

    // Dependency injection.
    internal lateinit var injector: Injector

    override fun create() {
        engine = Engine()

        uiCamera = OrthographicCamera()
        camera = createMainCamera()
        batch = SpriteBatch()

        injector = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).systems.map {
            injector.getInstance(it)
        }.forEach { s ->
            engine.addSystem(s)
        }
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
    }

    private fun createMainCamera(): OrthographicCamera {
        val width = Gdx.graphics.width.meters
        val height = Gdx.graphics.height.meters

        return OrthographicCamera(width, height).apply {
            position.set(width / 2f, height / 2f, 0f)
            update()
        }
    }
}