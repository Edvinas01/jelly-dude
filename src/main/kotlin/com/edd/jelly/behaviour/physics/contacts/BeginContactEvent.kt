package com.edd.jelly.behaviour.physics.contacts

import com.edd.jelly.core.events.Event
import org.jbox2d.dynamics.contacts.Contact

data class BeginContactEvent(val contact: Contact) : Event