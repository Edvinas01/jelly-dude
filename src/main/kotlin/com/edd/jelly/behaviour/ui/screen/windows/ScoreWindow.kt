package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.core.resources.Language

class ScoreWindow constructor(
        skin: Skin
) : Window("Score", skin), LanguageAware {

    override fun updateLanguage(lang: Language) {
    }
}