package com.oneops.infoblox.model.aaaa;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * IPv6 Address record {@link com.oneops.infoblox.model.DNSRecord#AAAA } response.
 *
 * @author Suresh G
 */
@AutoValue
public abstract class AAAA extends Record {

  @Json(name = "ipv6addr")
  public abstract String ipv6Addr();

  public abstract String name();

  public static Builder builder() {
    return new AutoValue_AAAA.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder ipv6Addr(String ipv6Addr);

    public abstract Builder name(String name);

    public abstract AAAA build();
  }

  public static JsonAdapter<AAAA> jsonAdapter(Moshi moshi) {
    return new AutoValue_AAAA.MoshiJsonAdapter(moshi);
  }
}
