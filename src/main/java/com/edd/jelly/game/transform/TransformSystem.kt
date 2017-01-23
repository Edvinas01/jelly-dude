package com.edd.jelly.game.transform

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.edd.jelly.game.component.Transform
import com.edd.jelly.game.component.transform

class TransformSystem() : IteratingSystem(Family.all(
        Transform::class.java).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transform

        transform.rotation += 1
        println(transform.rotation)

        transform.rotation %= 360
    }
}