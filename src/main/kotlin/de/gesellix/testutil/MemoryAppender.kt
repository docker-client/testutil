package de.gesellix.testutil

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory.getLogger
import java.io.ByteArrayOutputStream

/**
 * From https://github.com/grails-plugins/grails-logback/blob/master/src/java/grails/plugin/logback/MemoryAppender.java
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class MemoryAppender : OutputStreamAppender<ILoggingEvent>() {

    private val loggedEvents: ArrayList<ILoggingEvent> = arrayListOf()

    companion object {
        @JvmStatic
        fun clearLoggedEvents() {
            getMemoryAppender().clear()
        }

        @JvmStatic
        fun findLoggedEvent(needle: ILoggingEvent): ILoggingEvent? {
            val memoryAppender = getMemoryAppender()
            val events = memoryAppender.getLoggedEvents()
            return events.find { e: ILoggingEvent ->
                e.level == needle.level && e.message == needle.message
            }
        }

        @JvmStatic
        fun getMemoryAppender(): MemoryAppender {
            val rootLogger = getLogger(ROOT_LOGGER_NAME) as Logger
            val memoryAppender = rootLogger.iteratorForAppenders().asSequence().find { it is MemoryAppender }
                    ?: throw IllegalStateException("Didn't find MemoryAppender. Please check your logback(-test) config.")
            return memoryAppender as MemoryAppender
        }
    }

    @Override
    override fun start() {
        outputStream = ByteArrayOutputStream()
        super.start()
    }

    @Override
    override fun subAppend(event: ILoggingEvent) {
        super.subAppend(event)
        loggedEvents.add(event)
    }

    fun getLoggedEvents(): List<ILoggingEvent> {
        return loggedEvents.toList()
    }

    fun clear() {
        loggedEvents.clear()
    }
}
