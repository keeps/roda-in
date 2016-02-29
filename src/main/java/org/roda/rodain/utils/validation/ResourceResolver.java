package org.roda.rodain.utils.validation;

import java.io.InputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class ResourceResolver implements LSResourceResolver {
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

    // the XSD's are expected to be in the root of the classpath
    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(systemId);
    return new Input(publicId, systemId, resourceAsStream);
  }

}
