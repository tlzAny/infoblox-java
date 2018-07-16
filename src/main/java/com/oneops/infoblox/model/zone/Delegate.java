package com.oneops.infoblox.model.zone;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Zone <b>delegates_to</b> entry.
 *
 * @author Suresh
 */
@AutoValue
public abstract class Delegate {

  public abstract String address();

  public abstract String name();

  public static Delegate of(String address, String name) {
    return builder().address(address).name(name).build();
  }

  public static Builder builder() {
    return new AutoValue_Delegate.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder address(String address);

    public abstract Builder name(String name);

    public abstract Delegate build();
  }

  public static JsonAdapter<Delegate> jsonAdapter(Moshi moshi) {
    return new AutoValue_Delegate.MoshiJsonAdapter(moshi);
  }
}
