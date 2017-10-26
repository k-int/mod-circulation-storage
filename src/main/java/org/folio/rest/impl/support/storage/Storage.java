package org.folio.rest.impl.support.storage;

import io.vertx.core.Context;
import io.vertx.ext.sql.UpdateResult;

import java.util.concurrent.CompletableFuture;

public interface Storage {
  CompletableFuture<String> create(
    String id,
    Object entity,
    Context context,
    String tenantId);

  CompletableFuture<Object[]> getById(
    String id,
    Context context,
    String tenantId);

  CompletableFuture<String> deleteAll(
    Context context,
    String tenantId);

  CompletableFuture<Object[]> getAll(
    int offset,
    int limit,
    String query,
    Context context,
    String tenantId);

  CompletableFuture<UpdateResult> deleteById(
    String id,
    Context context,
    String tenantId);

  CompletableFuture<UpdateResult> update(
    String id,
    Object entity,
    Context context,
    String tenantId);
}
