package org.roda.rodain.rules.sip;

import java.util.ArrayList;
import java.util.List;

import org.roda.rodain.utils.UIPair;

/**
 * Created by adrapereira on 08-02-2016.
 */
public class MetadataValue {
  private String title, id, fieldType;
  public String value;
  private List<String> xpathDestinations;
  private List<UIPair> fieldOptions;

  public MetadataValue(String id, String title, String value, String fieldType) {
    this.id = id;
    this.title = title;
    this.value = value;
    this.fieldType = fieldType;
    this.xpathDestinations = new ArrayList<>();
    this.fieldOptions = new ArrayList<>();
  }

  public String getId() {
    return id;
  }
  public String getTitle() {
    return title;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getFieldType() {
    return fieldType;
  }

  public List<String> getXpathDestinations() {
    return xpathDestinations;
  }

  public List<UIPair> getFieldOptions() {
    return fieldOptions;
  }

  public void addXpathDestination(String xpath) {
    xpathDestinations.add(xpath);
  }

  public void addFieldOption(UIPair option) {
    fieldOptions.add(option);
  }

  @Override
  public String toString() {
    return "MetadataValue{" + "title='" + title + '\'' + ",\nvalue='" + value + '\'' + ",\nxpathDestination='"
      + xpathDestinations + '\'' + "}\n";
  }
}
