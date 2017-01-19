package com.edd.jelly.game.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

open class ComponentResolver<T : Component>(clazz: Class<T>) {
    val mapper: ComponentMapper<T> = ComponentMapper.getFor(clazz)

    operator fun get(entity: Entity): T = mapper.get(entity)
}

val Entity.transform: Transform
    get() = Transform[this]