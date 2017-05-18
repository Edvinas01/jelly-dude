package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.edd.jelly.behaviour.common.event.*
import com.edd.jelly.behaviour.physics.body.BodyEntityFactory
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.tiled.JellyMap
import com.edd.jelly.core.tiled.JellyMapLoader
import com.edd.jelly.util.GameException
import com.edd.jelly.util.EntityListenerAdapter
import com.google.inject.Inject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LevelSystem @Inject constructor(
        private val bodyEntityFactory: BodyEntityFactory,
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging
) : EntitySystem() {

    private companion object {
        val LOG : Logger = LogManager.getLogger(LevelSystem::class.java)
    }

    private lateinit var maps: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        maps = engine.getEntitiesFor(Family.all(JellyMap::class.java).get())

        initListeners()
    }

    /**
     * Get currently running map info.
     */
    private fun getCurrentMap(): JellyMap? {
        if (maps.size() > 1) {
            throw GameException("Only one map must be loaded")
        }
        return maps.firstOrNull()?.let {
            JellyMap.mapper[it]
        }
    }

    /**
     * Unload current level.
     */
    private fun unloadLevel() {
        getCurrentMap()?.let {
            LOG.debug("Unloading level: {} and clearing all entities", it.name)
            engine.removeAllEntities()
            it.tiledMap.dispose()
        }
    }

    /**
     * Load a new level by name and dispose of currently loaded map.
     */
    private fun loadLevel(name: String,
                          internal: Boolean = false) {

        val map = jellyMapLoader.loadMap(name, internal)

        // Unload only after properly loading map details.
        unloadLevel()

        // Create static collisions and entities.
        (bodyEntityFactory.create(map.collisionsLayer) + bodyEntityFactory.create(map.entitiesLayer)).forEach {
            engine.addEntity(it)
        }

        // Register map.
        engine.addEntity(Entity().apply {
            add(map)
        })

        // Play level ambiance.
        map.ambientSoundNames.forEach {
            messaging.send(PlaySoundEvent(name = it, loop = true))
        }

        // Play level music.
        map.musicNames.forEach {
            messaging.send(PlayMusicEvent(name = it, loop = true))
        }

        messaging.send(LevelLoadedEvent(map))
    }

    /**
     * Initialize listeners for this system.
     */
    private fun initListeners() {

        // Listen for new level load requests.
        messaging.listen<LoadNewLevelEvent> {
            loadLevel(it.name, it.internal)
        }

        // Listen for level restarts.
        messaging.listen<RestartLevelEvent> {
            getCurrentMap()?.let {
                loadLevel(it.name)
            }
        }

        // Listen for level removals.
        engine.addEntityListener(Family.all(JellyMap::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                JellyMap.mapper[entity].tiledMap.dispose()
            }
        })
    }
}