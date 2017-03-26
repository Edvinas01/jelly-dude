package com.edd.jelly.core.events

import com.edd.jelly.exception.GameException
import java.lang.reflect.ParameterizedType

class Messaging {

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<Event>>>()
    private val waiting = mutableListOf<Event>()
    private var ready = true

    /**
     * Register a new listener for a specific event type.
     */
    fun <T : Event> listen(listener: Listener<T>) {

        // Actual listener event type, which will be looked up from generic parameters.
        var actualType: Class<out Event>? = null

        for (type in listener.javaClass.genericInterfaces) {
            if (type is ParameterizedType) {
                if (Listener::class.java.isAssignableFrom(type.rawType as Class<*>)) {

                    // First parameter of a listener generic param is the event type.
                    @Suppress("UNCHECKED_CAST")
                    with(type.actualTypeArguments[0] as Class<Event>) {
                        actualType = this
                    }
                }
            }
        }

        // Register the listener if type is found.
        actualType?.let { type ->
            var existing = listeners[type]
            if (existing == null) {
                existing = mutableListOf<Listener<Event>>()
                listeners[type] = existing
            }

            @Suppress("UNCHECKED_CAST")
            existing.add(listener as Listener<Event>)

        } ?: throw GameException("Type ${listener.javaClass} is not a valid listener type")
    }

    /**
     * Send an event of a specific type to all listeners who listen for this event.
     */
    fun <T : Event> send(event: T) {
        if (!ready) {
            waiting.add(event)
            return
        }

        listeners[event.javaClass]?.forEach {
            it.listen(event)
        }
    }

    /**
     * Ready up messaging and clear waiting list.
     */
    fun start(): Messaging {
        ready = true
        waiting.forEach {
            send(it)
        }
        waiting.clear()
        return this
    }

    /**
     * Pause messaging.
     */
    fun stop(): Messaging {
        ready = false
        return this
    }
}