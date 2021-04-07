package de.gesellix.testutil

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import org.slf4j.LoggerFactory
import spock.lang.Specification

class MemoryAppenderTest extends Specification {

  def setup() {
    MemoryAppender.clearLoggedEvents()
  }

  def "collects log events"() {
    given:
    def logger = LoggerFactory.getLogger("for-test")

    when:
    logger.info("let's log something")

    then:
    def event = new LoggingEvent().with {
      it.level = Level.INFO
      it.message = "let's log something"
      it
    }
    MemoryAppender.findLoggedEvent(event)
  }
}
