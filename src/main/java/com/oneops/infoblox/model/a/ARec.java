package com.oneops.infoblox.model.a;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Address record {@link com.oneops.infoblox.model.DNSRecord#A } response.
 *
 * @author Suresh G
 */
@AutoValue
public abstract class ARec extends Record {

  @Json(name = "ipv4addr")
  public abstract String ipv4Addr();

  public abstract String name();

  public static Builder builder() {
    return new AutoValue_ARec.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder ipv4Addr(String ipv4Addr);

    public abstract Builder name(String name);

    public abstract ARec build();
  }

  public static JsonAdapter<ARec> jsonAdapter(Moshi moshi) {
    return new AutoValue_ARec.MoshiJsonAdapter(moshi);
  }
}
