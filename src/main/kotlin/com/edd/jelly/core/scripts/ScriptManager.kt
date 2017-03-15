package com.edd.jelly.core.scripts

import com.edd.jelly.exception.GameException
import com.google.inject.Inject
import com.google.inject.Singleton
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.ScriptObjectMirror
import java.io.InputStreamReader
import javax.script.Invocable
import javax.script.ScriptException

@Singleton
class ScriptManager @Inject constructor(private val engine: NashornScriptEngine) {

    private val scripts: Map<String, Script>

    private companion object {
        val SCRIPT_DIRECTORY = "scripts/"
        val DESCRIBE_FUNCTION = "main"
    }

    init {
        scripts = loadScripts()
    }

    /**
     * Get hooks based on provided hook type. Don't call this function very often!
     */
    fun <T> getHooks(hookType: Class<T>): List<T> {
        val mustContain = hookType.methods.map {
            it.name
        }

        return scripts.values.filter { s ->
            s.functions.containsAll(mustContain)
        }.map {
            engine.getInterface(it.scriptObject, hookType)
        }
    }

    private fun loadScripts(): Map<String, Script> {
        val scripts = mutableMapOf<String, Script>()

        for (name in InputStreamReader(ClassLoader.getSystemResourceAsStream(SCRIPT_DIRECTORY)).readLines()) {
            val content = InputStreamReader(ClassLoader.getSystemResourceAsStream("$SCRIPT_DIRECTORY/$name"))
                    .readText()

            try {
                engine.compile(content).apply {
                    eval()

                    try {
                        val res = (engine as Invocable).invokeFunction(DESCRIBE_FUNCTION) as ScriptObjectMirror
                        val formedName = if (name.contains(".")) name.takeWhile { it != '.' } else name

                        scripts.put(formedName, Script(formedName, res.keys.toSet(), res))
                    } catch (e: NoSuchMethodException) {
                        throw GameException("Script $name must \"$DESCRIBE_FUNCTION\" function")
                    }
                }
            } catch (e: ScriptException) {
                throw GameException("Could not compile script with name: $name", e)
            }
        }
        return scripts
    }
}