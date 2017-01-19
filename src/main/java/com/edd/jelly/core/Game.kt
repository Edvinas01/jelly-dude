package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.game.component.Transform
import com.google.inject.Guice
import com.google.inject.Injector

class Game : ApplicationAdapter() {

    internal lateinit var engine: Engine
    internal lateinit var batch: SpriteBatch

    internal lateinit var injector: Injector

    override fun create() {
        engine = Engine()
        batch = SpriteBatch()

        injector = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).systems.map {
            injector.getInstance(it)
        }.forEach { s ->
            engine.addSystem(s)
        }

        engine.addEntity(Entity().apply {
            add(Transform(15, 15))
        })
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
    }
}