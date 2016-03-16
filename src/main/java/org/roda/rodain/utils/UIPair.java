package org.roda.rodain.utils;

/**
 * A class to hold a key-value pair, but the toString method only returns the
 * value. This is useful for comboBoxes, because we may want to have data
 * associated to the String being displayed.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 */
public class UIPair {
  private Object key, value;

  /**
   * Creates a new UIPair object
   *
   * @param key
   *          The key object
   * @param value
   *          The value object
   */
  public UIPair(Object key, Object value) {
    this.key = key;
    this.value = value;
  }

  /**
   * @return The key object
   */
  public Object getKey() {
    return key;
  }

  /**
   * @return The value object
   */
  public Object getValue() {
    return value;
  }

  /**
   * @return The value to string
   */
  @Override
  public String toString() {
    return value.toString();
  }
}
