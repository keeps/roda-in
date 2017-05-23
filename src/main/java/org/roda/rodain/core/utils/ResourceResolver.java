package org.roda.rodain.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class ResourceResolver implements LSResourceResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolver.class.getName());

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    LOGGER.info("ResourceResolver | type: {}; namespaceURI: {}; publicId: {}; systemId: {}; baseURI: {}", type,
      namespaceURI, publicId, systemId, baseURI);
    InputStream resourceAsStream = null;
    try {
      URL url = new URL(systemId);
      resourceAsStream = url.openStream();
    } catch (MalformedURLException e) {
      // 20170519 hsilva: no, the schemas should be obtained from RODA-in home
      // schemas folder
      // the XSD's are expected to be in the root of the classpath

      try {
        Path schemaPath = ConfigurationManager.getRodainPath().resolve(Constants.FOLDER_SCHEMAS).resolve(systemId);
        resourceAsStream = Files.newInputStream(schemaPath);
      } catch (IOException e1) {
        resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(systemId);
      }

      // we use this catch exception to check if the systemID is a URL or a
      // file, that's why we don't re-throw it or LOGGER it
    } catch (IOException e) {
      LOGGER.error("Can't get file from URL", e);
    }
    return new ResourceResolverInput(publicId, systemId, resourceAsStream);
  }

}
