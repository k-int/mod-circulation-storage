package org.folio.rest.impl.support;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.folio.rest.support.ThrowingConsumer;

import java.util.function.Consumer;

public class ResultHandler {
  private ResultHandler() {}

  public static <T> Handler<AsyncResult<T>> filter(
    ThrowingConsumer<T, Exception> onSuccess,
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
