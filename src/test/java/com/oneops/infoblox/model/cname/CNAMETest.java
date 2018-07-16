package com.oneops.infoblox.model.cname;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Canonical record tests.
 *
 * @author Suresh G
 */
@DisplayName("Infoblox CNAME record tests.")
class CNAMETest {

  private static InfobloxClient client;

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

  @ParameterizedTest
  @ValueSource(strings = {"oneops-test"})
  void create(String prefix) throws IOException {

    final String canonicalName = String.format("%s.%s", prefix, domain());
    final String alias = String.format("%s-cname1.%s", prefix, domain());
    final String newAlias = String.format("%s-cname1-mod.%s", prefix, domain());

    // Clean it.
    client.deleteCNameRec(alias);
    client.deleteCNameRec(newAlias);

    List<CNAME> rec = client.getCNameRec(alias);
    assertTrue(rec.isEmpty());

    // Creates CNAME Record
    CNAME cname = client.createCNameRec(alias, canonicalName);
    assertEquals(canonicalName, cname.canonical());

    List<CNAME> cNameRec = client.getCNameRec(alias);
    assertEquals(1, cNameRec.size());
    assertEquals(canonicalName, cNameRec.get(0).canonical());

    // Modify CNAME Record
    List<CNAME> modCName = client.modifyCNameRec(alias, newAlias);
    assertEquals(1, modCName.size());

    // Delete CNAME Record
    List<String> delCName = client.deleteCNameRec(alias);
    assertEquals(0, delCName.size());

    List<String> delNewCName = client.deleteCNameRec(newAlias);
    assertEquals(1, delNewCName.size());
  }
}
