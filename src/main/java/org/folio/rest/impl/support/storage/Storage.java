package org.folio.rest.impl.support.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public interface Storage {
  void create(
    String id,
    Object entity,
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception;

  void getById(
    String id,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception;

  void deleteAll(
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception;

  void getAll(
    int offset,
    int limit,
    String query,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception;
}
