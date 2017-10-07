package org.folio.rest.unit.support;

import io.vertx.core.AsyncResult;

public class FakeAsyncResult {
  public static <T> AsyncResult<T> success(T result) {
    return new AsyncResult<T>() {
      @Override
      public T result() {
        return result;
      }

      @Override
      public Throwable cause() {
        return null;
      }

      @Override
      public boolean succeeded() {
        return true;
      }

      @Override
      public boolean failed() {
        return false;
      }
    };

  }

  public static <T> AsyncResult<T> failure(Exception expectedException) {
    return new AsyncResult<T>() {
      @Override
      public T result() {
        return null;
      }

      @Override
      public Throwable cause() {
        return expectedException;
      }

      @Override
      public boolean succeeded() {
        return false;
      }

      @Override
      public boolean failed() {
        return true;
      }
    };
  }
}
