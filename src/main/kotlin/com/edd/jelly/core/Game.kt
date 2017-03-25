package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.logging.log4j.LogManager

class Game(val configurations: Configurations) : ApplicationAdapter() {

    companion object {
        private val LOG = LogManager.getLogger(Game::class.java)
    }

    internal lateinit var engine: Engine
    internal lateinit var injector: Injector

    override fun create() {
        LOG.info("Starting game")

        engine = Engine()

        injector = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).systems.map {
            injector.getInstance(it)
        }.forEach { s ->
            engine.addSystem(s)
        }

        injector.getInstance(Messaging::class.java)
                .ready()

        injector.getInstance(FileAlterationMonitor::class.java)
                .start()

        Gdx.input.inputProcessor = injector.getInstance(InputMultiplexer::class.java)
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
    }
}