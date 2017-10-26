package org.folio.rest.impl.support;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import javax.ws.rs.core.Response;
import java.util.function.Function;

public class ResponseAssistant {
  public static void respond(
    Handler<AsyncResult<Response>> handler,
    Response response) {

    handler.handle(Future.succeededFuture(response));
  }

  public static void respondWithError(
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
}
