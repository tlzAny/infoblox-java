package com.oneops.infoblox.tls;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * A {@link X509TrustManager} to skip certificate chains validations.
 *
 * @author Suresh
 */
public class TrustAllCertsManager implements X509TrustManager {
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) {}

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) {}

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[] {};
  }
}
