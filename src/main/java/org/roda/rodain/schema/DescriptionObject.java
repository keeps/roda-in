package org.roda.rodain.schema;

import java.text.SimpleDateFormat;
import java.util.*;

import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.MetadataTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class DescriptionObject extends Observable {
  private String title, id, parentId, descriptionlevel;
  private String producer, creator, description, rights;
  private List<DescObjMetadata> metadata = new ArrayList<>();
  private Map<String, Object> additionalProperties = new TreeMap<>();

  public DescriptionObject() {
    title = I18n.t("root");
    id = UUID.randomUUID().toString();
    metadata.add(new DescObjMetadata(MetadataTypes.TEMPLATE, "ead", "2002"));
  }

  public void applyMetadataValues() {
    if (!metadata.isEmpty()) {
      metadata.get(0).applyMetadataValues();
    }
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
   * @param title
   *          The title
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
   * @param id
   *          The id
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
   * @param parentId
   *          The parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Gets the description level of the description object
   *
   * @return The descriptionLevel
   */
  public String getDescriptionlevel() {
    return descriptionlevel;
  }

  /**
   * Sets the description level of the description object
   *
   * @param descriptionLevel
   *          The description level
   */
  public void setDescriptionlevel(String descriptionLevel) {
    this.descriptionlevel = descriptionLevel;
  }

  public String getProducer() {
    return producer;
  }

  @JsonIgnore
  public void setProducer(String producer) {
    this.producer = producer;
  }

  public String getCreator() {
    return creator;
  }

  @JsonIgnore
  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDescription() {
    return description;
  }

  @JsonIgnore
  public void setDescription(String description) {
    this.description = description;
  }

  public String getRights() {
    return rights;
  }

  @JsonIgnore
  public void setRights(String rights) {
    this.rights = rights;
  }

  public List<DescObjMetadata> getMetadata() {
    return metadata;
  }

  /**
   * Gets the metadata list of the description object, replacing the fields from
   * the template.
   *
   * @return The metadata list
   */
  @JsonIgnore
  public List<DescObjMetadata> getMetadataWithReplaces() {
    for (DescObjMetadata dom : metadata) {
      getMetadataWithReplaces(dom);
    }
    return metadata;
  }

  @JsonIgnore
  public DescObjMetadata getMetadataWithReplaces(DescObjMetadata dom) {
    String content = dom.getContentDecoded();
    if (content != null) {
      Template tmpl = Mustache.compiler().defaultValue("").compile(content);
      Map<String, String> data = new HashMap<>();
      data.put("title", title);
      data.put("dateInitial", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
      data.put("dateFinal", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
      data.put("now", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
      data.put("id", id);
      data.put("level", descriptionlevel);
      data.put("creator", creator);
      data.put("producer", producer);
      data.put("rights", rights);
      data.put("description", description);
      content = tmpl.execute(data);
      // we need to clean the '\r' character in windows,
      // otherwise the strings are different even if no modification has been
      // made
      content = content.replace("\r", "");
      dom.setContentDecoded(content);
    }
    return dom;
  }

  /**
   * Sets the metadata list of the description object
   *
   * @param metadata
   *          The metadata list
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
   * @param name
   *          The name of the property.
   * @param value
   *          The value of the property.
   */
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
