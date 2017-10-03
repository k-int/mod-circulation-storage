package org.folio.rest.impl.support;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class ResultHandler {
  public static <T> Handler<AsyncResult<T>> filter(
    Consumer<T> onSuccess,
    Consumer<Throwable> onError) {

    return result -> {
      try {
        if (result.succeeded()) {
          onSuccess.accept(result.result());
          return;
        } else {
          onError.accept(result.cause());
          return;
        }
      } catch (Exception e) {
        onError.accept(e);
        return;
      }
    };
  }
}
