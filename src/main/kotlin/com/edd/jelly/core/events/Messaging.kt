package com.edd.jelly.core.events

class Messaging {

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<Event>>>()
    private val waiting = mutableListOf<Event>()
    private var ready = true

    /**
     * Register a new listener for a specific event type by providing a lambda function.
     */
    inline fun <reified T : Event> listen(crossinline listener: (T) -> Unit) {
        listen(T::class.java, object : Listener<T> {
            override fun listen(event: T) {
                listener(event)
            }
        })
    }

    /**
     * Register a new listener for a specific event type by providing a concrete listener implementation.
     */
    inline fun <reified T : Event> listen(listener: Listener<T>) {
        listen(T::class.java, listener)
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

    /**
     * Register a listener of provided event type.
     */
    fun <T : Event> listen(type: Class<T>, listener: Listener<T>) {
        var existing = listeners[type]
        if (existing == null) {
            existing = mutableListOf<Listener<Event>>()
            listeners[type] = existing
        }

        @Suppress("UNCHECKED_CAST")
        existing.add(listener as Listener<Event>)
    }
}