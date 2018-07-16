package com.oneops.infoblox.model.ptr;

import com.google.auto.value.AutoValue;
import com.oneops.infoblox.model.Record;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.net.InetAddress;
import javax.annotation.Nullable;

/**
 * Pointer record {@link com.oneops.infoblox.model.DNSRecord#PTR } response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class PTR extends Record {

  @Nullable
  public abstract String ipv4addr();

  @Nullable
  public abstract String ipv6addr();

  public abstract String name();

  /** PTR Domain name */
  public abstract String ptrdname();

  public static Builder builder() {
    return new AutoValue_PTR.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder extends RecBuilder<Builder> {

    public abstract Builder ipv4addr(String ipv4addr);

    public abstract Builder ipv6addr(String ipv6addr);

    public abstract Builder name(String name);

    public abstract Builder ptrdname(String ptrdname);

    public abstract PTR build();
  }

  public static JsonAdapter<PTR> jsonAdapter(Moshi moshi) {
    return new AutoValue_PTR.MoshiJsonAdapter(moshi);
  }

  /**
   * Creates a reverse map name corresponding to an IPAddress for DNS names used in reverse
   * mappings. For the IPv4 address a.b.c.d, the reverse map name is <b>d.c.b.a.in-addr.arpa.</b>
   * For an IPv6 address, the reverse map name is <b>...ip6.arpa</b>.
   *
   * @param address The address from which to build a name.
   * @return The name corresponding to the address in the reverse map.
   */
  public static String reverseMapName(InetAddress address) {
    byte[] addr = address.getAddress();
    if (addr.length != 4 && addr.length != 16) {
      throw new IllegalArgumentException("Address must contain 4 or 16 bytes");
    }

    StringBuilder sb = new StringBuilder();
    if (addr.length == 4) {
      for (int i = addr.length - 1; i >= 0; i--) {
        sb.append(addr[i] & 0xFF);
        if (i > 0) sb.append(".");
      }
    } else {
      int[] nibbles = new int[2];
      for (int i = addr.length - 1; i >= 0; i--) {
        nibbles[0] = (addr[i] & 0xFF) >> 4;
        nibbles[1] = (addr[i] & 0xFF) & 0xF;
        for (int j = nibbles.length - 1; j >= 0; j--) {
          sb.append(Integer.toHexString(nibbles[j]));
          if (i > 0 || j > 0) sb.append(".");
        }
      }
    }

    return addr.length == 4
        ? String.format("%s.in-addr.arpa", sb.toString())
        : String.format("%s.ip6.arpa", sb.toString());
  }
}
