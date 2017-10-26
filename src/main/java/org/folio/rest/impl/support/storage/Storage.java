package org.folio.rest.impl.support.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.concurrent.CompletableFuture;

public interface Storage {
  void create(
    String id,
    Object entity,
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception;

  CompletableFuture<String> create(
    String id,
    Object entity,
    Context context,
    String tenantId);

  void getById(
    String id,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception;

  CompletableFuture<Object[]> getById(
    String id,
    Context context,
    String tenantId);

  void deleteAll(
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception;

  CompletableFuture<String> deleteAll(
    Context context,
    String tenantId);

  void getAll(
    int offset,
    int limit,
    String query,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception;

  CompletableFuture<Object[]> getAll(
    int offset,
    int limit,
    String query,
    Context context,
    String tenantId);

  void deleteById(
    String id,
    Context context,
    String tenantId,
    Handler<AsyncResult<UpdateResult>> handleResult) throws Exception;

  CompletableFuture<UpdateResult> deleteById(
    String id,
    Context context,
    String tenantId);

  void update(
    String id,
    Object entity,
    Context context,
    String tenantId,
    Handler<AsyncResult<UpdateResult>> handleResult) throws Exception;

  CompletableFuture<UpdateResult> update(
    String id,
    Object entity,
    Context context,
    String tenantId);
}
