package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.unit.support.SampleParameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.folio.rest.unit.support.StubberAssistant.fail;
import static org.folio.rest.unit.support.StubberAssistant.succeed;
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
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
      SampleParameters.sampleLanguage(),
      SampleParameters.sampleHeaders(TENANT_ID),
      complete(requestFinished),
      context);

    AsyncResult<Response> response = requestFinished.get(3, TimeUnit.SECONDS);

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(200));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  private static <T> Handler<T> complete(CompletableFuture<T> future) {
    return t -> future.complete(t);
  }
}
