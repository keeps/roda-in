package org.roda.rodain.core;

/**
 * A class to hold a key-value pair, but the toString method only returns the
 * value. This is useful for comboBoxes, because we may want to have data
 * associated to the String being displayed.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 */
public class Pair<A, B> {

  private A key;
  private B value;

  /**
   * Creates a new UIPair object
   *
   * @param key
   *          The key object
   * @param value
   *          The value object
   */
  public Pair(A key, B value) {
    this.key = key;
    this.value = value;
  }

  /**
   * @return The key object
   */
  public A getKey() {
    return key;
  }

  /**
   * @return The value object
   */
  public B getValue() {
    return value;
  }

  /**
   * @return The value to string
   */
  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair other = (Pair) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
  
  
}
