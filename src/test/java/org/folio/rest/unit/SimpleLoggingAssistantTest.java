package org.folio.rest.unit;

import io.vertx.core.logging.Logger;
import io.vertx.core.spi.logging.LogDelegate;
import org.folio.rest.impl.support.SimpleLoggingAssistant;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SimpleLoggingAssistantTest extends AbstractVertxUnitTest {



  @Test
  public void shouldWriteErrorExceptionToLog() {

    //This isn't very nice, but don't know a better way to test logging
    LogDelegate mockLog = mock(LogDelegate.class);

    Logger logger = new Logger(mockLog);

    Exception expectedException = new Exception("A problem");

    new SimpleLoggingAssistant().logError(logger, expectedException);

    verify(mockLog, times(1)).error(eq("A problem"), eq(expectedException));
  }

  @Test
  public void shouldWriteErrorTextToLog() {

    //This isn't very nice, but don't know a better way to test logging
    LogDelegate mockLog = mock(LogDelegate.class);

    Logger logger = new Logger(mockLog);

    new SimpleLoggingAssistant().logError(logger, "A problem");

    verify(mockLog, times(1)).error(eq("A problem"));
  }
}
