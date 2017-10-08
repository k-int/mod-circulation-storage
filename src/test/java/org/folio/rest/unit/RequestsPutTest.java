package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.ext.sql.UpdateResult;
import org.folio.rest.impl.RequestsAPI;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.folio.rest.unit.support.SampleParameters;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.folio.rest.unit.support.HandlerCompletion.complete;
import static org.folio.rest.unit.support.HandlerCompletion.getOnCompletion;
import static org.folio.rest.unit.support.StubberAssistant.fail;
import static org.folio.rest.unit.support.StubberAssistant.succeed;
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
    Request exampleRequest = new Request().withId(expectedId);

    succeed(noRecordsFound(), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    succeed("", 4).when(mockStorage)
      .create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithNoContentWhenRequestUpdated() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    succeed(singleRecordFound(exampleRequest), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    succeed(new UpdateResult(), 4).when(mockStorage)
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    fail(expectedException, 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    fail(null, 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringGetById() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    doThrow(expectedException).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    succeed(noRecordsFound(), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    fail(expectedException, 4).when(mockStorage)
      .create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    succeed(noRecordsFound(), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    fail(null, 4).when(mockStorage)
      .create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringCreation() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    succeed(noRecordsFound(), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    doThrow(expectedException).when(mockStorage)
      .create(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenKnownFailureOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    succeed(singleRecordFound(exampleRequest), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    fail(expectedException, 4).when(mockStorage)
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnknownFailureOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    succeed(singleRecordFound(exampleRequest), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    fail(null, 4).when(mockStorage)
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  @Test
  public void shouldRespondWithErrorWhenUnexpectedExceptionOccursDuringUpdate() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    Exception expectedException = new Exception("Sample Failure");

    succeed(singleRecordFound(exampleRequest), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    doThrow(expectedException).when(mockStorage)
      .update(eq(expectedId), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, times(1))
      .update(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());

    verify(mockStorage, never())
      .create(eq(expectedId), any(), eq(context), eq(TENANT_ID), any());
  }

  private Object[] noRecordsFound() {
    Object[] result = new Object[2];
    result[0] = new ArrayList<>(Arrays.asList());
    result[1] = 0;
    return result;
  }

  private <T> Object[] singleRecordFound(T record) {
    Object[] result = new Object[2];
    result[0] = new ArrayList<>(Arrays.asList(record));
    result[1] = 1;
    return result;
  }
}
