package com.edd.jelly.core.resources

class Language(val internalName: String, val name: String) {
    override fun toString(): String {
        return name
    }
}