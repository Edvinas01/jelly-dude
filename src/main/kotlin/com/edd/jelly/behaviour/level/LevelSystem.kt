package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.behaviour.level.MapBodyBuilder
import com.edd.jelly.behaviour.physics.Physics
import com.edd.jelly.core.events.Listener
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.util.EntityListenerAdapter
import com.edd.jelly.util.resources.ResourceManager
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class LevelSystem @Inject constructor(
        private val inputMultiplexer: InputMultiplexer,
        private val resources: ResourceManager,
        private val messaging: Messaging,
        private val world: World
) : EntitySystem() {

    companion object {
        val COLLISION_LAYER = "collision"
        val BACKGROUND_NAME = "background"
        val FOREGROUND_NAME = "foreground"
        val BACKGROUND_TEXTURE_PROPERTY = "background_texture"
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
        val map = resources.getTiledMap(name)
        MapBodyBuilder
                .usingWorld(world)
                .tiledMapLayer(map, COLLISION_LAYER)
                .buildBodies().forEach {

            engine.addEntity(Entity().add(Physics(it)))
        }
        engine.addEntity(Entity().apply {
            add(createRenderableLevel(map))
        })
    }

    /**
     * Create level renderable details.
     */
    private fun createRenderableLevel(map: TiledMap): RenderableLevel {
        val baseLayers = mutableListOf<MapLayer>()
        val backgroundLayers = mutableListOf<MapLayer>()
        val foregroundLayers = mutableListOf<MapLayer>()

        // Segment layers.
        map.layers.forEach {
            with(it.name) {
                if (startsWith(BACKGROUND_NAME, true)) {
                    backgroundLayers.add(it)
                } else if (startsWith(FOREGROUND_NAME, true)) {
                    foregroundLayers.add(it)
                } else {
                    baseLayers.add(it)
                }
            }
        }

        return RenderableLevel(
                map,
                baseLayers.toList(),
                backgroundLayers.toList(),
                foregroundLayers.toList(),
                map.properties[BACKGROUND_TEXTURE_PROPERTY]?.let {
                    resources.getTexture(it as String)
                }
        )
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

        // Listen for map removals.
        engine.addEntityListener(Family.all(RenderableLevel::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                RenderableLevel.mapper[entity].tiledMap.dispose()
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