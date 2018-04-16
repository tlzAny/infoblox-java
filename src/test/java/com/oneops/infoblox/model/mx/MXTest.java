package com.oneops.infoblox.model.mx;

import static com.oneops.infoblox.IBAEnvConfig.domain;
import static com.oneops.infoblox.IBAEnvConfig.isValid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oneops.infoblox.IBAEnvConfig;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.util.Dig;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Type;

/**
 * MX record tests.
 *
 * @author Suresh G
 */
@DisplayName("Infoblox MX record tests.")
class MXTest {

  private static InfobloxClient client;

  private final String mailServer = "oneops-test-mail." + domain();
  private final String fqdn = "oneops-test-mx1." + domain();
  private final String newFqdn = "oneops-test-mx1-mod." + domain();

  @BeforeAll
  static void setUp() {
    assumeTrue(isValid(), IBAEnvConfig::errMsg);
    client =
        InfobloxClient.builder()
            .endPoint(IBAEnvConfig.host())
            .userName(IBAEnvConfig.user())
            .password(IBAEnvConfig.password())
            .tlsVerify(false)
            .debug(true)
            .build();
  }

  /** Make sure to clean the A record before each test. */
  @BeforeEach
  void clean() throws IOException {
    client.deleteMXRec(fqdn);
    client.deleteMXRec(newFqdn);
  }

  @Test
  void create() throws IOException {
    List<MX> rec = client.getMXRec(fqdn);
    assertTrue(rec.isEmpty());

    // Creates MX Record
    String mailServer = "mail.oneops-test." + domain();
    MX mx = client.createMXRec(fqdn, mailServer, 1);
    assertEquals(mailServer, mx.mailExchanger());
    List<String> expected = singletonList(mailServer + ".");
    assertEquals(expected, Dig.lookup(fqdn, Type.MX));

    // Modify MX Record

    // Delete MX Record
  }
}
