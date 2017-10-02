package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.rest.impl.Headers;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Stubber;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestsAPITest {

  private static final String TENANT_ID = "test_tenant";

  private static Vertx vertx;
  private static Context context;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx();
    context = vertx.getOrCreateContext();
  }

  @AfterClass
  public static void after() {
    vertx.close();
  }

  @Test
  public void shouldLogFailureDuringDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    fail(expectedException).when(mockStorage).deleteAll(any(), any(), any(), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockStorage, times(1)).deleteAll(any(), any(), any(), any());
  }

  @Test
  public void shouldLogUnexpectedExceptionDuringDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage).deleteAll(any(), any(), any(), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockStorage, times(1)).deleteAll(any(), any(), any(), any());
  }

  @Test
  public void shouldNotLogAnythinDuringSuccessfulDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    succeed(expectedException).when(mockStorage).deleteAll(any(), any(), any(), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(204));

    verify(mockLogAssistant, never()).logError(any(), eq(expectedException));
    verify(mockStorage, times(1)).deleteAll(any(), any(), any(), any());
  }

  private Stubber fail(Exception expectedException) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<String>> handler = invocation.getArgument(2);
      handler.handle(failure(expectedException));
      return null;
    });
  }

  private Stubber succeed(Exception expectedException) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<String>> handler = invocation.getArgument(2);
      handler.handle(success(""));
      return null;
    });
  }

  private AsyncResult<String> success(String result) {
    return new AsyncResult<String>() {
      @Override
      public String result() {
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

  private AsyncResult<String> failure(Exception expectedException) {
    return new AsyncResult<String>() {
      @Override
      public String result() {
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

  private Map<String, String> sampleHeaders() {
    HashMap<String, String> headers = new HashMap<>();

    headers.put(Headers.TENANT_HEADER, TENANT_ID);

    return headers;
  }

  private String sampleLanguage() {
    return "";
  }

  private static <T> Handler<T> complete(CompletableFuture<T> future) {
    return t -> future.complete(t);
  }
}
