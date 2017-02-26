package com.edd.jelly.util

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener

abstract class EntityListenerAdapter : EntityListener {

    override fun entityRemoved(entity: Entity) {
    }

    override fun entityAdded(entity: Entity) {
    }
}