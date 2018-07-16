package com.oneops.infoblox.model.txt;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Text record {@link com.oneops.infoblox.model.DNSRecord#TXT } response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class TXT extends Record {

  public abstract String name();

  public abstract String text();

  public static Builder builder() {
    return new AutoValue_TXT.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder name(String name);

    public abstract Builder text(String text);

    public abstract TXT build();
  }

  public static JsonAdapter<TXT> jsonAdapter(Moshi moshi) {
    return new AutoValue_TXT.MoshiJsonAdapter(moshi);
  }
}
