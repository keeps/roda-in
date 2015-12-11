
package org.roda.rodain.schema;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.util.Base64;

public class DescObjMetadata {

  private String id;
  private String type;
  private String content;
  private String contentEncoding;
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * 
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * 
   * @param id
   *          The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * 
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * 
   * @param type
   *          The type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * 
   * @return The content
   */
  public String getContent() {
    return content;
  }

  /**
   * 
   * @param content
   *          The content
   */
  public void setContent(String content) {
    byte[] decoded = Base64.decodeBase64(content);
    this.content = new String(decoded);
  }

  /**
   * 
   * @return The contentEncoding
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * 
   * @param contentEncoding
   *          The contentEncoding
   */
  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
