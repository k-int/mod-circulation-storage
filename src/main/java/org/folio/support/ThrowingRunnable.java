package org.folio.support;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
  void run() throws E;
}
