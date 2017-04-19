package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.edd.jelly.behaviour.physics.Physics
import com.edd.jelly.behaviour.physics.body.SoftBodyBuilder
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.tiled.JellyMap
import com.edd.jelly.core.tiled.JellyMapLoader
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.EntityListenerAdapter
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class LevelSystem @Inject constructor(
        private val softBodyBuilder: SoftBodyBuilder,
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging,
        private val world: World
) : EntitySystem() {

    private lateinit var maps: ImmutableArray<Entity>

    companion object {
        val COLLISION_LAYER = "collision"
    }

    override fun addedToEngine(engine: Engine) {
        maps = engine.getEntitiesFor(Family.all(JellyMap::class.java).get())

        initListeners()
    }

    /**
     * Get currently running map info.
     */
    private fun getCurrentMap(): JellyMap {
        if (maps.size() > 1) {
            throw GameException("Only one map must be loaded")
        }
        return maps.first()?.let {
            JellyMap.mapper[it]
        } ?: throw GameException("Map must be loaded")
    }

    /**
     * Unload current level.
     */
    private fun unloadLevel() {
        engine.removeAllEntities()
    }

    /**
     * Load level by name.
     */
    private fun loadLevel(name: String,
                          internal: Boolean = false) {

        val map = jellyMapLoader.loadMap(name, internal)
        MapBodyBuilder
                .usingWorld(world)
                .tiledMapLayer(map.tiledMap, COLLISION_LAYER)
                .buildBodies().forEach {

            engine.addEntity(Entity().add(Physics(it)))
        }
        engine.addEntity(Entity().apply {
            add(map)
        })

        map.entitiesLayer.objects.filter {
            it.properties["soft"] as? Boolean ?: false
        }.forEach {
            engine.addEntity(softBodyBuilder.create(it))
        }

        messaging.send(LevelLoadedEvent(map))
    }

    /**
     * Initialize listeners for this system.
     */
    private fun initListeners() {

        // Listen for new level load requests.
        messaging.listen<LoadNewLevelEvent> {
            unloadLevel()
            loadLevel(it.name, it.internal)
        }

        // Listen for level restarts.
        messaging.listen<RestartLevelEvent> {
            val name = getCurrentMap().name

            unloadLevel()
            loadLevel(name)
        }

        // Listen for level removals.
        engine.addEntityListener(Family.all(JellyMap::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                JellyMap.mapper[entity].tiledMap.dispose()
            }
        })
    }
}