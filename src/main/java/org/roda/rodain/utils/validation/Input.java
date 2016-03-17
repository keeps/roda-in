package org.roda.rodain.utils.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class Input implements LSInput {
  private static final Logger log = LoggerFactory.getLogger(Input.class.getName());
  private BufferedInputStream inputStream;
  private String publicId;
  private String systemId;

  public Input(String publicId, String sysId, InputStream input) {
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
        String result = new String(input);
        return result;
      } catch (IOException e) {
        log.error("Unable to get string", e);
        return null;
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error("Error closing stream", e);
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

  public BufferedInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(BufferedInputStream inputStream) {
    this.inputStream = inputStream;
  }
}
