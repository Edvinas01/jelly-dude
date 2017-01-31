package com.edd.jelly.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import org.jbox2d.particle.ParticleGroup

data class Particles(val particleGroup: ParticleGroup) : Component {

    companion object : ComponentResolver<Particles>(Particles::class.java)
}

val Entity.particles: Particles
    get() = Particles[this]