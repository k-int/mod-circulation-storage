package org.folio.rest.impl.support;

import io.vertx.core.logging.Logger;

public class SimpleLoggingAssistant {
  public void logError(Logger logger, Throwable error) {
    logger.error(error.getMessage(), error);
  }
}
