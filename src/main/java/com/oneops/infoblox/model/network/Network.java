package com.oneops.infoblox.model.network;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.BaseRecord;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * network record {@link com.oneops.infoblox.model.DNSRecord#network } response.
 *
 * @author tlz
 */
@AutoValue
public abstract class Network extends BaseRecord {

  public abstract String network();

  public static Builder builder() {
    return new AutoValue_Network.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder network(String name);

    public abstract Network build();
  }

  public static JsonAdapter<Network> jsonAdapter(Moshi moshi) {
    return new AutoValue_Network.MoshiJsonAdapter(moshi);
  }
}
