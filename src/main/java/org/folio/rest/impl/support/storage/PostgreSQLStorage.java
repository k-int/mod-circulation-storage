package org.folio.rest.impl.support.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.TenantTool;

public class PostgreSQLStorage implements Storage {
  private final String tableName;

  public PostgreSQLStorage(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public void deleteAll(
    Context vertxContext,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.mutate(String.format("TRUNCATE TABLE %s_%s.%s",
      tenantId, "mod_circulation_storage", tableName),
      handleResult);
  }
}
