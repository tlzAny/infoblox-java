package com.oneops.infoblox.model.ttl;

import static com.oneops.infoblox.IBAEnvConfig.domain;
import static com.oneops.infoblox.IBAEnvConfig.isValid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oneops.infoblox.IBAEnvConfig;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.a.ARec;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Record TTL Tests.
 *
 * @author Suresh G
 */
@DisplayName("DNS Record TTL Test")
class TTLTest {

  private static InfobloxClient client;

  private final String fqdn = "oneops-test-ttl1." + domain();

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
  }

  @Test
  void ttlTest() throws Exception {
    int ttl = 1;
    int newTTL = 5;

    assertEquals(ttl, client.ttl());

    List<ARec> rec = client.getARec(fqdn);
    assertTrue(rec.isEmpty());

    // Creates A Record
    String ip = "10.11.12.14";
    ARec aRec = client.createARec(fqdn, ip);
    assertEquals(ttl, aRec.ttl());

    // Check the A record for given fqdn and IP.
    List<ARec> aRecs = client.getARec(fqdn, ip);
    assertEquals(1, aRecs.size());
    assertEquals(ttl, aRecs.get(0).ttl());

    // Modify A Record TTL
    client.modifyTTL(aRec, newTTL);
    List<ARec> modARec = client.getARec(fqdn, ip);
    assertEquals(1, modARec.size());
    assertEquals(newTTL, modARec.get(0).ttl());

    // Delete A Record
    List<String> delARec = client.deleteARec(fqdn);
    assertEquals(1, delARec.size());
  }
}
