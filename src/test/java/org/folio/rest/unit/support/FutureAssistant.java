package org.folio.rest.unit.support;

import java.util.concurrent.CompletableFuture;

public class FutureAssistant {
  public static <T> CompletableFuture<T> exceptionalFuture(
    Exception exception) {

    CompletableFuture<T> future = new CompletableFuture<>();

    future.completeExceptionally(exception);

    return future;
  }
}
