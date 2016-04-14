package org.roda.rodain.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-01-2016.
 */
public class LoggingOutputStream extends OutputStream {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingOutputStream.class.getName());
  private StringBuilder buffer = new StringBuilder();

  @Override
  public final void write(int b) throws IOException {
    char c = (char) b;
    if (c == '\n' || c == '\r') {
      LOGGER.warn(buffer.toString());
      buffer = new StringBuilder();
    } else
      buffer.append(c);
  }
}
