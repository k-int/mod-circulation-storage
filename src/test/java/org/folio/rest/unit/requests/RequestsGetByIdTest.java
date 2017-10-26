package org.folio.rest.unit.requests;

import io.vertx.core.AsyncResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.folio.rest.unit.support.SampleParameters;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.folio.rest.unit.support.FakeMultipleRecordResult.noRecordsFound;
import static org.folio.rest.unit.support.FakeMultipleRecordResult.singleRecordFound;
import static org.folio.rest.unit.support.FutureAssistant.exceptionalFuture;
import static org.folio.rest.unit.support.HandlerCompletion.complete;
import static org.folio.rest.unit.support.HandlerCompletion.getOnCompletion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestsGetByIdTest extends AbstractVertxUnitTest {

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Exception expectedException = new Exception("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(expectedException));

    AsyncResult<Response> response = getOnCompletion(f -> {
      requestsAPI.getRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context);
    });

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).getById(eq(expectedId), eq(context), eq(TENANT_ID));
  }

  @Test
  @Ignore("Cannot complete a CompletableFuture with null exception")
  public void shouldRespondWithErrorWhenUnknownFailureOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(null));

    AsyncResult<Response> response = getOnCompletion(f -> {
      requestsAPI.getRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context);
    });

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to get a request by ID"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getById(eq(expectedId), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccurs() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Exception expectedException = new Exception("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenThrow(expectedException);

    AsyncResult<Response> response = getOnCompletion(f -> {
      requestsAPI.getRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context);
    });

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1)).getById(eq(expectedId), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondOkWhenRequestFound() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(singleRecordFound(new Request())));

    AsyncResult<Response> response = getOnCompletion(f -> {
      requestsAPI.getRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context);
    });

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(200));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getById(eq(expectedId), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondNotFoundWhenNoRequestFound() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(noRecordsFound()));

    AsyncResult<Response> response = getOnCompletion(f -> {
      requestsAPI.getRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context);
    });

    assertThat(String.format("Should be not found: %s", response.cause()),
      response.result().getStatus(), is(404));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1)).getById(eq(expectedId), eq(context), eq(TENANT_ID));
  }
}
