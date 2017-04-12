package com.edd.jelly.core.resources

data class LanguageHandle(
        val internalName: String,
        val name: String
) {

    override fun toString(): String {
        return name
    }
}