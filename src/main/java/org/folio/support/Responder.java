package org.folio.support;

import static io.vertx.core.Future.succeededFuture;

import java.util.function.Supplier;

import javax.ws.rs.core.Response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class Responder {
  public void respond(
    Handler<AsyncResult<Response>> responseHandler,
    Supplier<Response> responseSupplier) {

    responseHandler.handle(succeededFuture(responseSupplier.get()));
  }
}
