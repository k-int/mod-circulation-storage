package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
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

public class RequestsDeleteAllTest extends AbstractVertxUnitTest {

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    fail(expectedException, 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.deleteRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnknownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    fail(null, 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.deleteRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to delete all requests"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.deleteRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondOkWhenAllRequestsDeleted() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    succeed("", 2).when(mockStorage).deleteAll(eq(context), eq(TENANT_ID), any());

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.deleteRequestStorageRequests(
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(204));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).deleteAll(eq(context), eq(TENANT_ID), any());
  }
}
