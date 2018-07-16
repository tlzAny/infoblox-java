package com.oneops.infoblox.model.host;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;
import javax.annotation.Nullable;

/**
 * DNS Host record.
 *
 * <p>Note: There is no such thing as a Host record in the actual DNS specification. Host records
 * are generally a logical construct in DDI (DNS, DHCP, and IPAM) solutions like Infoblox and
 * others. They comprise various DNS record types (A, AAAA, PTR, CNAME, etc) and other metadata
 * associated with a "host".
 *
 * @author Suresh G
 * @see <a href="https://serverfault.com/a/700350">Host record and A record</a>
 */
@AutoValue
public abstract class Host extends Record {

  public abstract String name();

  /** <b>Do not mutate</b> the returned object. */
  @Json(name = "ipv4addrs")
  public abstract List<Ipv4Addrs> ipv4Addrs();

  /** <b>Do not mutate</b> the returned object. */
  @Nullable
  public abstract List<String> aliases();

  public static Builder builder() {
    return new AutoValue_Host.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder name(String name);

    public abstract Builder ipv4Addrs(List<Ipv4Addrs> ipv4Addrs);

    public abstract Builder aliases(List<String> aliases);

    public abstract Host build();
  }

  public static JsonAdapter<Host> jsonAdapter(Moshi moshi) {
    return new AutoValue_Host.MoshiJsonAdapter(moshi);
  }
}
