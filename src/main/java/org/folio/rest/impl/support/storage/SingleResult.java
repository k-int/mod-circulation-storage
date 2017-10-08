package org.folio.rest.impl.support.storage;

import java.util.List;

public class SingleResult<T> {
  private final boolean found;
  private final T result;

  public static <T> SingleResult<T> found(T result) {
    return new SingleResult<>(true, result);
  }

  public static <T> SingleResult<T> notFound() {
    return new SingleResult<>(false, null);
  }

  public static <T> SingleResult<T> from(Object[] response) {
    List<T> results = (List<T>) response[0];

    if (results.size() == 1) {
      return found(results.get(0));
    } else {
      return notFound();
    }
  }

  private SingleResult(boolean found, T response) {
    this.found = found;
    this.result = response;
  }

  public T getResult() {
    return result;
  }

  public boolean isFound() {
    return found;
  }
}
