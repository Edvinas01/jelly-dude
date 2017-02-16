package com.edd.jelly.util

/**
 * Take a given Float value from current Float value by using 0 as a base.
 */
fun Float.take(amount: Float): Float {
    val absThis = Math.abs(this)
    val absAmount = Math.abs(amount)

    if (absThis <= absAmount) {
        return Math.signum(amount) * absThis
    } else {
        return amount
    }
}