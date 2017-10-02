package org.folio.rest.impl.support;

import io.vertx.core.logging.Logger;

public interface LoggingAssistant {
  void logError(Logger logger, Throwable error);
}
