package com.oneops.infoblox.model.srv;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Service location record {@link com.oneops.infoblox.model.DNSRecord#SRV} response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class SRV extends Record {

  public abstract String name();

  public abstract int port();

  public abstract int priority();

  public abstract int target();

  public abstract int weight();

  public static Builder builder() {
    return new AutoValue_SRV.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder name(String name);

    public abstract Builder port(int port);

    public abstract Builder priority(int priority);

    public abstract Builder target(int target);

    public abstract Builder weight(int weight);

    public abstract SRV build();
  }

  public static JsonAdapter<SRV> jsonAdapter(Moshi moshi) {
    return new AutoValue_SRV.MoshiJsonAdapter(moshi);
  }
}
