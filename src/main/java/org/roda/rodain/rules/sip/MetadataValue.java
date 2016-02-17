package org.roda.rodain.rules.sip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrapereira on 08-02-2016.
 */
public class MetadataValue {
  private String title, id;
  public String value;
  private List<String> xpathDestinations;

  public MetadataValue(String id, String title, String value) {
    this.id = id;
    this.title = title;
    this.value = value;
    this.xpathDestinations = new ArrayList<>();
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

  public List<String> getXpathDestinations() {
    return xpathDestinations;
  }

  public void addXpathDestination(String xpath) {
    xpathDestinations.add(xpath);
  }

  @Override
  public String toString() {
    return "MetadataValue{" + "title='" + title + '\'' + ",\nvalue='" + value + '\'' + ",\nxpathDestination='"
      + xpathDestinations + '\'' + "}\n";
  }
}
