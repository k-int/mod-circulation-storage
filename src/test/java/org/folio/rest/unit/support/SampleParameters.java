package org.folio.rest.unit.support;

import org.folio.rest.impl.Headers;

import java.util.HashMap;
import java.util.Map;

public class SampleParameters {
  public static Map<String, String> sampleHeaders(String tenantId) {
    HashMap<String, String> headers = new HashMap<>();

    headers.put(Headers.TENANT_HEADER, tenantId);

    return headers;
  }

  public static String sampleLanguage() {
    return "";
  }
}
