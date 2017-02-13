package com.edd.jelly.core.events

interface Listener<in T : Event> {

    fun listen(event: T)
}