package com.oneops.infoblox.model;

import com.oneops.infoblox.model.ref.Ref;
import com.oneops.infoblox.model.ref.RefObject;
import com.squareup.moshi.Json;

/**
 * Base class for all Infoblox types.
 *
 * @author Suresh
 */
public abstract class BaseRecord {

  /**
   * Record object reference.
   *
   * @return {@link Ref}
   */
  @Json(name = "_ref")
  public abstract @RefObject Ref ref();

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
  }
}
