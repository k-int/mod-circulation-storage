package org.folio.rest.unit.requests;

import io.vertx.core.AsyncResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.folio.rest.unit.support.SampleParameters;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.folio.rest.unit.support.FakeMultipleRecordResult.noRecordsFound;
import static org.folio.rest.unit.support.HandlerCompletion.complete;
import static org.folio.rest.unit.support.HandlerCompletion.getOnCompletion;
import static org.folio.rest.unit.support.StubberAssistant.fail;
import static org.folio.rest.unit.support.StubberAssistant.succeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestsGetAllTest extends AbstractVertxUnitTest {

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    fail(expectedException, 5).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(
      f -> requestsAPI.getRequestStorageRequests(
        0, 10, "",
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnknownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    fail(null, 5).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(
      f -> requestsAPI.getRequestStorageRequests(
        0, 10, "",
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to get all requests"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.getRequestStorageRequests(
        0, 10, "",
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondOkWhenRequestsFound() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    succeed(noRecordsFound(), 5).when(mockStorage)
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.getRequestStorageRequests(
        0, 10, "",
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(200));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getAll(eq(0), eq(10), eq(""), eq(context), eq(TENANT_ID), any());
  }
}
