package com.oneops.infoblox.model;

import com.oneops.infoblox.model.ref.Ref;
import com.oneops.infoblox.model.ref.RefObject;
import com.squareup.moshi.Json;

/**
 * Base class for all Infoblox records.
 *
 * @author Suresh
 */
public abstract class Record {

  /**
   * Record object reference.
   *
   * @return {@link Ref}
   */
  @Json(name = "_ref")
  public abstract @RefObject Ref ref();

  /**
   * IBA view.
   *
   * @return view name string
   */
  public abstract String view();

  /**
   * TTL A 32-bit integer (range from 0 to 4294967295) that represents the duration in seconds that
   * the record is cached. Zero indicates that the record should not be cached.The default value is
   * undefined which indicates that the record inherits the TTL value of the zone.
   *
   * @return ttl.
   */
  public abstract int ttl();

  /** Base record builder. Uses <b>self-bounding generic</b> pattern. */
  public abstract static class RecBuilder<T extends RecBuilder<T>> {

    /**
     * Sets the object reference of the record.
     *
     * @param ref {@link Ref} object
     * @return T
     */
    public abstract T ref(Ref ref);

    /**
     * Sets the object reference of the record.
     *
     * @param ref String
     * @return T
     */
    public T ref(String ref) {
      return ref(Ref.of(ref));
    }

    /**
     * Sets the dns view name.
     *
     * @param view String
     * @return T
     */
    public abstract T view(String view);

    /**
     * Sets the record TTL
     *
     * @param ttl A 32-bit integer
     * @return T
     */
    public abstract T ttl(int ttl);
  }
}
