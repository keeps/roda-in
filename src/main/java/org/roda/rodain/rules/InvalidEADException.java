package org.roda.rodain.rules;

import org.roda.rodain.schema.InvalidMetadataException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 18-02-2016.
 */
public class InvalidEADException extends InvalidMetadataException {
  private String message;

  /**
   * Creates a new InvalidEADException object.
   * 
   * @param mess
   *          The message of the exception.
   */
  public InvalidEADException(String mess) {
    super(mess);
    message = mess;
  }

  /**
   * @return The message of the exception
   */
  @Override
  public String getMessage() {
    return message;
  }
}
