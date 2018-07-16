package com.oneops.infoblox.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xbill.DNS.Type.A;
import static org.xbill.DNS.Type.AAAA;
import static org.xbill.DNS.Type.CNAME;
import static org.xbill.DNS.Type.MX;
import static org.xbill.DNS.Type.NS;
import static org.xbill.DNS.Type.PTR;
import static org.xbill.DNS.Type.SRV;
import static org.xbill.DNS.Type.TXT;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

/**
 * A utility class for querying Domain Name System (DNS) servers.
 *
 * @author Suresh
 */
public class Dig {

  /**
   * Queries the DNS server for the given resource name and record type using a resolver.
   *
   * @param fqdn name of the resource record that is to be looked up.
   * @param queryType {@link Type} indicates what type of query is required, CNAME, A, MX, etc.
   * @param resolver DNS resolver to query.
   * @return DNS lookup result.
   * @throws IOException if a problem occurred talking to DNS.
   */
  private static List<String> lookup0(String fqdn, int queryType, String resolver)
      throws IOException {

    // Log the DNS query
    StringBuilder buf = new StringBuilder();
    buf.append("[").append(Type.string(queryType)).append("] ").append("-> dig +short ");
    if (resolver != null) {
      buf.append("@").append(resolver).append(" ");
    }
    buf.append(Type.string(queryType)).append(" ").append(fqdn);
    System.out.println(buf.toString());

    Lookup dig = new Lookup(fqdn, queryType);
    dig.setResolver(new SimpleResolver(resolver));
    // dig.setSearchPath(new String[] {domain()});
    Record[] records = dig.run();
    List<String> answer = Collections.emptyList();

    if (records != null) {
      switch (queryType) {
        case A:
          answer =
              Arrays.stream(records)
                  .map(ARecord.class::cast)
                  .map(a -> a.getAddress().getHostAddress())
                  .collect(Collectors.toList());
          break;

        case AAAA:
          answer =
              Arrays.stream(records)
                  .map(AAAARecord.class::cast)
                  .map(a -> a.getAddress().getHostAddress())
                  .collect(Collectors.toList());
          break;

        case CNAME:
          answer =
              Arrays.stream(records)
                  .map(CNAMERecord.class::cast)
                  .map(c -> c.getTarget().toString())
                  .collect(Collectors.toList());
          break;

        case MX:
          answer =
              Arrays.stream(records)
                  .map(MXRecord.class::cast)
                  .map(m -> m.getTarget().toString())
                  .collect(Collectors.toList());
          break;

        case NS:
          answer =
              Arrays.stream(records)
                  .map(NSRecord.class::cast)
                  .map(n -> n.getTarget().toString())
                  .collect(Collectors.toList());
          break;

        case SRV:
          answer =
              Arrays.stream(records)
                  .map(SRVRecord.class::cast)
                  .map(s -> s.getTarget().toString())
                  .collect(Collectors.toList());
          break;

        case TXT:
          //noinspection unchecked
          answer =
              Arrays.stream(records)
                  .map(TXTRecord.class::cast)
                  .flatMap(t -> ((List<String>) t.getStrings()).stream())
                  .collect(Collectors.toList());

          break;
        case PTR:
          answer =
              Arrays.stream(records)
                  .map(PTRRecord.class::cast)
                  .map(p -> p.getTarget().toString())
                  .collect(Collectors.toList());

          break;

        default:
          throw new IllegalArgumentException("Unknown Query type: " + queryType);
      }
    }

    System.out.printf("[%s] <- %s%n", Type.string(queryType), answer);
    return answer;
  }

  /**
   * Query DNS record using the authoritative name servers for a given domain/zone.
   *
   * <p>First find the authoritative name servers for a given domain using system resolvers and use
   * that authoritative name servers for subsequent DNS queries. When the name server is not
   * specified, it will use the operating system's default resolver, usually configured via the
   * <b>resolv.conf</b> file
   *
   * @param fqdn name of the resource record that is to be looked up.
   * @param queryType {@link Type} indicates what type of query is required, CNAME, A, MX, etc.
   * @param domain domain/zone name.
   * @return DNS lookup result.
   * @throws IOException if a problem occurred talking to DNS resolver.
   */
  public static List<String> lookup(String fqdn, int queryType, String domain) throws IOException {
    List<String> authoritativeNS = lookup0(domain, NS, null);
    assertTrue(authoritativeNS.size() > 0, "Can't find any Authoritative NS for " + domain);

    for (String ns : authoritativeNS) {
      List<String> res = lookup0(fqdn, queryType, ns);
      if (!res.isEmpty()) return res;
    }
    return Collections.emptyList();
  }
}
