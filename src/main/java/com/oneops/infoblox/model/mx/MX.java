package com.oneops.infoblox.model.mx;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Mail exchange record {@link com.oneops.infoblox.model.DNSRecord#MX } response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class MX extends Record {

  @Json(name = "mail_exchanger")
  public abstract String mailExchanger();

  public abstract String name();

  public abstract int preference();

  public static Builder builder() {
    return new AutoValue_MX.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder mailExchanger(String mailExchanger);

    public abstract Builder name(String name);

    public abstract Builder preference(int preference);

    public abstract MX build();
  }

  public static JsonAdapter<MX> jsonAdapter(Moshi moshi) {
    return new AutoValue_MX.MoshiJsonAdapter(moshi);
  }
}
