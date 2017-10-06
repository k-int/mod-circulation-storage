package org.folio.rest.impl.support;

import io.vertx.core.logging.Logger;

public class SimpleLoggingAssistant implements LoggingAssistant {
  @Override
  public void logError(Logger logger, Throwable error) {
    logger.error(error.getMessage(), error);
  }

  public void logError(Logger logger, String error) {
    logger.error(error);
  }
}
