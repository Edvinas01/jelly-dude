package com.edd.jelly.exception

import spock.lang.Ignore
import spock.lang.Specification

// Just testing spock here.
class JellyExceptionSpec extends Specification {

    @Ignore
    def "Should format the message"() {
        given:
        def message = 'bad'

        when:
        def exception = new GameException('Bad {0}', message)

        then:
        exception.message == "Bad $message" as String
    }

    def "Should trow exception"() {
        when:
        new GameException('Bad {}', badArg)

        then:
        thrown(IllegalArgumentException)

        where:
        badArg | _
        null   | _
    }
}