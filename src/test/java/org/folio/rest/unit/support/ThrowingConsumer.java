package org.folio.rest.unit.support;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
  void accept(CompletableFuture<T> t) throws E;
}
