package com.oneops.infoblox.model.mx;

import static com.oneops.infoblox.IBAEnvConfig.domain;
import static com.oneops.infoblox.IBAEnvConfig.isValid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oneops.infoblox.IBAEnvConfig;
import com.oneops.infoblox.InfobloxClient;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MX record tests.
 *
 * @author Suresh G
 */
@DisplayName("Infoblox MX record tests.")
class MXTest {

  private static InfobloxClient client;

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
            .ttl(1)
            .tlsVerify(false)
            .debug(true)
            .build();
  }

  /** Make sure to clean the MX record before each test. */
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

    // Check the A record for given fqdn and IP.
    List<MX> mxRecs = client.getMXRec(fqdn, mailServer);
    assertEquals(1, mxRecs.size());
    assertEquals(mailServer, mxRecs.get(0).mailExchanger());

    // Modify MX Record
    List<MX> mxList = client.modifyMXRec(fqdn, newFqdn);
    assertEquals(1, mxList.size());
    assertEquals(mailServer, mxList.get(0).mailExchanger());

    // Delete MX Record
    List<String> deleteMXRec = client.deleteMXRec(fqdn);
    assertEquals(0, deleteMXRec.size());

    List<String> deleteMXRec1 = client.deleteMXRec(newFqdn);
    assertEquals(1, deleteMXRec1.size());
  }
}
