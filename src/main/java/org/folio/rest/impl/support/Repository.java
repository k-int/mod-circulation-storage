package org.folio.rest.impl.support;

import io.vertx.core.Context;

import java.util.function.Consumer;

public interface Repository {
  void delete(
    String tenantId,
    Context vertxContext,
    Consumer<Void> onSuccess,
    Consumer<Throwable> onFailure);
}
