package com.edd.jelly.exception

class JellyException : RuntimeException {

    constructor(message: String, e: Exception) : super(message, e)

    constructor(message: String) : super(message)
}