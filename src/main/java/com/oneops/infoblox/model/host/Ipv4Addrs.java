package com.oneops.infoblox.model.host;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.ref.Ref;
import com.oneops.infoblox.model.ref.RefObject;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Host record response IPv4 object.
 *
 * @author Suresh G
 */
@AutoValue
public abstract class Ipv4Addrs {

  @Json(name = "_ref")
  public abstract @RefObject Ref ref();

  public abstract String host();

  @Json(name = "ipv4addr")
  public abstract String ipv4Addr();

  @Json(name = "configure_for_dhcp")
  public abstract boolean configureForDhcp();

  public static Builder builder() {
    return new AutoValue_Ipv4Addrs.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder ref(Ref ref);

    public abstract Builder host(String host);

    public abstract Builder ipv4Addr(String ipv4Addr);

    public abstract Builder configureForDhcp(boolean configureForDhcp);

    public abstract Ipv4Addrs build();
  }

  public static JsonAdapter<Ipv4Addrs> jsonAdapter(Moshi moshi) {
    return new AutoValue_Ipv4Addrs.MoshiJsonAdapter(moshi);
  }
}
