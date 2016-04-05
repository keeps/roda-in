package org.roda.rodain.rules;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19-10-2015.
 */
public enum MetadataTypes {
  SINGLE_FILE, SAME_DIRECTORY, DIFF_DIRECTORY, TEMPLATE, NEW_FILE;

  @JsonCreator
  public static MetadataTypes getEnumFromValue(String value) {
    if ("".equals(value))
      return SINGLE_FILE;
    for (MetadataTypes testEnum : values()) {
      if (testEnum.toString().equals(value)) {
        return testEnum;
      }
    }
    throw new IllegalArgumentException();
  }
}
