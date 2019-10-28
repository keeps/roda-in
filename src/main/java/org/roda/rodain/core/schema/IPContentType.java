package org.roda.rodain.core.schema;

import org.roda.rodain.core.Constants;

public class IPContentType {
  private String packageType;
  private String value;
  private String otherValue;

  public IPContentType(String value) {
    super();
    this.packageType = Constants.SIP_DEFAULT_PACKAGE_TYPE;
    this.value = value;
    this.otherValue = "";
  }

  public IPContentType(String packageType, String value) {
    super();
    this.packageType = packageType;
    this.value = value;
    this.otherValue = "";
  }

  public IPContentType(IPContentType ipContentType) {
    super();
    this.packageType = ipContentType.getPackageType();
    this.value = ipContentType.getValue();
    this.otherValue = ipContentType.getOtherValue();
  }

  public String getPackageType() {
    return packageType;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getOtherValue() {
    return otherValue;
  }

  public void setOtherValue(String otherValue) {
    this.otherValue = otherValue;
  }

  public static IPContentType defaultIPContentType() {
    return new IPContentType(Constants.SIP_DEFAULT_PACKAGE_TYPE, Constants.SIP_DEFAULT_CONTENT_TYPE);
  }

  public String asString() {
    return packageType + " - " + value;
  }

  public String asStringWithOtherValue() {
    return packageType + " - " + value + " (" + otherValue + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((otherValue == null) ? 0 : otherValue.hashCode());
    result = prime * result + ((packageType == null) ? 0 : packageType.hashCode());
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
    IPContentType other = (IPContentType) obj;
    if (otherValue == null) {
      if (other.otherValue != null)
        return false;
    } else if (!otherValue.equals(other.otherValue))
      return false;
    if (packageType == null) {
      if (other.packageType != null)
        return false;
    } else if (!packageType.equals(other.packageType))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
