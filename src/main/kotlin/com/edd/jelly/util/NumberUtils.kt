package com.edd.jelly.util

import com.badlogic.gdx.math.MathUtils

fun Float.clamp(min: Number, max: Number) =
        MathUtils.clamp(this, min.toFloat(), max.toFloat())