package org.roda.rodain.rules;

import org.roda.rodain.schema.InvalidMetadataException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 18-02-2016.
 */
public class InvalidEADException extends InvalidMetadataException {
  private String message;

  public InvalidEADException(String mess) {
    super(mess);
    message = mess;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
