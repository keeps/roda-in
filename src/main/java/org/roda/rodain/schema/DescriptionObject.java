
package org.roda.rodain.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionObject {

  private String title;
  private String id;
  private String parentId;
  private String descriptionlevel;
  private List<DescObjMetadata> metadata = new ArrayList<>();
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * 
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * 
   * @param title
   *          The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

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
   * @return The parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * 
   * @param parentId
   *          The parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * 
   * @return The descriptionlevel
   */
  public String getDescriptionlevel() {
    return descriptionlevel;
  }

  /**
   * 
   * @param descriptionlevel
   *          The descriptionlevel
   */
  public void setDescriptionlevel(String descriptionlevel) {
    this.descriptionlevel = descriptionlevel;
  }

  /**
   * 
   * @return The metadata
   */
  public List<DescObjMetadata> getMetadata() {
    return metadata;
  }

  /**
   * 
   * @param metadata
   *          The metadata
   */
  public void setMetadata(List<DescObjMetadata> metadata) {
    this.metadata = metadata;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
