package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.rest.impl.Headers;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Stubber;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
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

    fail(expectedException, 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnknownFailureDuringDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    fail(null, 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to delete all requests"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnexpectedExceptionDuringDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldNotLogDuringSuccessfulDeleteAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    succeed("", 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.deleteRequestStorageRequests(
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(204));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogFailureDuringGetAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    fail(expectedException, 5).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.getRequestStorageRequests(
      0, 10, "",
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnknownFailureDuringGetAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    fail(null, 5).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.getRequestStorageRequests(
      0, 10, "",
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to get all requests"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnexpectedExceptionDuringGetAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.getRequestStorageRequests(
      0, 10, "",
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldNotLogDuringSuccessfulGetAll() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Object[] result = new Object[2];
    result[0] = new ArrayList<Request>();
    result[1] = 0;

    succeed(result, 5).when(mockStorage).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    CompletableFuture<AsyncResult<Response>> requestFinished = new CompletableFuture<>();

    requestsAPI.getRequestStorageRequests(
      0, 10, "",
      sampleLanguage(),
      sampleHeaders(),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(200));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  private <T> Stubber fail(Exception expectedException, int handlerAgumentIndex) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<T>> handler = invocation.getArgument(handlerAgumentIndex);
      handler.handle(failure(expectedException));
      return null;
    });
  }

  // Difficult to mock responding via a handler, as need to react to a void method
  private <T> Stubber succeed(T result, int handlerArgumentIndex) {
    return doAnswer(invocation -> {
      Handler<AsyncResult<T>> handler = invocation.getArgument(handlerArgumentIndex);
      handler.handle(success(result));
      return null;
    });
  }

  private <T> AsyncResult<T> success(T result) {
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

  private <T> AsyncResult<T> failure(Exception expectedException) {
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
