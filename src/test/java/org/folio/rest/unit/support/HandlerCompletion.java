package org.folio.rest.unit.support;

import io.vertx.core.Handler;
import org.folio.rest.support.ThrowingConsumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HandlerCompletion {
  public static <T> Handler<T> complete(CompletableFuture<T> future) {
    return t -> {
      future.complete(t);
    };
  }

  public static <T, E extends Exception> T getOnCompletion(
    ThrowingConsumer<CompletableFuture<T>, E> throwingConsumer)
    throws InterruptedException, ExecutionException, TimeoutException {

      CompletableFuture<T> finished = new CompletableFuture<>();

      try {
        throwingConsumer.accept(finished);
      } catch (Exception ex) {
        finished.completeExceptionally(ex);
      }

      return finished.get(3, TimeUnit.SECONDS);
    }
}
