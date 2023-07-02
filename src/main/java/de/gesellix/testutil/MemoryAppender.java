package de.gesellix.testutil;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * From <a href="https://github.com/grails-plugins/grails-logback/blob/6f13c7b17c16a08b8a355031c20c0f6c71a80d33/src/java/grails/plugin/logback/MemoryAppender.java">github.com/grails-plugins/grails-logback/.../MemoryAppender.java</a>
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class MemoryAppender extends OutputStreamAppender<ILoggingEvent> {

  private final List<ILoggingEvent> loggedEvents = new ArrayList<>();

  public static void clearLoggedEvents() {
    getMemoryAppender().clear();
  }

  public static ILoggingEvent findLoggedEvent(ILoggingEvent needle) {
    MemoryAppender memoryAppender = getMemoryAppender();
    List<ILoggingEvent> events = memoryAppender.getLoggedEvents();
    return events.stream().filter(e -> Objects.equals(e.getLevel(), needle.getLevel()) && Objects.equals(e.getMessage(), needle.getMessage()))
        .findFirst()
        .orElse(null);
  }

  public static MemoryAppender getMemoryAppender() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
    Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
    while (appenderIterator.hasNext()) {
      Appender<ILoggingEvent> appender = appenderIterator.next();
      if (appender instanceof MemoryAppender) {
        return (MemoryAppender) appender;
      }
    }
    throw new IllegalStateException("Didn't find a MemoryAppender. Please check your logback(-test) config.");
  }

  @Override
  public void start() {
    setOutputStream(new ByteArrayOutputStream());
    super.start();
  }

  @Override
  public void subAppend(ILoggingEvent event) {
    super.subAppend(event);
    loggedEvents.add(event);
  }

  public List<ILoggingEvent> getLoggedEvents() {
    return loggedEvents;
  }

  public void clear() {
    loggedEvents.clear();
  }
}
