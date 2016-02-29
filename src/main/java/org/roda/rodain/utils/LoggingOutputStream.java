package org.roda.rodain.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-01-2016.
 */
public class LoggingOutputStream extends OutputStream {
  private static final Logger log = LoggerFactory.getLogger(LoggingOutputStream.class.getName());
  private StringBuilder buffer = new StringBuilder();

  @Override
  public final void write(int b) throws IOException {
    char c = (char) b;
    if (c == '\n' || c == '\r') {
      log.warn(buffer.toString());
      System.out.println(buffer.toString());
      buffer = new StringBuilder();
    } else
      buffer.append(c);
  }
}
