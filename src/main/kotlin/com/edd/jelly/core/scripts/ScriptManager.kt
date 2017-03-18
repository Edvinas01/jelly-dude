/*
package com.edd.jelly.core.scripts

import com.google.inject.Inject
import com.google.inject.Singleton
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.ScriptObjectMirror
import org.apache.commons.io.monitor.FileAlterationListener
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.script.Invocable

@Singleton
class ScriptManager @Inject constructor(
        private val engine: NashornScriptEngine,
        fileMonitor: FileAlterationMonitor
) {

    */
/*

    functionType -> [
        function
    ]

    scriptName -> [
        type
    ]

        Jumps -> [ jumpProcessor: {}, jumpProcessor: {} ]
        (Script) -> [ ref: jumpProcessor: {} ]
     *//*


    inner class Hook<out T>(
            val functions: T,
            val script: Script
    )

    private val scripts: MutableMap<String, Script>

    private val hooks = mutableMapOf<Class<*>, MutableList<*>>()


    private companion object {
        val JS_FILE_TYPE = ".js"
        val SCRIPT_DIRECTORY = "scripts/"
        val DESCRIBE_FUNCTION = "main"
    }

    init {
        scripts = loadScripts()

        fileMonitor.addObserver(FileAlterationObserver(SCRIPT_DIRECTORY).apply {
            addListener(createFileListener())
        })
    }

    */
/**
     * Create a hook for a given type interface and return hook functions for this type.
     *//*

    fun <T> hook(hookType: Class<T>): List<T> {

        @Suppress("UNCHECKED_CAST")
        return hooks.getOrPut(hookType, defaultValue = {
            val hookList = mutableListOf<T>()

            // Function names which are required for this hook.
            val requiredFunctions = hookType.methods.map {
                it.name
            }

            // Applicable scripts which have the required functions.
            scripts.values.filter {
                it.functions.containsAll(requiredFunctions)
            }.forEach {
                hookList.add(engine.getInterface(it.scriptObject, hookType))
            }
            hookList
        }) as List<T>
    }

    */
/**
     * Create script change listener.
     *//*

    private fun createFileListener(): FileAlterationListener {
        return object : FileAlterationListenerAdaptor() {
            override fun onFileCreate(file: File) {
                loadScript(file)?.let { s ->

                    hooks.forEach { h ->
                        h.
                    }
                }
            }

            override fun onFileChange(file: File) {


                loadScript(file)?.let {

                }


                // Load the changed script to see if it compiles.
                loadScript(file)?.let { s ->

                    // Replace existing script with new loaded one.
                    scripts.put(s.name, s)?.let {

                        // Reload hooks for the old script.
//
//
//                        it.hooked.forEach {
//
//                            val requiredFunctions = it.methods.map {
//                                it.name
//                            }
//
//                            if (s.functions.containsAll(requiredFunctions)) {
//
//                            }
//
//                            s.scriptObject.values.filter {
//                                it.functions.containsAll(requiredFunctions)
//                            }.forEach {
//                                hookList.add(engine.getInterface(it.scriptObject, hookType))
//                                it.hooked.add(hookType)
//                            }
//                        }
                    }
                } ?: {
                    false
                }
            }

            override fun onFileDelete(file: File) {
                println("Delete: ${file.name}")
            }
        }
    }

    */
/**
     * Load a single script from a given file.
     *//*

    private fun loadScript(file: File): Script? {
        val content = file.readText()
        try {
            engine.compile(content).let { s ->
                s.eval()

                val result = (engine as Invocable).invokeFunction(DESCRIBE_FUNCTION) as ScriptObjectMirror
                return Script(
                        file.path,
                        result.keys.toSet(),
                        result
                )
            }
        } catch (e: Exception) {
            e.printStackTrace() // TODO LOGGING
        }
        return null
    }

    */
/**
     * Load all scripts from script directory.
     *//*

    private fun loadScripts(): MutableMap<String, Script> {
        val map = mutableMapOf<String, Script>()
        try {

            // Load initial scripts.
            Files.walk(Paths.get(SCRIPT_DIRECTORY))
                    .filter { Files.isRegularFile(it) }
                    .map(Path::toFile)
                    .filter { it.name.endsWith(JS_FILE_TYPE) }
                    .forEach {
                        loadScript(it)?.let {
                            map.put(it.name, it)
                        }
                    }
        } catch (e: NoSuchFileException) {
            e.printStackTrace() // TODO LOGGING
        }
        return map
    }
}*/
