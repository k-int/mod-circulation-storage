package org.folio.rest.impl.support;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumerAssistant {
  public static <T> Function<Throwable, T> toNullResultFunction(
    Consumer<Throwable> exceptionConsumer) {
    return e -> {
      exceptionConsumer.accept(e);
      return null;
    };
  }

  public static <T> Consumer<T> doNothingWhenNull(Consumer<T> consumer) {
    return r -> {
      if(r == null) {
        return;
      }

      consumer.accept(r);
    };
  }
}
