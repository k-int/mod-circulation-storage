package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.impl.support.LoggingAssistant;
import org.folio.rest.impl.support.ResultHandler;
import org.folio.rest.impl.support.SimpleLoggingAssistant;
import org.folio.rest.impl.support.storage.PostgreSQLStorage;
import org.folio.rest.impl.support.storage.SingleResult;
import org.folio.rest.impl.support.storage.Storage;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.Requests;
import org.folio.rest.jaxrs.resource.RequestStorageResource;
import org.folio.rest.tools.utils.OutStream;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.folio.rest.impl.Headers.TENANT_HEADER;

public class RequestsAPI implements RequestStorageResource {

  private static final String REQUEST_TABLE = "request";
  private static final Logger log = LoggerFactory.getLogger(RequestsAPI.class);

  private final Storage storage;
  private final LoggingAssistant loggingAssistant;

  public RequestsAPI() {
    this(new PostgreSQLStorage(REQUEST_TABLE, Request.class),
      new SimpleLoggingAssistant());
  }

  public RequestsAPI(Storage storage, LoggingAssistant loggingAssistant) {
    this.storage = storage;
    this.loggingAssistant = loggingAssistant;
  }

  @Override
  public void deleteRequestStorageRequests(
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure = onExceptionalFailure(
      asyncResultHandler,
      "Unknown failure cause when attempting to delete all requests",
      DeleteRequestStorageRequestsResponse::withPlainInternalServerError);

    vertxContext.runOnContext(v -> {
      try {
        storage.deleteAll(vertxContext, tenantId,
          ResultHandler.filter(
            r -> respond(asyncResultHandler,
              DeleteRequestStorageRequestsResponse.withNoContent()),
            onExceptionalFailure));
      }
      catch(Exception e) {
        onExceptionalFailure.accept(e);
      }
    });
  }

  @Override
  public void getRequestStorageRequests(
    int offset,
    int limit,
    String query,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure = onExceptionalFailure(
      asyncResultHandler,
      "Unknown failure cause when attempting to get all requests",
      GetRequestStorageRequestsResponse::withPlainInternalServerError);

    try {
      vertxContext.runOnContext(v -> {
        try {
          storage.getAll(offset, limit, query, vertxContext, tenantId)
            .exceptionally(toNullResultFunction(onExceptionalFailure))
            .thenAccept(doNothingWhenNull(r -> {
                List<Request> requests = (List<Request>) r[0];

                Requests pagedRequests = new Requests();
                pagedRequests.setRequests(requests);
                pagedRequests.setTotalRecords((Integer) r[1]);

                respond(asyncResultHandler,
                  GetRequestStorageRequestsResponse.withJsonOK(pagedRequests));
              }));
        } catch (Exception e) {
          onExceptionalFailure.accept(e);
        }
      });
    } catch (Exception e) {
      onExceptionalFailure.accept(e);
    }
  }

  @Override
  public void postRequestStorageRequests(
    String lang,
    Request entity,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure =
      onExceptionalFailure(
        asyncResultHandler,
        "Unknown failure cause when attempting to create a request",
        PostRequestStorageRequestsResponse::withPlainInternalServerError);

    try {
      vertxContext.runOnContext(v -> {
        try {
          if(entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
          }

          storage.create(entity.getId(), entity, vertxContext, tenantId)
            .exceptionally(toNullResultFunction(onExceptionalFailure))
            .thenAccept(doNothingWhenNull(r -> {
              OutStream stream = new OutStream();
              stream.setData(entity);

              respond(asyncResultHandler,
                PostRequestStorageRequestsResponse.withJsonCreated(r, stream));
            }));
        } catch (Exception e) {
          onExceptionalFailure.accept(e);
        }
      });
    } catch (Exception e) {
      onExceptionalFailure.accept(e);
    }
  }

  @Override
  public void getRequestStorageRequestsByRequestId(
    String requestId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure =
      onExceptionalFailure(
        asyncResultHandler,
        "Unknown failure cause when attempting to get a request by ID",
        GetRequestStorageRequestsByRequestIdResponse::withPlainInternalServerError);

    try {
      vertxContext.runOnContext(v -> {
            try {
              storage.getById(requestId, vertxContext, tenantId)
                .exceptionally(toNullResultFunction(onExceptionalFailure))
                .thenAccept(doNothingWhenNull(r -> {
                  SingleResult<Request> result = SingleResult.from(r);

                  if (result.isFound()) {
                    respond(asyncResultHandler,
                      GetRequestStorageRequestsByRequestIdResponse.
                        withJsonOK(result.getResult()));
                  }
                  else {
                    respond(asyncResultHandler,
                      GetRequestStorageRequestsByRequestIdResponse.
                        withPlainNotFound("Not Found"));
                  }
                }));
        } catch (Exception e) {
          onExceptionalFailure.accept(e);
        }
      });
    } catch (Exception e) {
      onExceptionalFailure.accept(e);
    }
  }

