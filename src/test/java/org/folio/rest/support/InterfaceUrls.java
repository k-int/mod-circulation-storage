package org.folio.rest.support;

import org.folio.rest.api.StorageTestSuite;

import java.net.MalformedURLException;
import java.net.URL;

public class InterfaceUrls {
  public static URL loanStorageUrl(String subPath)
    throws MalformedURLException {

    return StorageTestSuite.storageUrl("/loan-storage/loans" + subPath);
  }
}
