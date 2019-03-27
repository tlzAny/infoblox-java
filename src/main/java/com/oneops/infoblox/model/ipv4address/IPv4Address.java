package com.oneops.infoblox.model.ipv4address;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.BaseRecord;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class IPv4Address extends BaseRecord {

  public abstract String ip_address();

  public abstract String status();

  public static Builder builder() {
    return new AutoValue_IPv4Address.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract IPv4Address.Builder ip_address(String address);

    public abstract IPv4Address.Builder status(String status);

    public abstract IPv4Address build();
  }

  public static JsonAdapter<IPv4Address> jsonAdapter(Moshi moshi) {
    return new AutoValue_IPv4Address.MoshiJsonAdapter(moshi);
  }
}
