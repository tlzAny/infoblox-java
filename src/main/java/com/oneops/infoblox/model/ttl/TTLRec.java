package com.oneops.infoblox.model.ttl;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * TTL response of a dns record.
 *
 * @author Suresh
 */
@AutoValue
public abstract class TTLRec extends Record {

  public static Builder builder() {
    return new AutoValue_TTLRec.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {
    public abstract TTLRec build();
  }

  public static JsonAdapter<TTLRec> jsonAdapter(Moshi moshi) {
    return new AutoValue_TTLRec.MoshiJsonAdapter(moshi);
  }
}
