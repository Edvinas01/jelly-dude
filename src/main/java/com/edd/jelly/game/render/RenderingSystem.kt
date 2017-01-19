package com.edd.jelly.game.render

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.game.component.Transform
import com.google.inject.Inject

class RenderingSystem @Inject constructor(private val batch: SpriteBatch) : IteratingSystem(Family.all(
        Transform::class.java).get()) {

    override fun update(deltaTime: Float) {
        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // Render stuff.
    }
}