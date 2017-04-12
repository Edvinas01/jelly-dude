package com.edd.jelly.core.resources

class Language(
        private val messages: Map<String, String>,
        val handle: LanguageHandle
) {

    operator fun get(key: String) = messages[key] ?: "{$key}"
}