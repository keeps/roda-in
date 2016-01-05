package org.roda.rodain.schema;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class ClassificationSchema {
  private static final Logger log = LoggerFactory.getLogger(ClassificationSchema.class.getName());
  private List<DescriptionObject> dos = new ArrayList<>();
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Get the list of DescriptionObjects in the classification schema.
   *
   * @return The description objects list
   */
  public List<DescriptionObject> getDos() {
    return dos;
  }

  /**
   * Sets the list of DescriptionObjects.
   *
   * @param dos The description objects list
   */
  public void setDos(List<DescriptionObject> dos) {
    this.dos = dos;
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

  public void export(String fileName) {
    try {
      OutputStream outputStream = new FileOutputStream(fileName);
      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      // convert object to json string
      objectMapper.writeValue(outputStream, this);
    } catch (IOException e) {
      log.error("Error exporting classification scheme", e);
    }
  }

}