  @Override
  public void deleteRequestStorageRequestsByRequestId(
    String requestId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure = onExceptionalFailure(
      asyncResultHandler,
      "Unknown failure cause when attempting to delete a request by ID",
      PostRequestStorageRequestsResponse::withPlainInternalServerError);

    try {
      vertxContext.runOnContext(v -> {
        try {
          storage.deleteById(requestId, vertxContext, tenantId,
            ResultHandler.filter(
              r -> respond(asyncResultHandler,
                DeleteRequestStorageRequestsByRequestIdResponse
                  .withNoContent()),
              onExceptionalFailure));
        } catch (Exception e) {
          onExceptionalFailure.accept(e);
        }
      });
    } catch (Exception e) {
      onExceptionalFailure.accept(e);
    }
  }

  @Override
  public void putRequestStorageRequestsByRequestId(
    String requestId,
    String lang, Request entity,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    Consumer<Throwable> onExceptionalFailure =
      onExceptionalFailure(
        asyncResultHandler,
        "Unknown failure cause when attempting to get a request by ID",
        GetRequestStorageRequestsByRequestIdResponse::withPlainInternalServerError);

    try {
      vertxContext.runOnContext(v -> {
        try {
          storage.getById(requestId, vertxContext, tenantId)
            .exceptionally(toNullResultFunction(onExceptionalFailure))
            .thenAccept(doNothingWhenNull(g -> {
              SingleResult<Request> getResult = SingleResult.from(g);

              try {
                if (getResult.isFound()) {
                  storage.update(requestId, entity, vertxContext, tenantId)
                    .exceptionally(toNullResultFunction(onExceptionalFailure))
                    .thenAccept(doNothingWhenNull(r -> respond(asyncResultHandler,
                      PutRequestStorageRequestsByRequestIdResponse.withNoContent())));
                } else {
                  storage.create(entity.getId(), entity, vertxContext, tenantId)
                    .exceptionally(toNullResultFunction(onExceptionalFailure))
                    .thenAccept(doNothingWhenNull(r -> respond(asyncResultHandler,
                      PutRequestStorageRequestsByRequestIdResponse.withNoContent())));
                }
              }
              catch (Exception e) {
                onExceptionalFailure.accept(e);
              }
            }));
        } catch (Exception e) {
          onExceptionalFailure.accept(e);
        }
      });
    } catch (Exception e) {
      onExceptionalFailure.accept(e);
    }
  }

  private void respond(
    Handler<AsyncResult<Response>> handler,
    Response response) {

    handler.handle(Future.succeededFuture(response));
  }

  private Consumer<Throwable> onExceptionalFailure(
    Handler<AsyncResult<Response>> responseHandler,
    String unknownFailureMessage,
    Function<String, Response> failureResponseCreator) {

    return e -> {
      logError(unknownFailureMessage, e);
      respondWithError(responseHandler, unknownFailureMessage, failureResponseCreator, e);
    };
  }

  private static <T> Function<Throwable, T> toNullResultFunction(
    Consumer<Throwable> exceptionConsumer) {
    return e -> {
      exceptionConsumer.accept(e);
      return null;
    };
  }

  private void respondWithError(
    Handler<AsyncResult<Response>> responseHandler,
    String unknownFailureMessage,
    Function<String, Response> failureResponseCreator,
    Throwable e) {

    if(e != null) {
      respond(responseHandler, failureResponseCreator.apply(e.getMessage()));
    }
    else {
      respond(responseHandler, failureResponseCreator.apply(unknownFailureMessage));
    }
  }

  private void logError(String unknownFailureMessage, Throwable e) {
    if(e != null) {
      loggingAssistant.logError(log, e);
    }
    else {
      loggingAssistant.logError(log, unknownFailureMessage);
    }
  }

  private <T> Consumer<T> doNothingWhenNull(Consumer<T> consumer) {
    return r -> {
      if(r == null) {
        return;
      }

      consumer.accept(r);
    };
  }
}
