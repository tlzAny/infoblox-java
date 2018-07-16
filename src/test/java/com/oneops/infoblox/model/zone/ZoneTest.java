package com.oneops.infoblox.model.zone;

import static com.oneops.infoblox.IBAEnvConfig.domain;
import static com.oneops.infoblox.IBAEnvConfig.isValid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oneops.infoblox.IBAEnvConfig;
import com.oneops.infoblox.InfobloxClient;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Authoritative Zone tests.
 *
 * @author Suresh
 */
@DisplayName("Zone tests")
class ZoneTest {

  private static InfobloxClient client;

  private final String zoneName = domain();

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

  @Test
  void authZones() throws IOException {
    List<ZoneAuth> authZones = client.getAuthZones(zoneName);
    assertTrue(authZones.size() > 0);
    assertEquals(zoneName, authZones.get(0).fqdn());

    List<ZoneAuth> az = client.getAuthZones();
    assertTrue(az.size() > 0);
    System.out.println("List of all auth zone are,");
    az.forEach(System.out::println);
  }

  @Test
  void delegatedZones() throws IOException {
    List<ZoneDelegate> delegates = client.getDelegatedZones();
    assertTrue(delegates.size() > 0);

    String glbDomain = String.format("glb.%s", zoneName);
    List<ZoneDelegate> dz = client.getDelegatedZones(glbDomain);
    assertNotNull(dz);
    System.out.println("Delegated zone for " + glbDomain);
    dz.forEach(System.out::println);
  }
}
