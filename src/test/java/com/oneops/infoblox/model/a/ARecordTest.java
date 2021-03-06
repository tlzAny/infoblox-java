package com.oneops.infoblox.model.a;

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
 * Address record tests.
 *
 * @author Suresh G
 */
@DisplayName("Infoblox address record tests.")
class ARecordTest {

  private static InfobloxClient client;

  private final String fqdn = "oneops-test-a1." + domain();
  private final String newFqdn = "oneops-test-a1-mod." + domain();

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

  /** Make sure to clean the A record before each test. */
  @BeforeEach
  void clean() throws IOException {
    client.deleteARec(fqdn);
    client.deleteARec(newFqdn);
  }

  @Test
  void create() throws Exception {
    List<ARec> rec = client.getARec(fqdn);
    assertTrue(rec.isEmpty());

    // Creates A Record
    String ip = "10.11.12.13";
    ARec aRec = client.createARec(fqdn, ip);
    assertEquals(ip, aRec.ipv4Addr());

    // Check the A record for given fqdn and IP.
    List<ARec> aRecs = client.getARec(fqdn, ip);
    assertEquals(1, aRecs.size());
    assertEquals(ip, aRecs.get(0).ipv4Addr());

    // Modify A Record
    List<ARec> modifedARec = client.modifyARec(fqdn, newFqdn);
    assertEquals(1, modifedARec.size());

    // Delete A Records
    List<String> delARec = client.deleteARec(fqdn);
    assertEquals(0, delARec.size());

    List<String> delNewARec = client.deleteARec(newFqdn);
    assertEquals(1, delNewARec.size());
  }
}
