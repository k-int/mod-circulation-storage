package org.folio.rest.unit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.folio.rest.impl.support.ResultHandler;
import org.folio.rest.unit.support.AbstractVertxUnitTest;
import org.folio.rest.unit.support.FakeAsyncResult;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResultHandlerTest extends AbstractVertxUnitTest {
  @Test
  public void shouldExecuteSuccessConsumerOnSuccess()
    throws InterruptedException, ExecutionException, TimeoutException {

    CompletableFuture<String> consumerCalled = new CompletableFuture<>();

    Handler<AsyncResult<String>> handler = ResultHandler.filter(
      v -> consumerCalled.complete(v),
      e -> { }
    );

    handler.handle(FakeAsyncResult.success("Yay"));

    String result = consumerCalled.get(1, TimeUnit.SECONDS);

    assertThat(result, is("Yay"));
  }

  @Test
  public void shouldExecuteFailureConsumerOnFailure()
    throws InterruptedException, ExecutionException, TimeoutException {

    CompletableFuture<Throwable> consumerCalled = new CompletableFuture<>();

    Exception expectedException = new Exception("Oh No");

    Handler<AsyncResult<String>> handler = ResultHandler.filter(
      v -> { },
      e -> consumerCalled.complete(e)
    );

    handler.handle(FakeAsyncResult.failure(expectedException));

    Throwable result = consumerCalled.get(1, TimeUnit.SECONDS);

    assertThat(result, is(expectedException));
  }

  @Test
  public void shouldExecuteFailureConsumerWhenNullExceptionThrownDuringHandling()
    throws InterruptedException, ExecutionException, TimeoutException {

    CompletableFuture<Throwable> consumerCalled = new CompletableFuture<>();

    Handler<AsyncResult<String>> handler = ResultHandler.filter(
      v -> { },
      e -> consumerCalled.complete(e)
    );

    handler.handle(null);

    Throwable result = consumerCalled.get(1, TimeUnit.SECONDS);

    assertThat(result, is(instanceOf(Exception.class)));
  }
}