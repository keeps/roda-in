package org.roda.rodain.rules.sip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Properties;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23/11/2015.
 */
public class SipMetadata {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipMetadata.class.getName());
  private static Properties properties;
  private MetadataTypes type;
  private boolean loaded = false, modified = false;
  private String content;
  private Path path;
  private String resource;

  public SipMetadata(MetadataTypes type, Path path, String resource) {
    this.type = type;
    this.path = path;
    this.resource = resource;
  }

  public boolean isModified() {
    return modified;
  }

  private void loadMetadata() {
    try {
      if (type == MetadataTypes.NEWTEXT) {
        if (resource != null) {
          String fileName;
          if ("EAD-C".equals(resource)) {
            fileName = properties.getProperty("metadata.template.ead");
          } else
            fileName = properties.getProperty("metadata.template.dcmes");

          InputStream contentStream = ClassLoader.getSystemResource(fileName).openStream();
          content = Utils.convertStreamToString(contentStream);
          contentStream.close();
        }
      } else {
        if (path != null) {
          content = Utils.readFile(path.toString(), Charset.defaultCharset());
          loaded = true;
        }
      }
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
    }
  }

  /**
   * Gets the metadata content of a SIP, loading it from the disk if that action
   * hasn't been previously done.
   *
   * @return The metadata content of the SIP.
   */
  public String getMetadataContent() {
    if (!loaded) {
      loadMetadata();
    }
    return content;
  }

  /**
   * Updates the metadata content of the SIP.
   * 
   * @param meta
   *          The new metadata content.
   */
  public void update(String meta) {
    modified = true;
    content = meta;
  }

  public static void setProperties(Properties prop) {
    properties = prop;
  }
}
