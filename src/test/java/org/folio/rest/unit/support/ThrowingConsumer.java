package org.folio.rest.unit.support;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
  void accept(T t) throws E;
}
