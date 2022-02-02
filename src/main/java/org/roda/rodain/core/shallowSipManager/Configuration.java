package org.roda.rodain.core.shallowSipManager;

import java.util.Optional;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Configuration {
  /**
   * The source basepath for URI creation.
   */
  private String sourceBasepath;
  /**
   * The host for URI creation.
   */
  private String host;
  /**
   * The protocol for URI creation.
   */
  private String protocol;
  /**
   * Target Base path for URI creation.
   */
  private String targetBasepath;

  /**
   * Port for URI creation.
   */
  private String port;

  /**
   * Creates a new Configuration for Shallow SIP URI creation.
   * 
   * @param sourceBasepath
   *          The source base path.
   * @param targetBasepath
   *          {@link Optional} target basepath.
   * @param host
   *          {@link Optional} host.
   * @param protocol
   *          The protocol.
   */
  public Configuration(final String sourceBasepath, final Optional<String> targetBasepath, final Optional<String> host,
    final String protocol, final Optional<String> port) {
    this.sourceBasepath = sourceBasepath;
    this.host = host.orElse(null);
    this.protocol = protocol;
    this.targetBasepath = targetBasepath.orElse(null);
    this.port = port.orElse(null);
  }

  public String getSourceBasepath() {
    return sourceBasepath;
  }

  public void setSourceBasepath(final String sourceBasepath) {
    this.sourceBasepath = sourceBasepath;
  }

  public String getTargetBasepath() {
    return targetBasepath;
  }

  public void setTargetBasepath(final String targetBasepath) {
    this.targetBasepath = targetBasepath;
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

  public String getPort() {
    return this.port;
  }
}
