package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.folio.rest.unit.support.SampleParameters;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.folio.rest.unit.support.HandlerCompletion.complete;
import static org.folio.rest.unit.support.HandlerCompletion.getOnCompletion;
import static org.folio.rest.unit.support.StubberAssistant.fail;
import static org.folio.rest.unit.support.StubberAssistant.succeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestsPostTest extends AbstractVertxUnitTest {

  @Test
  public void shouldLogFailureDuringPost() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    Request exampleRequest = new Request();

    fail(expectedException, 4).when(mockStorage)
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.postRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnknownFailureDuringPost() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Request exampleRequest = new Request();

    fail(null, 4).when(mockStorage)
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.postRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to create a request"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldLogUnexpectedExceptionDuringPost() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    Request exampleRequest = new Request();

    doThrow(expectedException).when(mockStorage)
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.postRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldNotLogDuringSuccessfulPost() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Request exampleRequest = new Request();

    succeed("", 4).when(mockStorage)
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.postRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(201));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());
  }
}
