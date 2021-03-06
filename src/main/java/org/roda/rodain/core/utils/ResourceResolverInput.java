package org.roda.rodain.core.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class ResourceResolverInput implements LSInput {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolverInput.class.getName());
  private BufferedInputStream inputStream;
  private String publicId;
  private String systemId;

  public ResourceResolverInput(String publicId, String sysId, InputStream input) {
    this.publicId = publicId;
    this.systemId = sysId;
    this.inputStream = new BufferedInputStream(input);
  }

  @Override
  public String getPublicId() {
    return publicId;
  }

  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  @Override
  public String getBaseURI() {
    return null;
  }

  @Override
  public InputStream getByteStream() {
    return null;
  }

  @Override
  public boolean getCertifiedText() {
    return false;
  }

  @Override
  public Reader getCharacterStream() {
    return null;
  }

  @Override
  public String getEncoding() {
    return null;
  }

  @Override
  public String getStringData() {
    synchronized (inputStream) {
      try {
        byte[] input = new byte[inputStream.available()];
        inputStream.read(input);
        return new String(input);
      } catch (IOException e) {
        LOGGER.error("Unable to get string", e);
        return null;
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          LOGGER.error("Error closing stream", e);
        }
      }
    }
  }

  @Override
  public void setBaseURI(String baseURI) {
  }

  @Override
  public void setByteStream(InputStream byteStream) {
  }

  @Override
  public void setCertifiedText(boolean certifiedText) {
  }

  @Override
  public void setCharacterStream(Reader characterStream) {
  }

  @Override
  public void setEncoding(String encoding) {
  }

  @Override
  public void setStringData(String stringData) {
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }
}
