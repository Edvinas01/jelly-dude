package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Component
import com.edd.jelly.util.ComponentResolver
import org.jbox2d.particle.ParticleGroup

data class Particles(val particleGroup: ParticleGroup) : Component {

    companion object : ComponentResolver<Particles>(Particles::class.java)
}