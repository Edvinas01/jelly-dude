package com.edd.jelly.util

class GameException : RuntimeException {

    constructor(message: String, e: Exception) : super(message, e)

    constructor(message: String) : super(message)
}