package org.roda.rodain.rules.sip;

/**
 * Created by adrapereira on 08-02-2016.
 */
public class MetadataValue {
  private String title, value, xpathDestination;

  public MetadataValue(String title, String value, String xpath) {
    this.title = title;
    this.value = value;
    this.xpathDestination = xpath;
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

  public String getXpathDestination() {
    return xpathDestination;
  }
}
