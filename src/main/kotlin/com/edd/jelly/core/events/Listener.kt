package com.edd.jelly.core.events

interface Listener<T : Event> {

    fun listen(event: T)
}