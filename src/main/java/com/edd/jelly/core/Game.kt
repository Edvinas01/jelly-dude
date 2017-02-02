package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.google.inject.Guice
import com.google.inject.Injector

class Game : ApplicationAdapter() {

    internal lateinit var engine: Engine
    internal lateinit var injector: Injector

    override fun create() {
        engine = Engine()

        injector = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).systems.map {
            injector.getInstance(it)
        }.forEach { s ->
            engine.addSystem(s)
        }

        Gdx.input.inputProcessor = injector.getInstance(InputMultiplexer::class.java)
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
    }
}