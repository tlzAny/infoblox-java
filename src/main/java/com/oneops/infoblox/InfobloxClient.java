package com.oneops.infoblox;

import static com.oneops.infoblox.util.IPAddrs.requireIPv4;
import static com.oneops.infoblox.util.IPAddrs.requireIPv6;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.curl.CurlLoggingInterceptor;
import com.oneops.infoblox.model.Error;
import com.oneops.infoblox.model.JsonAdapterFactory;
import com.oneops.infoblox.model.Record;
import com.oneops.infoblox.model.Redacted;
import com.oneops.infoblox.model.SearchModifier;
import com.oneops.infoblox.model.a.ARec;
import com.oneops.infoblox.model.aaaa.AAAA;
import com.oneops.infoblox.model.cname.CNAME;
import com.oneops.infoblox.model.host.Host;
import com.oneops.infoblox.model.mx.MX;
import com.oneops.infoblox.model.network.Network;
import com.oneops.infoblox.model.ptr.PTR;
import com.oneops.infoblox.model.ref.RefObject;
import com.oneops.infoblox.model.ttl.TTLRec;
import com.oneops.infoblox.model.zone.Delegate;
import com.oneops.infoblox.model.zone.ZoneAuth;
import com.oneops.infoblox.model.zone.ZoneDelegate;
import com.oneops.infoblox.tls.SNIDisabledSocketFactory;
import com.oneops.infoblox.tls.TrustAllCertsManager;
import com.oneops.infoblox.util.IPAddrs;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * A Client for interacting with Infoblox Appliance (IBA) NIOS over WAPI. This client implements the
 * subset of Infoblox API.
 *
 * @author Suresh G
 */
