package org.roda.rodain.core.shallowSipManager;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Configuration {
  private String basepath;
  private String host;
  private String protocol;

  public Configuration(final String basepath, final String host, final String protocol) {
    this.basepath = basepath;
    this.host = host;
    this.protocol = protocol;
  }

  public String getBasepath() {
    return basepath;
  }

  public void setBasepath(final String basepath) {
    this.basepath = basepath;
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(final String protocol) {
    this.protocol = protocol;
  }
}
