package com.oneops.infoblox.model.mx;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.ref.Ref;
import com.oneops.infoblox.model.ref.RefObject;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import javax.annotation.Nullable;

/**
 * Mail exchange record {@link com.oneops.infoblox.model.DNSRecord#MX } response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class MX {

  @RefObject
  @Json(name = "_ref")
  public abstract Ref ref();

  @Json(name = "mail_exchanger")
  public abstract String mailExchanger();

  public abstract String name();

  public abstract int preference();

  @Nullable
  public abstract String view();

  public static MX create(Ref ref, String mailExchanger, String name, int preference, String view) {
    return new AutoValue_MX(ref, mailExchanger, name, preference, view);
  }

  public static JsonAdapter<MX> jsonAdapter(Moshi moshi) {
    return new AutoValue_MX.MoshiJsonAdapter(moshi);
  }
}
