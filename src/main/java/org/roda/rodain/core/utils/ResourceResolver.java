package org.roda.rodain.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class ResourceResolver implements LSResourceResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolver.class.getName());

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    InputStream resourceAsStream = null;
    try {
      URL url = new URL(systemId);
      resourceAsStream = url.openStream();
    } catch (MalformedURLException e) {
      // the XSD's are expected to be in the root of the classpath
      resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(systemId);
      // we use this catch exception to check if the systemID is a URL or a
      // file, that's why we don't re-throw it or LOGGER it
    } catch (IOException e) {
      LOGGER.error("Can't get file from URL", e);
    }
    return new ResourceResolverInput(publicId, systemId, resourceAsStream);
  }

}
