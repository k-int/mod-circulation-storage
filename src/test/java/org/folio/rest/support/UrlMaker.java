package org.folio.rest.support;

import java.net.MalformedURLException;
import java.net.URL;

@FunctionalInterface
public interface UrlMaker {
  URL combine(String subPath) throws MalformedURLException;
}
