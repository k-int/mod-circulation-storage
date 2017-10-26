package org.folio.rest.unit.requests;

import io.vertx.core.AsyncResult;
import io.vertx.ext.sql.UpdateResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.support.builders.RequestRequestBuilder;
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
import static org.folio.rest.unit.support.JsonSerialization.fromJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RequestsPutTest extends AbstractVertxUnitTest {

  @Test
  public void shouldRespondWithNoContentWhenRequestCreated() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(noRecordsFound()));

    when(mockStorage.create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(""));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(204));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithNoContentWhenRequestUpdated() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(singleRecordFound(exampleRequest)));

    when(mockStorage.update(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(new UpdateResult()));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(String.format("Should succeed: %s", response.cause()),
      response.result().getStatus(), is(204));

    verify(mockLogAssistant, never()).logError(any(), any(String.class));
    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new Exception("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(expectedException));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  @Ignore("Cannot complete a CompletableFuture with null exception")
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(null));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to create or update a request by ID"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new RuntimeException("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenThrow(expectedException);

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new Exception("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(noRecordsFound()));

    when(mockStorage.create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(expectedException));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  @Ignore("Cannot complete a CompletableFuture with null exception")
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(noRecordsFound()));

    when(mockStorage.create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(null));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to create or update a request by ID"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new RuntimeException("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(noRecordsFound()));

    when(mockStorage.create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenThrow(expectedException);

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new Exception("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(singleRecordFound(exampleRequest)));

    when(mockStorage.update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(expectedException));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  @Ignore("Cannot complete a CompletableFuture with null exception")
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(singleRecordFound(exampleRequest)));

    when(mockStorage.update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenReturn(exceptionalFuture(null));

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(),
      eq("Unknown failure cause when attempting to create or update a request by ID"));

    verify(mockLogAssistant, never()).logError(any(), any(Throwable.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();

    Request exampleRequest = fromJson(Request.class,
      new RequestRequestBuilder().withId(expectedId).create());

    Exception expectedException = new RuntimeException("Sample Failure");

    when(mockStorage.getById(eq(expectedId), eq(context), eq(TENANT_ID)))
      .thenReturn(CompletableFuture.completedFuture(singleRecordFound(exampleRequest)));

    when(mockStorage.update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID)))
      .thenThrow(expectedException);

    AsyncResult<Response> response = getOnCompletion(f ->
      requestsAPI.putRequestStorageRequestsByRequestId(
        expectedId,
        SampleParameters.sampleLanguage(),
        exampleRequest,
        SampleParameters.sampleHeaders(TENANT_ID),
        complete(f),
        context));

    assertThat(response.result().getStatus(), is(500));

    verify(mockLogAssistant, times(1)).logError(any(), eq(expectedException));
    verify(mockLogAssistant, never()).logError(any(), any(String.class));

    verify(mockStorage, times(1))
      .getById(eq(expectedId), eq(context), eq(TENANT_ID));

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID));

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID));
  }
}
