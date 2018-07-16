package com.oneops.infoblox.model.ns;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Name server record {@link com.oneops.infoblox.model.DNSRecord#NS } response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class NS extends Record {

  public abstract String name();

  public abstract String nameserver();

  public static Builder builder() {
    return new AutoValue_NS.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder name(String name);

    public abstract Builder nameserver(String nameserver);

    public abstract NS build();
  }

  public static JsonAdapter<NS> jsonAdapter(Moshi moshi) {
    return new AutoValue_NS.MoshiJsonAdapter(moshi);
  }
}
