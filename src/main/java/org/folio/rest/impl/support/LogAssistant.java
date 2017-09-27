package org.folio.rest.impl.support;

import io.vertx.core.logging.Logger;

public interface LogAssistant {
  void log(Logger logger, Throwable throwable);
}
