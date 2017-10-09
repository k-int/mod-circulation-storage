package org.folio.rest.unit.support;

import java.util.ArrayList;
import java.util.Arrays;

public class FakeMultipleRecordResult {

  public static Object[] noRecordsFound() {
    Object[] result = new Object[2];
    result[0] = new ArrayList<>(Arrays.asList());
    result[1] = 0;
    return result;
  }

  public static <T> Object[] singleRecordFound(T record) {
    Object[] result = new Object[2];
    result[0] = new ArrayList<>(Arrays.asList(record));
    result[1] = 1;
    return result;
  }
}
