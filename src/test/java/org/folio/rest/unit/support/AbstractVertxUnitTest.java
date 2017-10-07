package org.folio.rest.unit.support;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractVertxUnitTest {
  protected static final String TENANT_ID = "test_tenant";
  protected static Context context;

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx();
    context = vertx.getOrCreateContext();
  }

  @AfterClass
  public static void after() {
    vertx.close();
  }
}
