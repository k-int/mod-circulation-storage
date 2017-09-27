package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LogAssistant;
import org.folio.rest.impl.support.Repository;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.folio.rest.impl.Headers.TENANT_HEADER;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RequestsAPITest {

  @Test
  public void exceptionIsLoggedWhenDeleteAllRequestsFails()
    throws Exception {

    Context context = Vertx.vertx().getOrCreateContext();

    LogAssistant mockLogAssistant = mock(LogAssistant.class);

    Exception expectedException = new Exception();

    RequestsAPI requestsAPI = new RequestsAPI(mockLogAssistant,
      new FailingRepository(expectedException));

    HashMap<String, String> headers = new HashMap<>();

    headers.put(TENANT_HEADER, "");

    CompletableFuture handlerInvoked = new CompletableFuture();

    Handler<AsyncResult<Response>> asyncResultHandler = new Handler<AsyncResult<Response>>() {
      @Override
      public void handle(AsyncResult<Response> responseAsyncResult) {
        handlerInvoked.complete(null);
      }
    };

    requestsAPI.deleteRequestStorageRequests("", headers, asyncResultHandler, context);

    handlerInvoked.get(3, TimeUnit.SECONDS);

    verify(mockLogAssistant, times(1)).log(any(), any());
  }

  private class FailingRepository implements Repository {

    private final Throwable expectedException;

    public FailingRepository(Throwable expectedException) {

      this.expectedException = expectedException;
    }

    @Override
    public void delete(
      String tenantId,
      Context vertxContext,
      Consumer<Void> onSuccess,
      Consumer<Throwable> onFailure) {

      onFailure.accept(expectedException);

    }
  }
}
