package com.edd.jelly.exception

import spock.lang.Ignore
import spock.lang.Specification

// Just testing spock here.
class JellyExceptionSpec extends Specification {

    @Ignore
    def "should format the message"() {
        given:
        def message = 'bad'

        when:
        def exception = new JellyException('Bad {0}', message)

        then:
        exception.message == "Bad $message" as String
    }

    def "should trow exception"() {
        when:
        new JellyException('Bad {}', badArg)

        then:
        thrown(IllegalArgumentException)

        where:
        badArg | _
        null   | _
    }
}