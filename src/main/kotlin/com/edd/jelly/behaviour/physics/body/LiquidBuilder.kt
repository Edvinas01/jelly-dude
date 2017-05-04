package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.physics.Particles
import com.edd.jelly.core.tiled.float
import com.edd.jelly.core.tiled.string
import com.edd.jelly.util.meters
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Color3f
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleColor
import org.jbox2d.particle.ParticleGroupDef
import org.jbox2d.particle.ParticleType

@Singleton
class LiquidBuilder @Inject constructor(
        private val world: World
) {

    private companion object {
        val COLOR_ALPHA = 0.1f
        val COLOR_ALPHA_BYTE = COLOR_ALPHA.toByte()
    }

    /**
     * Create liquid.
     *
     * @return created liquid object entity.
     */
    fun create(obj: MapObject): Entity? {
        if (obj is RectangleMapObject) {
            return rectangle(obj)
        }
        return null
    }

    /**
     * Create liquid from rectangle.
     */
    private fun rectangle(obj: RectangleMapObject): Entity {
        val transform = with(obj.rectangle) {
            val width = width.meters
            val height = height.meters

            Transform(
                    position = Vector2(x.meters, y.meters),
                    size = Vector2(width, height)
            )
        }

        val hw = transform.width / 2
        val hh = transform.height / 2

        val group = world.createParticleGroup(ParticleGroupDef().apply {
            position.set(transform.x + hw, transform.y + hh)
            color = obj.particleColor()
            flags = ParticleType.b2_waterParticle
            shape = PolygonShape().apply {
                setAsBox(hw, hh)
            }
        })

        return Entity().apply {
            add(Particles(group))
            add(transform)
        }
    }

    /**
     * Get particle color from map object.
     */
    private fun MapObject.particleColor(): ParticleColor {
        return string("color")?.let { hex ->
            val color = Color.valueOf(hex)
            ParticleColor(Color3f(color.r, color.g, color.b)).apply {
                a = (255 * this@particleColor.float("colorAlpha", COLOR_ALPHA)).toByte()
            }

        } ?: ParticleColor(Color3f(MathUtils.random(), MathUtils.random(), MathUtils.random())).apply {
            a = COLOR_ALPHA_BYTE
        }
    }
}