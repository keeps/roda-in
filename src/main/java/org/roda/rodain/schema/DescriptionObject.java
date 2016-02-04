package org.roda.rodain.schema;

import org.roda.rodain.core.AppProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class DescriptionObject {
  private String title;
  private String id;
  private String parentId;
  private String descriptionlevel;
  private List<DescObjMetadata> metadata = new ArrayList<>();
  private Map<String, Object> additionalProperties = new HashMap<>();

  public DescriptionObject(){
    title = AppProperties.getLocalizedString("root");
  }

  /**
   * Gets the title of the description object
   *
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of the description object
   *
   * @param title The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the id of the description object
   *
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the description object
   *
   * @param id The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the parentId of the description object
   *
   * @return The parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Sets the parentId of the description object
   *
   * @param parentId The parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Gets the descriptionLevel of the description object
   *
   * @return The descriptionlevel
   */
  public String getDescriptionlevel() {
    return descriptionlevel;
  }

  /**
   * Sets the descriptionLevel of the description object
   *
   * @param descriptionlevel The descriptionlevel
   */
  public void setDescriptionlevel(String descriptionlevel) {
    this.descriptionlevel = descriptionlevel;
  }

  /**
   * Gets the metadata list of the description object
   *
   * @return The metadata list
   */
  public List<DescObjMetadata> getMetadata() {
    return metadata;
  }

  /**
   * Sets the metadata list of the description object
   *
   * @param metadata The metadata list
   */
  public void setMetadata(List<DescObjMetadata> metadata) {
    this.metadata = metadata;
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
