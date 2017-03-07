package org.roda.rodain.core.template;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 08-02-2016.
 */
public class TemplateFieldValue implements Comparable {
  private String id;
  private Map<String, Object> options;

  public TemplateFieldValue() {
    this.id = null;
    this.options = new HashMap<>();
  }

  /**
   * Creates a new MetadataValue object.
   *
   * @param id
   *          The id of the MetadataValue object.
   * @param options
   *          The options map of the MetadataValue
   */
  public TemplateFieldValue(String id, Map<String, Object> options) {
    this.id = id;
    if (options == null || options.isEmpty()) {
      this.options = new HashMap<>();
    } else
      this.options = options;

    if (!this.options.containsKey("label")) {
      // We don't need a label if the field is hidden
      if (options.containsKey("hidden") && options.get("hidden").equals(Constants.MISC_TRUE)) {
        this.options.put("label", "");
      } else {
        this.options.put("label", getTitle(id));
      }
    }
  }

  /**
   * @return The ID of the object.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the value from the "options" map that corresponds to the key received
   * as argument.
   * 
   * @param key
   *          The key used to retrieve the value.
   * @return The value that corresponds to the key, from the options map. If the
   *         map doesn't contain the required key, it returns null.
   */
  public Object get(String key) {
    return options.get(key);
  }

  /**
   * Sets the option identified by the key argument with the value argument.
   * 
   * @param key
   *          The identifier of the option.
   * @param value
   *          The value of the option.
   */
  public void set(String key, Object value) {
    options.put(key, value);
  }

  /**
   * @return The options of the object.
   */
  public Map<String, Object> getOptions() {
    return options;
  }

  private static String getTitle(String var) {
    String result = null;
    try {
      result = I18n.t(Constants.CONF_K_PREFIX_METADATA + var);
    } catch (MissingResourceException e) {
      // we will use the name of the variable if there's no available title
      // no need to log the exception or rethrow it
    }
    if (result == null)
      result = var;
    return result;
  }

  /**
   * Returns a hash code value for the object.
   * 
   * @return The Hash code.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Checks the equality of two objects
   * 
   * @param obj
   *          The object that will be compared to the current object
   * @return True if the obj reference equals the current object's reference or
   *         if both IDs are equal. False otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof TemplateFieldValue) {
      TemplateFieldValue mv = (TemplateFieldValue) obj;
      return mv.getId().equals(this.getId());
    }
    return false;
  }

  /**
   * Compares two MetadataValue objects.
   *
   * The first deciding factor is the "order" option, if it's defined on both
   * objects. Objects with the "order" defined always appear first than objects
   * without that property. In cases where both objects have an "order", the
   * parsed integer of both values is compared.
   *
   * When neither object has the "order" option defined, its labels are compared
   * alphabetically.
   * 
   * @param o
   *          The object that will be compared to the current object.
   * @return the value 0 if x == y; a value less than 0 if x < y; and a value
   *         greater than 0 if x > y
   */
  @Override
  public int compareTo(Object o) {
    if (o == this) {
      return 0;
    }
    if (o instanceof TemplateFieldValue) {
      // Compare the order option
      TemplateFieldValue mv = (TemplateFieldValue) o;
      Object selfOrder = get("order");
      Object mvOrder = mv.get("order");
      if (selfOrder != null) {
        if (mvOrder != null) {
          int selfInt = selfOrder instanceof String ? Integer.parseInt((String) selfOrder) : (Integer) selfOrder;
          int mvInt = mvOrder instanceof String ? Integer.parseInt((String) mvOrder) : (Integer) mvOrder;
          int result = Integer.compare(selfInt, mvInt);
          if (result != 0)
            return result;
        }
        return -1;
      } else if (mvOrder != null) {
        return 1;
      }
      // Compare the labels as a fallback
      String selfLabel = (String) get("label");
      String mvLabel = (String) mv.get("label");
      if (selfLabel != null) {
        return selfLabel.compareTo(mvLabel);
      }
    }
    return 0;
  }
}
