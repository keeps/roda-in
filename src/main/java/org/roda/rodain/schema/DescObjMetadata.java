package org.roda.rodain.schema;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class DescObjMetadata {
  private String id;
  private String type;
  private String content;
  private String contentEncoding;
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * Gets the id of the description object metadata.
   *
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the description object metadata.
   *
   * @param id The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the type of the description object metadata.
   *
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type of the description object metadata.
   *
   * @param type The type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the content of the description object metadata.
   *
   * @return The content
   */
  public String getContent() {
    return this.content;
  }

  /**
   * Gets the decoded content of the description object metadata.
   *
   * @return The content decoded
   */
  @JsonIgnore
  public String getContentDecoded() {
    byte[] decoded = Base64.decodeBase64(content);
    return new String(decoded);
  }

  /**
   * Sets the content of the description object metadata.
   *
   * @param content The content enconded in Base64
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Sets the content of the description object metadata.
   *
   * @param content The decoded content
   */
  public void setContentDecoded(String content) {
    byte[] encoded = Base64.encodeBase64(content.getBytes());
    this.content = new String(encoded);
  }

  /**
   * Gets the content encoding of the description object metadata.
   *
   * @return The content encoding
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Sets the content encoding of the description object metadata.
   *
   * @param contentEncoding The contentEncoding
   */
  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  /**
   * Gets the additional properties map.
   *
   * @return The additional properties map.
   */
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  /**
   * Sets an additional property.
   *
   * @param name  The name of the property.
   * @param value The value of the property.
   */
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
