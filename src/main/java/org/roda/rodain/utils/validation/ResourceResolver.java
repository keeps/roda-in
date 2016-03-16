package org.roda.rodain.utils.validation;

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
  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    InputStream resourceAsStream = null;
    try {
      URL url = new URL(systemId);
      resourceAsStream = url.openStream();
    } catch (MalformedURLException e) {
      // the XSD's are expected to be in the root of the classpath
      resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(systemId);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new Input(publicId, systemId, resourceAsStream);
  }

}
