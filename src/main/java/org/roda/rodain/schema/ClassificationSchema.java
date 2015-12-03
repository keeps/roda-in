
package org.roda.rodain.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassificationSchema {

  private List<DescriptionObject> dos = new ArrayList<>();
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * 
   * @return The dos
   */
  public List<DescriptionObject> getDos() {
    return dos;
  }

  /**
   * 
   * @param dos
   *          The dos
   */
  public void setDos(List<DescriptionObject> dos) {
    this.dos = dos;
  }

  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    }

}
