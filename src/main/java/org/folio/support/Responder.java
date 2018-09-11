package org.folio.support;

import static io.vertx.core.Future.succeededFuture;

import java.util.function.Supplier;

import javax.ws.rs.core.Response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class Responder {
  private final Handler<AsyncResult<Response>> responseHandler;
  private final Supplier<Response> responseSupplier;

  public Responder(
    Handler<AsyncResult<Response>> responseHandler,
    Supplier<Response> responseSupplier) {

    this.responseHandler = responseHandler;
    this.responseSupplier = responseSupplier;
  }

  public void respond() {
    responseHandler.handle(succeededFuture(responseSupplier.get()));
  }
}
