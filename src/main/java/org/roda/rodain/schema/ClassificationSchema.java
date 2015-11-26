package org.roda.rodain.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 22-09-2015.
 */
public class ClassificationSchema {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ClassificationSchema.class.getName());
  private List<DescriptionObject> dos;

  public ClassificationSchema() {
    dos = new ArrayList<DescriptionObject>();
  }

  public ClassificationSchema(List<DescriptionObject> dos) {
    this.dos = dos;
  }

  public List<DescriptionObject> getDos() {
    return dos;
  }

  public void setDos(List<DescriptionObject> dos) {
    this.dos = dos;
  }

  public static ClassificationSchema instantiate() {
    // read json file data to String
    try {
      InputStream input = ClassLoader.getSystemResourceAsStream("test.json");

      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();

      // convert json string to object
      return objectMapper.readValue(input, ClassificationSchema.class);
    } catch (IOException e) {
      log.error("Error reading classification schema specification", e);
    }
    return null;
  }

  @Override
  public String toString() {
    return "ClassificationSchema{" + "dos=" + dos + '}';
  }
}
