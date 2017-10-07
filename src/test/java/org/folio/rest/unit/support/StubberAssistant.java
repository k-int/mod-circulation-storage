package org.folio.rest.unit.support;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.mockito.stubbing.Stubber;

import static org.mockito.Mockito.doAnswer;

public class StubberAssistant {
  public static <T> Stubber fail(Exception expectedException, int handlerAgumentIndex) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<T>> handler = invocation.getArgument(handlerAgumentIndex);
      handler.handle(FakeAsyncResult.failure(expectedException));
      return null;
    });
  }

  // Difficult to mock responding via a handler, as need to react to a void method
  public static <T> Stubber succeed(T result, int handlerArgumentIndex) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<T>> handler = invocation.getArgument(handlerArgumentIndex);
      handler.handle(FakeAsyncResult.success(result));
      return null;
    });
  }
}
