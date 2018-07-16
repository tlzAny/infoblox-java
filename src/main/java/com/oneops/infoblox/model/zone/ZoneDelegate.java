package com.oneops.infoblox.model.zone;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;

/**
 * DNS Authoritative Zone.
 *
 * <p>Note: TTL field for this record is <b>delegated_ttl</b>
 *
 * @author Suresh G
 */
@AutoValue
public abstract class ZoneDelegate extends Record {

  public abstract String fqdn();

  @Json(name = "delegate_to")
  public abstract List<Delegate> delegateTo();

  @Override
  @Json(name = "delegated_ttl")
  public abstract int ttl();

  public abstract boolean locked();

  public static Builder builder() {
    return new AutoValue_ZoneDelegate.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder fqdn(String fqdn);

    public abstract Builder delegateTo(List<Delegate> delegateTo);

    public abstract Builder locked(boolean locked);

    public abstract ZoneDelegate build();
  }

  public static JsonAdapter<ZoneDelegate> jsonAdapter(Moshi moshi) {
    return new AutoValue_ZoneDelegate.MoshiJsonAdapter(moshi);
  }
}
