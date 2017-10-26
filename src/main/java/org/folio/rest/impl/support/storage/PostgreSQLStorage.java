package org.folio.rest.impl.support.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;
import org.folio.rest.impl.support.ResultHandler;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.utils.TenantTool;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

import java.util.concurrent.CompletableFuture;

public class PostgreSQLStorage implements Storage {
  private final String tableName;
  private final Class entityClass;

  public PostgreSQLStorage(String tableName, Class entityClass) {
    this.tableName = tableName;
    this.entityClass = entityClass;
  }

  @Override
  public void create(
    String id,
    Object entity,
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.save(tableName, id, entity, handleResult);
  }

  @Override
  public CompletableFuture<String> create(
    String id,
    Object entity,
    Context context,
    String tenantId) throws Exception {

    CompletableFuture<String> future = new CompletableFuture<>();

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.save(tableName, id, entity, ResultHandler.complete(future));

    return future;
  }

  @Override
  public void getById(
    String id,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.get(tableName, entityClass, equalityCriteria(id), true, false,
      handleResult);
  }

  @Override
  public CompletableFuture<Object[]> getById(
    String id,
    Context context,
    String tenantId) throws Exception {

    CompletableFuture<Object[]> future = new CompletableFuture<>();

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.get(tableName, entityClass, equalityCriteria(id), true, false,
      ResultHandler.complete(future));

    return future;
  }

  @Override
  public void deleteAll(
    Context context,
    String tenantId,
    Handler<AsyncResult<String>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.mutate(String.format("TRUNCATE TABLE %s_%s.%s",
      tenantId, "mod_circulation_storage", tableName),
      handleResult);
  }

  @Override
  public void getAll(
    int offset,
    int limit,
    String query,
    Context context,
    String tenantId,
    Handler<AsyncResult<Object[]>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    String[] fieldList = {"*"};

    CQL2PgJSON cql2pgJson = new CQL2PgJSON(String.format("%s.jsonb", tableName));
    CQLWrapper cql = new CQLWrapper(cql2pgJson, query)
      .setLimit(new Limit(limit))
      .setOffset(new Offset(offset));

    postgresClient.get(tableName, entityClass, fieldList, cql,
      true, false, handleResult);
  }

  @Override
  public void deleteById(
    String id,
    Context context,
    String tenantId,
    Handler<AsyncResult<UpdateResult>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.delete(tableName, equalityCriteria(id), handleResult);
  }

  @Override
  public void update(
    String id,
    Object entity,
    Context context,
    String tenantId,
    Handler<AsyncResult<UpdateResult>> handleResult) throws Exception {

    PostgresClient postgresClient = PostgresClient.getInstance(
      context.owner(), TenantTool.calculateTenantId(tenantId));

    postgresClient.update(tableName, entity, equalityCriteria(id), true,
      handleResult);
  }

  private Criterion equalityCriteria(String id) {
    Criteria a = new Criteria();

    a.addField("'id'");
    a.setOperation("=");
    a.setValue(id);

    return new Criterion(a);
  }
}
