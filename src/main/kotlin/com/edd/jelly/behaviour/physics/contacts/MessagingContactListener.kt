package com.edd.jelly.behaviour.physics.contacts

import com.edd.jelly.core.events.Messaging
import com.edd.jelly.behaviour.common.event.BeginContactEvent
import com.edd.jelly.behaviour.common.event.EndContactEvent
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.contacts.Contact

class MessagingContactListener(private val messaging: Messaging) : ContactListener {

    override fun endContact(contact: Contact) {
        if (isInteresting(contact)) {
            messaging.send(EndContactEvent(contact))
        }
    }

    override fun beginContact(contact: Contact) {
        if (isInteresting(contact)) {
            messaging.send(BeginContactEvent(contact))
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }

    private fun isInteresting(contact: Contact) =
            contact.fixtureA.body.userData != null || contact.fixtureB.body.userData != null
}