package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.edd.jelly.behaviour.physics.Physics
import com.edd.jelly.core.events.Listener
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.tiled.JellyMap
import com.edd.jelly.core.tiled.JellyMapLoader
import com.edd.jelly.util.EntityListenerAdapter
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class LevelSystem @Inject constructor(
        private val inputMultiplexer: InputMultiplexer,
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging,
        private val world: World
) : EntitySystem() {

    companion object {
        val COLLISION_LAYER = "collision"
    }

    override fun addedToEngine(engine: Engine) {
        initListeners()
        messaging.send(LoadLevelEvent("test"))
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
    private fun loadLevel(name: String) {
        val map = jellyMapLoader.loadMap(name)
        MapBodyBuilder
                .usingWorld(world)
                .tiledMapLayer(map.tiledMap, COLLISION_LAYER)
                .buildBodies().forEach {

            engine.addEntity(Entity().add(Physics(it)))
        }
        engine.addEntity(Entity().apply {
            add(map)
        })
    }

    /**
     * Initialize listeners for this system.
     */
    private fun initListeners() {
        messaging.listen(object : Listener<LoadLevelEvent> {
            override fun listen(event: LoadLevelEvent) {
                unloadLevel()
                loadLevel(event.levelName)
            }
        })

        // Listen for level removals.
        engine.addEntityListener(Family.all(JellyMap::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                JellyMap.mapper[entity].tiledMap.dispose()
            }
        })

        // TODO only for testing.
        inputMultiplexer.addProcessor(object : InputAdapter() {
            override fun keyUp(keycode: Int): Boolean {
                if (Input.Keys.NUMPAD_1 == keycode) {
                    messaging.send(LoadLevelEvent("test"))
                    return true
                }
                if (Input.Keys.NUMPAD_2 == keycode) {
                    messaging.send(LoadLevelEvent("test2"))
                    return true
                }
                return false
            }
        })
    }
}