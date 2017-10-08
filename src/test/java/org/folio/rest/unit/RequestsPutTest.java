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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.folio.rest.unit.support.HandlerCompletion.complete;
import static org.folio.rest.unit.support.HandlerCompletion.getOnCompletion;
import static org.folio.rest.unit.support.StubberAssistant.succeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RequestsPutTest extends AbstractVertxUnitTest {

  @Test
  public void shouldRespondCreatedWhenRequestCreated() throws Exception {
    LoggingAssistant mockLogAssistant = mock(LoggingAssistant.class);
    Storage mockStorage = mock(Storage.class);

    RequestsAPI requestsAPI = new RequestsAPI(mockStorage, mockLogAssistant);

    String expectedId = UUID.randomUUID().toString();
    Request exampleRequest = new Request().withId(expectedId);

    succeed(noRecordsFound(), 3).when(mockStorage)
      .getById(eq(expectedId), eq(context), eq(TENANT_ID), any());

    succeed("", 4).when(mockStorage)
      .create(anyString(), eq(exampleRequest), eq(context), eq(TENANT_ID), any());

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
  }

  private Object[] noRecordsFound() {
    Object[] result = new Object[2];
    result[0] = new ArrayList<>(Arrays.asList());
    result[1] = 0;
    return result;
  }
}
