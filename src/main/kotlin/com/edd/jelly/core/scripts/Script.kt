package com.edd.jelly.core.scripts

import jdk.nashorn.api.scripting.ScriptObjectMirror

class Script(val name: String,
             val functions: Set<String>,
             val scriptObject: ScriptObjectMirror)