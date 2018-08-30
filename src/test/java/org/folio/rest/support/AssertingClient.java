package org.folio.rest.support;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.folio.rest.support.builders.Builder;

import io.vertx.core.json.JsonObject;

public class AssertingClient {
  private final HttpClient client;
  private final String tenantId;
  private final UrlMaker urlMaker;

  public AssertingClient(
    HttpClient client,
    String tenantId,
    UrlMaker urlMaker) {

    this.client = client;
    this.tenantId = tenantId;
    this.urlMaker = urlMaker;
  }

  public JsonResponse attemptCreateOrReplace(
    String id,
    JsonObject representation)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    final URL location = urlMaker.combine(String.format("/%s", id));

    CompletableFuture<JsonResponse> putCompleted = new CompletableFuture<>();

    client.put(location, representation, tenantId,
      ResponseHandler.json(putCompleted));

    return putCompleted.get(5, TimeUnit.SECONDS);
  }

  public JsonResponse attemptCreateOrReplace(
    String id,
    Builder builder)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    return attemptCreateOrReplace(id, builder.create());
  }

  public IndividualResource createOrReplace(
    String id,
    Builder builder)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    return createOrReplace(id, builder.create());
  }

  public IndividualResource createOrReplace(
    String id,
    JsonObject representation)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    final JsonResponse putResponse = attemptCreateOrReplace(id, representation);

    assertThat(String.format("Failed to create or replace: %s", putResponse.getBody()),
      putResponse.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));

    return get(id);
  }

  private IndividualResource get(String id)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    final URL location = urlMaker.combine(String.format("/%s", id));

    CompletableFuture<JsonResponse> getCompleted = new CompletableFuture<>();

    client.get(location, tenantId, ResponseHandler.json(getCompleted));

    final JsonResponse replaceResponse = getCompleted.get(5, TimeUnit.SECONDS);

    assertThat(replaceResponse.getStatusCode(), is(200));

    return new IndividualResource(replaceResponse);
  }

  public JsonResponse attemptCreate(JsonObject representation)
    throws MalformedURLException,
      InterruptedException,
      ExecutionException,
      TimeoutException {

    CompletableFuture<JsonResponse> createCompleted = new CompletableFuture<>();

    client.post(urlMaker.combine(""), representation, tenantId,
      ResponseHandler.json(createCompleted));

    return createCompleted.get(5, TimeUnit.SECONDS);
  }

  public JsonResponse attemptCreate(Builder builder)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    return attemptCreate(builder.create());
  }

  public IndividualResource create(JsonObject representation)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    final JsonResponse response = attemptCreate(representation);

    assertThat(String.format("Failed to create: %s", response.getBody()),
      response.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));

    return new IndividualResource(response);
  }

  public JsonResponse getById(UUID id)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    URL getInstanceUrl = urlMaker.combine(String.format("/%s", id));

    CompletableFuture<JsonResponse> getCompleted = new CompletableFuture<>();

    client.get(getInstanceUrl, tenantId, ResponseHandler.json(getCompleted));

    return getCompleted.get(5, TimeUnit.SECONDS);
  }
}
