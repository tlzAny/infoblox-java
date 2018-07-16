package com.oneops.infoblox.tls;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * An SSL socket factory that disables <b>SNIExtension</b> by passing null host.
 *
 * @author Suresh
 */
public class SNIDisabledSocketFactory extends DelegatingSSLSocketFactory {

  public SNIDisabledSocketFactory(SSLSocketFactory delegate) {
    super(delegate);
  }

  @Override
  public SSLSocket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException {
    return super.createSocket(socket, null, port, autoClose);
  }
}
