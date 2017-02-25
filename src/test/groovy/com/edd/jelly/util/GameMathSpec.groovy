package com.edd.jelly.util

import spock.lang.Specification

class GameMathSpec extends Specification {

    def "Should take value"() {
        when:
        def result = GameMathKt.take(value, take)

        then:
        result == taken

        where:
        value | take | taken
        1f    | 2f   | 1f
        0f    | 1f   | 0f
        -1f   | -2f  | -1f
    }
}