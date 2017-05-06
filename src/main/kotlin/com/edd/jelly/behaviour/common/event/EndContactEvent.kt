package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event
import org.jbox2d.dynamics.contacts.Contact

data class EndContactEvent(val contact: Contact) : Event