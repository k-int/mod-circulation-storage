package org.folio.rest.impl.support;

import io.vertx.core.Context;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.TenantTool;

import java.util.function.Consumer;

public class PostgresRepository implements Repository {

  private final String REQUEST_TABLE = "request";

  @Override
  public void delete(
    String tenantId,
    Context vertxContext,
    Consumer<Void> onSuccess,
    Consumer<Throwable> onFailure) {

    try {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      postgresClient.mutate(String.format("TRUNCATE TABLE %s_%s.%s",
        tenantId, "mod_circulation_storage", REQUEST_TABLE),
        reply -> {
          try {
            if(reply.succeeded()) {
              onSuccess.accept(null);
            }
            else {
              onFailure.accept(reply.cause());
            }
          } catch (Exception e) {
            onFailure.accept(reply.cause());
          }
        });
    } catch(Throwable t) {
      onFailure.accept(t);
    }
  }
}
