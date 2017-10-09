package org.folio.rest.unit.support;

import io.vertx.core.json.JsonObject;
import org.folio.rest.tools.utils.ObjectMapperTool;

public class JsonSerialization {
  public static <T> T fromJson(Class<T> type, JsonObject record) throws java.io.IOException {
    return ObjectMapperTool.getMapper().readValue(
      record.encode(), type);
  }
}
