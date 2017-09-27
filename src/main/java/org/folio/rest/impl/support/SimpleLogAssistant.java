package org.folio.rest.impl.support;

import io.vertx.core.logging.Logger;

public class SimpleLogAssistant implements LogAssistant {
  @Override
  public void log(Logger logger, Throwable throwable) {
    logger.error(throwable.getMessage(), throwable);
  }
}
