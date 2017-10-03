package org.folio.rest.impl.support.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public interface Storage {
  void deleteAll(
    Context vertxContext,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception;
}
