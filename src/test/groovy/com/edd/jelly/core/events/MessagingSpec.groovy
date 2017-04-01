package com.edd.jelly.core.events

import spock.lang.Specification

class MessagingSpec extends Specification {

    private Messaging messaging

    def setup() {
        messaging = new Messaging()
    }

    def "Should receive one event"() {
        given:
        def received
        messaging.listen(TestEvent.class, new Listener<TestEvent>() {

            @Override
            void listen(TestEvent event) {
                received = event
            }
        })

        when:
        messaging.send(new TestEvent())
        messaging.send(new SecondTestEvent())

        then:
        received != null && received instanceof TestEvent
    }

    def "Should receive no events"() {
        given:
        def received = []
        messaging.listen(ThirdTestEvent.class, new Listener<ThirdTestEvent>() {

            @Override
            void listen(ThirdTestEvent event) {
                received << event
            }
        })

        when:
        messaging.send(new TestEvent())
        messaging.send(new SecondTestEvent())

        then:
        received.isEmpty()
    }

    def "Should not send events"() {
        given:
        def counter = 0

        messaging.stop()
        messaging.listen(TestEvent.class, new Listener<TestEvent>() {

            @Override
            void listen(TestEvent event) {
                counter++
            }
        })

        when:
        messaging.send(new TestEvent())

        then:
        counter == 0
    }

    def "Should send events later"() {
        given:
        def received = null

        messaging.stop()
        messaging.listen(TestEvent.class, new Listener<TestEvent>() {

            @Override
            void listen(TestEvent event) {
                received = event
            }
        })

        when:
        messaging.send(new TestEvent())
        messaging.start()

        then:
        received != null && received instanceof TestEvent
    }

    class TestEvent implements Event {
    }

    class SecondTestEvent implements Event {
    }

    class ThirdTestEvent implements Event {
    }
}