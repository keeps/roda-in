package org.roda.rodain.rules;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19-10-2015.
 */
public enum MetadataOptions {
  SINGLE_FILE, SAME_DIRECTORY, DIFF_DIRECTORY, TEMPLATE, NEW_FILE;

  /**
   * Translates the value that was serialized to the Enum.
   * @param value The serialized value.
   * @return The translated value.
   */
  @JsonCreator
  public static MetadataOptions getEnumFromValue(String value) {
    if ("".equals(value))
      return SINGLE_FILE;
    for (MetadataOptions testEnum : values()) {
      if (testEnum.toString().equals(value)) {
        return testEnum;
      }
    }
    throw new IllegalArgumentException();
  }
}