@AutoValue
public abstract class InfobloxClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /** IBA IP address of management interface */
  public abstract String endPoint();

  /**
   * IBA WAPI version. Browse to <a href="https://infoblox-server/wapidoc/">WapiDoc</a> to see the
   * current wapi version of Infoblox appliance. Defaults to 2.7
   */
  public abstract String wapiVersion();

  /** IBA user name */
  @Redacted
  public abstract String userName();

  /** IBA user password */
  @Redacted
  public abstract String password();

  /** IBA default view. Defaults to 'default`. */
  public abstract String dnsView();

  /**
   * A 32-bit integer (range from 0 to 4294967295) that represents the duration in seconds that the
   * record is cached. Zero indicates that the record should not be cached. The default value is
   * undefined which indicates that the record inherits the TTL value of the zone. Specify a TTL
   * value to override the TTL value at the zone level.
   *
   * <p>TTL — (Time-To-Live) How often a copy of the record stored in cache (local storage) must be
   * updated (fetched from original storage) or discarded. Shorter TTLs mean records are fetched
   * more often (access is slower, data is more current). Longer TTLs mean records are fetched from
   * less often (access is faster, data is less current). The default value is 5 sec. Note: When you
   * make changes to a resource record, It could take up to the length of the TTL time for the
   * change to propagate.
   */
  public abstract int ttl();

  /** Checks if TLS certificate validation is enabled for communicating with Infoblox. */
  public abstract boolean tlsVerify();

  /**
   * PKCS#12 (.p12) file path contains trusted CA certs. If the path starts with <b>classpath:</b>,
   * it will be loaded from classpath. This is optional and required only if {@link #tlsVerify()} is
   * enabled.
   */
  public abstract Optional<String> trustStore();

  /**
   * TrustStore password. Default password is 'changeit'. This is optional and required only if
   * {@link #tlsVerify()} is enabled.
   */
  @Redacted
  public abstract Optional<String> trustStorePassword();

  /** IBA WAPI connection/read/write timeout. Default is 10 sec */
  public abstract int timeout();

  /** Enable http curl logging for debugging. */
  public abstract boolean debug();

  private Infoblox infoblox;

  private Converter<ResponseBody, Error> errResConverter;

  /**
   * Initializes the TLS retrofit client. Server Name Indication (SNI) TLS extension is disabled by
   * default as it never worked with Infoblox.
   *
   * @throws GeneralSecurityException if any error initializing the TLS context.
   */
  private void init() throws GeneralSecurityException {
    log.info("Initializing " + toString());
    Moshi moshi =
        new Moshi.Builder()
            .add(JsonAdapterFactory.create())
            .add(new RefObject.JsonAdapter())
            .build();

    TrustManager[] trustManagers = getTrustManagers();
    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(null, trustManagers, new SecureRandom());

    // Disable SNIExtension.
    SSLSocketFactory socketFactory = new SNIDisabledSocketFactory(sslContext.getSocketFactory());

    String basicCreds = Credentials.basic(userName(), password());
    OkHttpClient.Builder okBuilder =
        new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
            .connectionSpecs(singletonList(ConnectionSpec.MODERN_TLS))
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .connectTimeout(timeout(), SECONDS)
            .readTimeout(timeout(), SECONDS)
            .writeTimeout(timeout(), SECONDS)
            .addInterceptor(
                chain -> {
                  HttpUrl origUrl = chain.request().url();
                  HttpUrl url =
                      origUrl
                          .newBuilder()
                          .addQueryParameter("_return_as_object", "1")
                          // .addQueryParameter("_max_results","1")
                          .build();
                  Request req =
                      chain
                          .request()
                          .newBuilder()
                          .addHeader("Content-Type", "application/json")
                          .addHeader("Authorization", basicCreds)
                          .url(url)
                          .build();
                  return chain.proceed(req);
                });

    if (!tlsVerify()) {
      okBuilder.hostnameVerifier((host, session) -> true);
    }

    if (debug()) {
      CurlLoggingInterceptor logIntcp = new CurlLoggingInterceptor(log::info);
      logIntcp.curlOptions("-k");
      okBuilder.addNetworkInterceptor(logIntcp);
    }
    OkHttpClient okHttp = okBuilder.build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build();

    infoblox = retrofit.create(Infoblox.class);
    errResConverter = retrofit.responseBodyConverter(Error.class, new Annotation[0]);
  }

  /**
   * Returns the trust-store manager.If the {@link #tlsVerify()} is disabled, it trusts all certs
   * using a custom trust manager.
   *
   * @return trust managers.
   * @throws GeneralSecurityException if any error initializing trust store.
   */
  private TrustManager[] getTrustManagers() throws GeneralSecurityException {
    final TrustManager[] trustMgrs;
    if (tlsVerify()) {
      KeyStore trustStore = loadTrustStore();
      final TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustMgrs = trustManagerFactory.getTrustManagers();
    } else {
      log.info("Skipping TLS certs verification.");
      trustMgrs = new X509TrustManager[] {new TrustAllCertsManager()};
    }
    return trustMgrs;
  }

  /**
   * Load trust store (PKCS12) from the given file/classpath resource.
   *
   * @throws IllegalStateException if the file/classpath resource doesn't exist.
   */
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private KeyStore loadTrustStore() {

    String tsPath = trustStore().get().toLowerCase();
    char[] tsPasswd = trustStorePassword().get().toCharArray();
    boolean fileResource = true;

    if (tsPath.startsWith("classpath:")) {
      tsPath = tsPath.replace("classpath:", "");
      fileResource = false;
    }

    try {
      try (InputStream ins =
          fileResource
              ? Files.newInputStream(Paths.get(tsPath))
              : getClass().getResourceAsStream(tsPath)) {

        log.info("Loading the trustStore: {}", tsPath);
        if (ins == null) {
          throw new IllegalStateException("Can't find the trustStore: " + tsPath);
        }
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(ins, tsPasswd);
        return ks;
      }
    } catch (IOException | GeneralSecurityException ex) {
      throw new IllegalStateException("Can't load the trustStore: " + tsPath, ex);
    }
  }

  /**
   * Helper method to handle {@link Call} object and return the execution result(s). The error
   * handling is done as per the response content-type.
   *
   * @see <a href="https://ipam.illinois.edu/wapidoc/#error-handling">WAPI error-handling</a>
   */
  private <T> T exec(Call<T> call) throws IOException {
    Response<T> res = call.execute();
    if (res.isSuccessful()) {
      return res.body();
    } else {
      Error err;
      String contentType = res.headers().get("Content-Type");
      if ("application/json".equalsIgnoreCase(contentType)) {
        err = errResConverter.convert(requireNonNull(res.errorBody()));
      } else {
        err = Error.create("Request failed, " + res.message(), res.code());
      }
      throw err.cause();
    }
  }

  /**
   * Returns infoblox WAPI base url for given version.
   *
   * @return WAPI base url.
   */
  private String getBaseUrl() {
    StringBuilder buf = new StringBuilder();
    if (!endPoint().toLowerCase().startsWith("http")) {
      buf.append("https://");
    }
    return buf.append(endPoint()).append("/wapi/v").append(wapiVersion()).append("/").toString();
  }

  /**
   * Create a new request map with ttl set. TTL is associated with the flag <b>use_ttl</b>. In an
   * object, the value of this field will only take effect when its use flag is true.
   *
   * @return request map
   */
  private Map<String, Object> newTTLReq() {
    Map<String, Object> req = new HashMap<>();
    req.put("ttl", ttl());
    req.put("use_ttl", true);
    return req;
  }

  // --------<Auth Zone Record>--------
  /**
   * Fetch all Authoritative Zones.
   *
   * @return list of {@link ZoneAuth}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneAuth> getAuthZones() throws IOException {
    return exec(infoblox.queryAuthZones()).result();
  }

  /**
   * Fetch all authoritative zones for the given domain name and search option.
   *
   * @param domainName fqdn.
   * @param modifier {@link SearchModifier}
   * @return list of {@link ZoneAuth}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneAuth> getAuthZones(String domainName, SearchModifier modifier)
      throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("fqdn" + modifier.getValue(), domainName);
    return exec(infoblox.queryAuthZone(options)).result();
  }

  /**
   * Search all authoritative zones for the given domain name.
   *
   * @param domainName fqdn.
   * @return list of {@link ZoneAuth}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneAuth> getAuthZones(String domainName) throws IOException {
    return getAuthZones(domainName, SearchModifier.NONE);
  }

  // --------<Delegated Zone Record>--------
  /**
   * Fetch all delegated zones.
   *
   * @return list of {@link ZoneDelegate}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneDelegate> getDelegatedZones() throws IOException {
    return exec(infoblox.queryDelegatedZones()).result();
  }

  /**
   * Fetch all delegated zones for the given domain name and search option.
   *
   * @param domainName fqdn.
   * @param modifier {@link SearchModifier}
   * @return list of {@link ZoneDelegate}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneDelegate> getDelegatedZones(String domainName, SearchModifier modifier)
      throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("fqdn" + modifier.getValue(), domainName);
    return exec(infoblox.queryDelegatedZone(options)).result();
  }

  /**
   * Search all delegated zones for the given domain name.
   *
   * @param domainName fqdn.
   * @return list of {@link ZoneDelegate}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneDelegate> getDelegatedZones(String domainName) throws IOException {
    return getDelegatedZones(domainName, SearchModifier.NONE);
  }

  /**
   * Creates delegated zone for a domain.
   *
   * @param domainName fqdn
   * @param delegateTo list of {@link Delegate} for this fqdn
   * @param ttl delegated ttl
   * @return {@link ZoneDelegate} ZoneDelegate record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public ZoneDelegate createDelegatedZone(String domainName, List<Delegate> delegateTo, int ttl)
      throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireNonNull(delegateTo, "DelegateTo is null");
    Map<String, Object> req = new HashMap<>(3);
    req.put("fqdn", domainName);
    req.put("delegate_to", delegateTo);
    req.put("delegated_ttl", ttl);
    return exec(infoblox.createDelegatedZone(req)).result();
  }

  /**
   * Modify delegated zone for the given domain
   *
   * @param domainName fqdn.
   * @param params delegated zone param to change.
   * @return changed delegated zone records.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ZoneDelegate> modifyDelegatedZone(String domainName, Map<String, Object> params)
      throws IOException {
    return getDelegatedZones(domainName)
        .stream()
        .map(
            rec -> {
              try {
                log.warn("Modifying delegated zone config for " + rec + " to" + params);
                return exec(infoblox.modifyDelegatedZone(rec.ref().value(), params)).result();
              } catch (IOException ioe) {
                throw new IllegalStateException(
                    "Error modifying delegated zone record: " + rec, ioe);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Delete delegated zones for the given domain name.
   *
   * @param domainName domain name.
   * @return list of delegated zone obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteDelegatedZone(String domainName) throws IOException {
    return deleteRecords(getDelegatedZones(domainName));
  }

  // --------<Host Record>--------
  /**
   * Get host information for the given domain name and search option.
   *
   * @param domainName fqdn
   * @return list of matching {@link Host}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<Host> getHostRec(String domainName, SearchModifier modifier) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name" + modifier.getValue(), domainName);
    return exec(infoblox.queryHostRec(options)).result();
  }

  /**
   * Get host information for the given domain name.
   *
   * @param domainName fqdn
   * @return list of matching {@link Host}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<Host> getHostRec(String domainName) throws IOException {
    return getHostRec(domainName, SearchModifier.NONE);
  }

  /**
   * Create host record with next free IP address in network (IP-range).
   *
   * @param hostName full qualified domain name to be created
   * @param mac valid mac-address (lowercase)
   * @param targetSubnet string representation with / notation (192.168.1.0/24)
   */
  public Host createHostRecNextAvailIP(String hostName, String mac, Network targetSubnet)
      throws IOException {

    requireNonNull(mac, "mac name is null");
    requireNonNull(hostName, "hostName is null");

    // Sample data to achieve
    /*
        "ipv4addrs": [
        {
            "ipv4addr": {
                "_object_function": "next_available_ip",
                "_object": "network",
                "_object_parameters": {"network": "192.168.1.0/24"},
                "_result_field": "ips",
            },
            "mac": "aa:bb:cc:11:22:21"
        }
    ]
    */

    Map<String, Object> entry = new HashMap<>(1);
    entry.put("_object_function", "next_available_ip");
    entry.put("_object", "network");
    Map<String, String> networkMap = new HashMap<>(1);

    // networkMap.put("network", "192.168.1.0");
    networkMap.put("network", targetSubnet.network());
    entry.put("_object_parameters", networkMap);
    entry.put("_result_field", "ips");

    Map<String, Object> ipMap = new HashMap<>(1);
    ipMap.put("ipv4addr", entry);
    ipMap.put("mac", mac);

    List<Object> listEntries = new ArrayList(1);
    listEntries.add(ipMap);

    Map<String, Object> req = newTTLReq();
    req.put("name", hostName);
    req.put("ipv4addrs", listEntries);
    return exec(infoblox.createHostRec(req)).result();
  }

  /**
   * Creates IBA host record.
   *
   * @param domainName hostname in fqdn.
   * @param ipv4Addrs IPv4 address(s)
   * @return {@link Host} containing IPv4 addresses for the hostname.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public Host createHostRec(String domainName, List<String> ipv4Addrs) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireNonNull(ipv4Addrs, "IPv4Address list is null");
    // Transform the address.
    List<Map<String, String>> addrs =
        ipv4Addrs
            .stream()
            .map(
                s -> {
                  Map<String, String> map = new HashMap<>(1);
                  map.put("ipv4addr", s);
                  return map;
                })
            .collect(Collectors.toList());

    Map<String, Object> req = newTTLReq();
    req.put("name", domainName);
    req.put("ipv4addrs", addrs);
    return exec(infoblox.createHostRec(req)).result();
  }

  /**
   * Deletes IBA host record with given domain name.
   *
   * @param domainName fqdn for the host record.
   * @return list of deleted host references.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteHostRec(String domainName) throws IOException {
    return deleteRecords(getHostRec(domainName));
  }

  // --------<A Record>--------
  /**
   * Get address records (A Record) for the given domain name and search option.
   *
   * @param domainName fqdn
   * @return list of matching {@link ARec}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ARec> getARec(String domainName, SearchModifier modifier) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name" + modifier.getValue(), domainName);
    return exec(infoblox.queryARec(options)).result();
  }

  /**
   * Get address records (A Record) for the given domain name.
   *
   * @param domainName fqdn
   * @return list of matching {@link ARec}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ARec> getARec(String domainName) throws IOException {
    return getARec(domainName, SearchModifier.NONE);
  }

  /**
   * Get address record (A Record) for the given domain name and IPv4 address.
   *
   * @param domainName fqdn
   * @param ipv4Address IPv4 address
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ARec> getARec(String domainName, String ipv4Address) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireIPv4(ipv4Address);
    Map<String, String> options = new HashMap<>(2);
    options.put("name", domainName);
    options.put("ipv4addr", ipv4Address);
    return exec(infoblox.queryARec(options)).result();
  }

  /**
   * Creates an address record (A Record)
   *
   * @param domainName FQDN
   * @param ipv4Address IPv4 address
   * @return {@link ARec} address record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public ARec createARec(String domainName, String ipv4Address) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireIPv4(ipv4Address);
    Map<String, Object> req = newTTLReq();
    req.put("name", domainName);
    req.put("ipv4addr", ipv4Address);
    return exec(infoblox.createARec(req)).result();
  }

  /**
   * Deletes address record with given domain name.
   *
   * @param domainName fqdn for the A record.
   * @return list of A record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteARec(String domainName) throws IOException {
    return deleteRecords(getARec(domainName));
  }

  /**
   * Deletes an address record (A Record) with given domain name and IPv4 address.
   *
   * @param domainName fqdn for the A record.
   * @param ipv4Address IPv4 address
   * @return list of A record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteARec(String domainName, String ipv4Address) throws IOException {
    return deleteRecords(getARec(domainName, ipv4Address));
  }

  /**
   * Modify the domain name of A record with given name.
   *
   * @param domainName fqdn for the A record.
   * @param newDomainName new fqdn.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<ARec> modifyARec(String domainName, String newDomainName) throws IOException {
    return getARec(domainName)
        .stream()
        .map(
            rec -> {
              Map<String, String> req = new HashMap<>(1);
              req.put("name", newDomainName);
              try {
                return exec(infoblox.modifyARec(rec.ref().value(), req)).result();
              } catch (IOException ioe) {
                throw new IllegalStateException("Error modifying A record: " + rec, ioe);
              }
            })
        .collect(Collectors.toList());
  }

  // --------<query network>--------
  public List<Network> getNetworks(String networkName, SearchModifier modifier) throws IOException {
    requireNonNull(networkName, "network name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("network" + modifier.getValue(), networkName);
    return exec(infoblox.queryNetwork(options)).result();
  }
  // _max_results=50&_paging=1

  /**
   * Default max result is 5000
   *
   * @return
   * @throws IOException
   */
  public List<Network> getAllNetworks() throws IOException {
    return exec(infoblox.queryNetwork()).result();
  }

  /**
   * Retrieve networks by max number of records
   *
   * @param maxResults
   * @return
   * @throws IOException
   */
  public List<Network> getAllNetworks(int maxResults) throws IOException {
    return exec(infoblox.queryNetwork(maxResults)).result();
  }

  /**
   * Idea is to define a boolean custom attribute on each network to query for i.e.
   * valid4autoServerDeployment = true
   * https://10.4.96.18/wapi/v2.7/network?_return_fields%2B=extattrs&*Zone%3A~=+*&_max_results=5000
   *
   * @param attributeName name of the custom attribute on 'network' object in IPAM
   * @return List of network objects
   * @throws IOException
   */
  public List<Network> getAllNetworkbyCustomAttribute(String attributeName) throws IOException {
    String CUSTOM_ATTR_PREFIX = "*";
    String REGEX_QUERY = "~";
    String ANY_VALUE = "+*";

    requireNonNull(attributeName, "customAttribute name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put(CUSTOM_ATTR_PREFIX + attributeName + REGEX_QUERY, ANY_VALUE);
    return exec(infoblox.queryNetworkCustomAttribute(options)).result();
  }

  // --------<AAAA Record>--------
  /**
   * Get IPv6 address records (AAAA) for the given domain name and search option.
   *
   * @param domainName fqdn
   * @return list of matching {@link AAAA}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<AAAA> getAAAARec(String domainName, SearchModifier modifier) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name" + modifier.getValue(), domainName);
    return exec(infoblox.queryAAAARec(options)).result();
  }

  /**
   * Get IPv6 address records (AAAA) for the given domain name.
   *
   * @param domainName fqdn
   * @return list of matching {@link AAAA}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<AAAA> getAAAARec(String domainName) throws IOException {
    return getAAAARec(domainName, SearchModifier.NONE);
  }

  /**
   * Get AAAA records for the given domain name and ipv6Address.
   *
   * @param domainName fqdn
   * @param ipv6Address IPv6 address
   * @return list of matching {@link AAAA}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<AAAA> getAAAARec(String domainName, String ipv6Address) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireIPv6(ipv6Address);
    Map<String, String> options = new HashMap<>(1);
    options.put("name", domainName);
    options.put("ipv6addr", ipv6Address);
    return exec(infoblox.queryAAAARec(options)).result();
  }

  /**
   * Creates an IPv6 address record (AAAA Record)
   *
   * @param domainName fqdn
   * @param ipv6Address IPv6 address
   * @return {@link AAAA} address record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public AAAA createAAAARec(String domainName, String ipv6Address) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireIPv6(ipv6Address);
    Map<String, Object> req = newTTLReq();
    req.put("name", domainName);
    req.put("ipv6addr", ipv6Address);
    return exec(infoblox.createAAAARec(req)).result();
  }

  /**
   * Deletes IPv6 address record with given domain name.
   *
   * @param domainName fqdn for the AAAA record.
   * @return list of A record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteAAAARec(String domainName) throws IOException {
    return deleteRecords(getAAAARec(domainName));
  }

  /**
   * Deletes AAAA record with given domain name and ipv6Address.
   *
   * @param domainName fqdn for the AAAA record.
   * @param ipv6Address IPv6Address
   * @return list of A record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteAAAARec(String domainName, String ipv6Address) throws IOException {
    return deleteRecords(getAAAARec(domainName, ipv6Address));
  }

  /**
   * Modify the domain name of AAAA record with given name.
   *
   * @param domainName fqdn for the AAAA record.
   * @param newDomainName new fqdn.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<AAAA> modifyAAAARec(String domainName, String newDomainName) throws IOException {
    return getAAAARec(domainName)
        .stream()
        .map(
            rec -> {
              Map<String, String> req = new HashMap<>(1);
              req.put("name", newDomainName);
              try {
                return exec(infoblox.modifyAAAARec(rec.ref().value(), req)).result();
              } catch (IOException ioe) {
                throw new IllegalStateException("Error modifying AAAA record: " + rec, ioe);
              }
            })
        .collect(Collectors.toList());
  }

  // --------<CNAME Record>--------
  /**
   * Get canonical records (CNAME) for the given alias name and search option.
   *
   * @param aliasName fqdn
   * @return list of matching {@link CNAME}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<CNAME> getCNameRec(String aliasName, SearchModifier modifier) throws IOException {
    requireNonNull(aliasName, "Alias name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name" + modifier.getValue(), aliasName);
    return exec(infoblox.queryCNAMERec(options)).result();
  }

  /**
   * Get canonical records (CNAME) for the given alias name.
   *
   * @param aliasName fqdn
   * @return list of matching {@link CNAME}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<CNAME> getCNameRec(String aliasName) throws IOException {
    return getCNameRec(aliasName, SearchModifier.NONE);
  }

  /**
   * Get canonical records (CNAME) for the given alias name and canonical name.
   *
   * @param aliasName fqdn
   * @param canonicalName Canonical (true/actual) domain name.
   * @return list of matching {@link CNAME}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<CNAME> getCNameRec(String aliasName, String canonicalName) throws IOException {
    requireNonNull(aliasName, "Alias name is null");
    requireNonNull(canonicalName, "Canonical name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name", aliasName);
    options.put("canonical", canonicalName);
    return exec(infoblox.queryCNAMERec(options)).result();
  }

  /**
   * Creates a canonical record (CNAME Record). CNAME records must always point to another domain
   * name, never directly to an IP address.
   *
   * @param aliasName alias domain name
   * @param canonicalName Canonical (true/actual) domain name.
   * @return {@link CNAME} record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public CNAME createCNameRec(String aliasName, String canonicalName) throws IOException {
    requireNonNull(aliasName, "Alias name is null");
    requireNonNull(canonicalName, "Canonical name is null");
    Map<String, Object> req = newTTLReq();
    req.put("name", aliasName);
    req.put("canonical", canonicalName);
    return exec(infoblox.createCNAMERec(req)).result();
  }

  /**
   * Deletes canonical record with given alias name and it's associated canonicalName.
   *
   * @param aliasName alias name to be deleted.
   * @param canonicalName Canonical (true/actual) domain name.
   * @return list of CNAME record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteCNameRec(String aliasName, String canonicalName) throws IOException {
    return deleteRecords(getCNameRec(aliasName, canonicalName));
  }

  /**
   * Deletes canonical record with given alias name.
   *
   * @param aliasName alias name to be deleted.
   * @return list of CNAME record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteCNameRec(String aliasName) throws IOException {
    return deleteRecords(getCNameRec(aliasName));
  }

  /**
   * Modify alias name of the CNAME record with new name.
   *
   * @param aliasName alias name.
   * @param newAliasName new fqdn.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<CNAME> modifyCNameRec(String aliasName, String newAliasName) throws IOException {
    return getCNameRec(aliasName)
        .stream()
        .map(
            rec -> {
              Map<String, String> req = new HashMap<>(1);
              req.put("name", newAliasName);
              try {
                return exec(infoblox.modifyCNAMERec(rec.ref().value(), req)).result();
              } catch (IOException ioe) {
                throw new IllegalStateException("Error modifying CNAME record: " + rec, ioe);
              }
            })
        .collect(Collectors.toList());
  }

  // --------<MX Record>--------
  /**
   * Get mail exchange (MX) record for the given domain name and search option.
   *
   * @param domainName fqdn
   * @return list of matching {@link MX}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<MX> getMXRec(String domainName, SearchModifier modifier) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name" + modifier.getValue(), domainName);
    return exec(infoblox.queryMXRec(options)).result();
  }

  /**
   * Get mail exchange (MX) record for the given domain name.
   *
   * @param domainName fqdn
   * @return list of matching {@link MX}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<MX> getMXRec(String domainName) throws IOException {
    return getMXRec(domainName, SearchModifier.NONE);
  }

  /**
   * Get mail exchange (MX) records for the given domain name and mail exchanger.
   *
   * @param domainName fqdn
   * @param mailExchanger Mail server host responsible for accepting email messages on behalf of the
   *     domain name. The host name must map directly to one or more address record (A, or AAAA) in
   *     the DNS, and must not point to any CNAME records.
   * @return list of matching {@link MX}
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<MX> getMXRec(String domainName, String mailExchanger) throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireNonNull(mailExchanger, "MailExchanger is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("name", domainName);
    options.put("mail_exchanger", mailExchanger);
    return exec(infoblox.queryMXRec(options)).result();
  }

  /**
   * Creates mail exchange (MX) record for a domain name.
   *
   * @param domainName domain name.
   * @param mailExchanger Mail server host responsible for accepting email messages on behalf of the
   *     domain name. The host name must map directly to one or more address record (A, or AAAA) in
   *     the DNS, and must not point to any CNAME records.
   * @param preference A value used to prioritize mail delivery if multiple mail servers are
   *     available, smaller distances are more preferable.
   * @return {@link MX} record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public MX createMXRec(String domainName, String mailExchanger, int preference)
      throws IOException {
    requireNonNull(domainName, "Domain name is null");
    requireNonNull(mailExchanger, "MailExchanger is null");

    Map<String, Object> req = newTTLReq();
    req.put("name", domainName);
    req.put("mail_exchanger", mailExchanger);
    req.put("preference", preference);
    return exec(infoblox.createMXRec(req)).result();
  }

  /**
   * Deletes MX record for a domain name.
   *
   * @param domainName fqdn.
   * @return list of MX record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteMXRec(String domainName) throws IOException {
    return deleteRecords(getMXRec(domainName));
  }

  /**
   * Deletes MX record for a domain name and mail exchanger.
   *
   * @param domainName fqdn.
   * @param mailExchanger Mail server host responsible for accepting email messages on behalf of the
   *     domain name.
   * @return list of MX record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deleteMXRec(String domainName, String mailExchanger) throws IOException {
    return deleteRecords(getMXRec(domainName, mailExchanger));
  }

  /**
   * Modify the MX record domain name.
   *
   * @param domainName mx domain name.
   * @param newDomainName new domain name.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<MX> modifyMXRec(String domainName, String newDomainName) throws IOException {
    return getMXRec(domainName)
        .stream()
        .map(
            rec -> {
              Map<String, String> req = new HashMap<>(1);
              req.put("name", newDomainName);
              try {
                return exec(infoblox.modifyMXRec(rec.ref().value(), req)).result();
              } catch (IOException ioe) {
                throw new IllegalStateException("Error modifying MX record: " + rec, ioe);
              }
            })
        .collect(Collectors.toList());
  }

  // --------<PTR Record>--------
  /**
   * Get pointer records (PTR) for the given IP Address.
   *
   * @param ipAddress IPv4/v6 address
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<PTR> getPTRRec(String ipAddress) throws IOException {
    requireNonNull(ipAddress, "IPAddress is null");
    String addrType = IPAddrs.isIPv4(ipAddress) ? "ipv4addr" : "ipv6addr";
    Map<String, String> options = new HashMap<>(1);
    options.put(addrType, ipAddress);
    return exec(infoblox.queryPTRRec(options)).result();
  }

  /**
   * Get pointer (PTR) records for the given domain name.
   *
   * @param ptrdname pointer domain name.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<PTR> getPTRDRec(String ptrdname) throws IOException {
    requireNonNull(ptrdname, "Pointer domain name is null");
    Map<String, String> options = new HashMap<>(1);
    options.put("ptrdname", ptrdname);
    return exec(infoblox.queryPTRRec(options)).result();
  }

  /**
   * Creates pointer (PTR) record for the IP address and domain name.
   *
   * @param ptrdname pointer domain name
   * @param ipAddress IPv4/v6 address
   * @return {@link PTR} record.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public PTR createPTRRec(String ptrdname, String ipAddress) throws IOException {
    requireNonNull(ptrdname, "Pointer domain name is null");
    requireNonNull(ipAddress, "IPAddress is null");
    String addrType = IPAddrs.isIPv4(ipAddress) ? "ipv4addr" : "ipv6addr";

    Map<String, Object> req = newTTLReq();
    req.put("name", PTR.reverseMapName(InetAddress.getByName(ipAddress)));
    req.put("ptrdname", ptrdname);
    req.put(addrType, ipAddress);
    return exec(infoblox.createPTRRec(req)).result();
  }

  /**
   * Deletes PTR records for the given IP address.
   *
   * @param ipAddress IPv4/v6 address
   * @return list of PTR record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deletePTRRec(String ipAddress) throws IOException {
    return deleteRecords(getPTRRec(ipAddress));
  }

  /**
   * Deletes PTR records for the given domain name.
   *
   * @param ptrdname pointer domain name
   * @return list of PTR record obj references deleted.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public List<String> deletePTRDRec(String ptrdname) throws IOException {
    return deleteRecords(getPTRDRec(ptrdname));
  }

  // --------<TXT Record>--------
  // --------<SRV Record>--------
  // --------<NS Record>--------
  // --------<TTL>--------
  /**
   * Modify TTL for a record.
   *
   * @param record {@link Record}
   * @param newTTL new TTL value in seconds.
   * @return TTL response.
   * @throws IOException if a problem occurred talking to the infoblox.
   */
  public TTLRec modifyTTL(Record record, int newTTL) throws IOException {
    requireNonNull(record, "Record is null.");
    log.warn("Changing TTL of record " + record + " to '" + newTTL + "' seconds.");
    Map<String, Object> req = newTTLReq();
    req.put("ttl", newTTL);
    return exec(infoblox.modifyTTL(record.ref().value(), req)).result();
  }

  /**
   * A helper method to delete list of DNS records.
   *
   * @param recs list of DNS records to be deleted.
   * @return list of deleted record ref ids.
   */
  private <T extends Record> List<String> deleteRecords(List<T> recs) {
    return recs.stream().map(this::deleteRecord).collect(Collectors.toList());
  }

  /**
   * A helper method to delete a DNS record.
   *
   * @param rec {@link Record}
   * @return ref id of the deleted dns record.
   */
  private <T extends Record> String deleteRecord(T rec) {
    try {
      log.warn("Deleting a dns record: " + rec);
      return exec(infoblox.deleteRef(rec.ref().value())).result();
    } catch (IOException ioe) {
      throw new IllegalStateException("Error deleting record: " + rec, ioe);
    }
  }

  /**
   * Returns the builder for {@link InfobloxClient} with default values for un-initialized optional
   * fields.
   *
   * @return Builder
   */
  public static Builder builder() {
    return new AutoValue_InfobloxClient.Builder()
        .wapiVersion("2.5")
        .dnsView("default")
        .ttl(5)
        .tlsVerify(true)
        .timeout(10)
        .debug(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder endPoint(String endPoint);

    public abstract Builder wapiVersion(String wapiVersion);

    public abstract Builder userName(String userName);

    public abstract Builder password(String password);

    public abstract Builder dnsView(String dnsView);

    public abstract Builder ttl(int ttl);

    public abstract Builder tlsVerify(boolean tlsVerify);

    public abstract Builder trustStore(String trustStore);

    public abstract Builder trustStorePassword(String trustStorePassword);

    public abstract Builder timeout(int timeout);

    public abstract Builder debug(boolean debug);

    abstract boolean tlsVerify();

    abstract Optional<String> trustStore();

    abstract Optional<String> trustStorePassword();

    abstract InfobloxClient autoBuild();

    /**
     * Build and initialize Infoblox client.
     *
     * @return client.
     */
    public InfobloxClient build() {
      // Trust-store properties validation if TLS is enabled.
      if (tlsVerify()) {
        trustStore().orElseThrow(() -> new IllegalStateException("Truststore path is empty."));
        trustStorePassword()
            .orElseThrow(() -> new IllegalStateException("Truststore password is empty."));
      }

      InfobloxClient client = autoBuild();
      try {
        client.init();
      } catch (GeneralSecurityException ex) {
        throw new IllegalArgumentException("Infoblox client init failed.", ex);
      }
      return client;
    }
  }
}
