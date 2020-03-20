package com.oneops.infoblox.model.network;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.BaseRecord;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * network record {@link com.oneops.infoblox.model.DNSRecord#network } response.
 *
 * @author tlz
 */
@AutoValue
public abstract class Network extends BaseRecord {

  public abstract String network();

  @Nullable
  public abstract String comment();

  @Nullable
  public abstract Map<String, Map<String, String>> extattrs();

  @Nullable
  public abstract List<Map<String, Object>> options();

  public static Builder builder() {
    return new AutoValue_Network.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder network(String name);

    public abstract Builder comment(@Nullable String name);

    public abstract Builder extattrs(@Nullable Map<String, Map<String, String>> name);

    public abstract Builder options(@Nullable List<Map<String, Object>> name);

    public abstract Network build();
  }

  public static JsonAdapter<Network> jsonAdapter(Moshi moshi) {
    return new AutoValue_Network.MoshiJsonAdapter(moshi);
  }
}
